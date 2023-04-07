package regalloc;

import gen.asm.*;

import java.util.*;

public class GraphColouringRegAlloc implements AssemblyPass {

    public static final GraphColouringRegAlloc INSTANCE = new GraphColouringRegAlloc();

    @Override
    public AssemblyProgram apply(AssemblyProgram program) {

        AssemblyProgram newProg = new AssemblyProgram();
        ControlFlowGraph cfg = new ControlFlowGraph();
        cfg.CreateControlFlowGraph(program);
        // display writes control flow graph to predetermined file graph.gv
        //cfg.Display();

        FixPointLivenessAnalysis(cfg);

        //DisplayRegisters(cfg);

        ArrayList<Set<Register>> encountered = EncounteredRegisterSets(cfg);
        InterferenceGraph ig = new InterferenceGraph(cfg, encountered);
        //ig.DrawGraph();

        //System.out.println(ig.archMapping.size());



        newProg = run(program, ig);
        return newProg;
    }

    private static AssemblyProgram run(AssemblyProgram prog, InterferenceGraph ig) {

        AssemblyProgram newProg = new AssemblyProgram();
        // emit labels for all virtual registers
        AssemblyProgram.Section sec = newProg.newSection(AssemblyProgram.Section.Type.DATA);
        for(Label lab : ig.virtualMapping.values()){
            sec.emit(lab);
            sec.emit(new Directive("space 4"));
        }
        // we assume that each function has a single corresponding text section
        prog.sections.forEach(section -> {
            if (section.type == AssemblyProgram.Section.Type.DATA)
                newProg.emitSection(section);
            else {
                assert (section.type == AssemblyProgram.Section.Type.TEXT);

                // map from virtual register to corresponding uniquely created label

                final AssemblyProgram.Section newSection = newProg.newSection(AssemblyProgram.Section.Type.TEXT);

                section.items.forEach((item) -> {
                    switch (item) {
                        case (Comment comment) -> newSection.emit(comment);
                        case (Label label) -> newSection.emit(label);
                        case (Directive directive) -> newSection.emit(directive);
                        case (Instruction insn) -> {
                            if (insn == Instruction.Nullary.pushRegisters) {
                                newSection.emit("Original instruction: pushRegisters");
                            /*for (Label l : vrLabels) {
                                // load content of memory at label into $t0
                                newSection.emit(OpCode.LA, Register.Arch.t0, l);
                                newSection.emit(OpCode.LW, Register.Arch.t0, Register.Arch.t0, 0);

                                // push $t0 onto stack
                                newSection.emit(OpCode.ADDI, Register.Arch.sp, Register.Arch.sp, -4);
                                newSection.emit(OpCode.SW, Register.Arch.t0, Register.Arch.sp, 0);
                            }
                            */
                            } else if (insn == Instruction.Nullary.popRegisters) {
                                newSection.emit("Original instruction: popRegisters");
                            /*
                            for (Label l : reverseVrLabels) {
                                // pop from stack into $t0
                                newSection.emit(OpCode.LW, Register.Arch.t0, Register.Arch.sp, 0);
                                newSection.emit(OpCode.ADDI, Register.Arch.sp, Register.Arch.sp, 4);

                                // store content of $t0 in memory at label
                                newSection.emit(OpCode.LA, Register.Arch.t1, l);
                                newSection.emit(OpCode.SW, Register.Arch.t0, Register.Arch.t1, 0);
                            }*/
                            } else
                                emitInstructionWithoutVirtualRegister(insn, newSection, ig.archMapping, ig.virtualMapping);
                        }
                    }
                });
            }
        });
        return newProg;
    }

    private static void emitInstructionWithoutVirtualRegister(Instruction insn, AssemblyProgram.Section section, HashMap<Register, Register> map,
                                                              HashMap<Register, Label> virtualMap){

        Register reg1 = null;
        Register reg2 = null;
        Register reg3 = null;
        int counter = 0;
        if(virtualMap.containsKey(insn.def())) {
            reg1 = insn.def();
            counter++;
        }
        for(Register reg : insn.uses()) {
            if (virtualMap.containsKey(reg)) {
                if (counter == 0 || counter == 1) {
                    // cases where reg1 is assigned or not
                    counter = 2;
                    reg2 = reg;
                    section.emit(OpCode.LA, Register.Arch.t0, virtualMap.get(reg));
                    section.emit(OpCode.LW, Register.Arch.t0, Register.Arch.t0, 0);
                } else {
                    assert (counter == 3);
                    reg3 = reg;
                    section.emit(OpCode.LA, Register.Arch.t1, virtualMap.get(reg));
                    section.emit(OpCode.LW, Register.Arch.t1, Register.Arch.t1, 0);
                }
            }
        }


        if(counter == 0){
            section.emit(insn.rebuild(map));
        }else{
            HashMap<Register, Register> newMap = new HashMap<>();
            newMap.putAll(map);
            boolean storeWord = false;
            if(reg1!=null){
                // reg1 is always def set
                newMap.put(reg1, Register.Arch.t0);
                storeWord = true;
            }
            if(reg2!=null){
                if(reg3!=null && reg3==reg1){
                    // take t1
                    newMap.putIfAbsent(reg2, Register.Arch.t1);
                }else{
                    newMap.putIfAbsent(reg2, Register.Arch.t0);
                }
            }
            if(reg3!=null){
                // put if absent menans if reg3==reg1 reg1's mapping wont be overwritten
                newMap.putIfAbsent(reg3, Register.Arch.t1);
            }


            section.emit(insn.rebuild(newMap));
            if(storeWord){
                //need to store value in reg1
                // no longer need value in t1
                section.emit(OpCode.LA, Register.Arch.t1, virtualMap.get(reg1));
                section.emit(OpCode.SW, Register.Arch.t0,Register.Arch.t1, 0);
            }

        }





    }
    private static Map<Register.Virtual, Label>  collectVirtualRegisters(AssemblyProgram.Section section) {
        final Map<Register.Virtual, Label> vrMap = new HashMap<>();

        section.items.forEach((item) -> {
            switch (item) {
                case Instruction insn -> insn.registers().forEach(reg -> {
                    if (reg instanceof Register.Virtual) {
                        Register.Virtual vr = (Register.Virtual) reg;
                        Label l = Label.create(vr.toString());
                        vrMap.put(vr, l);
                    }
                });
                default -> {} // nothing to do
            }
        });

        return vrMap;
    }

    public void FixPointLivenessAnalysis(ControlFlowGraph cfg){
        // first step of setting livein and liveout can be skipped because
        // it is already done when controlflownodes are initialized
        for(ArrayList<ControlFlowNode> nodeList : cfg.sortedArrays){
            if(nodeList == null){
                System.out.println("Node list null");
                continue;
            }
            Collections.reverse(nodeList);
            boolean noChange = false;
            int counter = 1;
            while(!noChange){
                // set noChange = true at start of loop, update to false if a change occurs
                noChange = true;
                System.out.println(counter);
                counter++;
                for(ControlFlowNode cf : nodeList){
                    Set<Register> LiveInPrime = new HashSet<>();
                    Set<Register> LiveOutPrime = new HashSet<>();
                    // Calculate Live in'
                    LiveInPrime.addAll(cf.GetLiveIn());

                    // Calculate Live out'
                    LiveOutPrime.addAll(cf.GetLiveOut());

                    // Calculate Live out
                    Set<Register> LiveOut =  cf.LiveOut();

                    // Calculate Live in
                    Set<Register> LiveIn = cf.LiveIn();

                    if(noChange == false){
                        // indicates there was change and another iteration must commence
                        continue;
                    }else{
                        if(LiveInPrime.equals(LiveIn) &&
                        LiveOutPrime.equals(LiveOut)){
                            continue;
                        }
                        // sets not equal change has occured
                        noChange = false;
                    }

                }
            }



        }
    }

    public ArrayList<Set<Register>> EncounteredRegisterSets(ControlFlowGraph cfg){
        HashSet<Register> fullArchSet = new HashSet<>(Arrays.asList(Register.Arch.ra, Register.Arch.a0,
                Register.Arch.a1,Register.Arch.a2,Register.Arch.a3,Register.Arch.sp, Register.Arch.fp,
                Register.Arch.v1, Register.Arch.zero, Register.Arch.gp));

        ArrayList<Set<Register>> encountered = new ArrayList<>();
        for(ArrayList<ControlFlowNode> nodeList : cfg.sortedArrays) {
            Set<Register> set = new HashSet<>();
            if (nodeList == null) {
                System.out.println("Node list null");
                continue;
            }
            for(ControlFlowNode cf : nodeList){
                set.addAll(cf.GetLiveOut());
                set.addAll(cf.GetLiveIn());
            }
            encountered.add(set);
        }

        for(Set<Register> set : encountered) {
            set.removeAll(fullArchSet);
        }
        return encountered;
    }

    public void DisplayRegisters(ControlFlowGraph cfg){
        for(ControlFlowNode cf : cfg.functionStartNodes){
            cf.DisplayNodeRegisters();
        }
    }



}


