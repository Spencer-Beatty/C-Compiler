package regalloc;

import gen.asm.AssemblyPass;
import gen.asm.AssemblyProgram;
import gen.asm.Register;

import java.util.*;

public class GraphColouringRegAlloc implements AssemblyPass {

    public static final GraphColouringRegAlloc INSTANCE = new GraphColouringRegAlloc();


    @Override
    public AssemblyProgram apply(AssemblyProgram program) {

        AssemblyProgram newProg = new AssemblyProgram();
        ControlFlowGraph cfg = new ControlFlowGraph();
        cfg.CreateControlFlowGraph(program);
        // display writes control flow graph to predetermined file graph.gv
        cfg.Display();

        FixPointLivenessAnalysis(cfg);
        // To complete
        DisplayRegisters(cfg);

        return newProg;
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
                    //todo
                    // implement FixPointLivenessAnalysis
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

    public void DisplayRegisters(ControlFlowGraph cfg){
        for(ControlFlowNode cf : cfg.functionStartNodes){
            cf.DisplayNodeRegisters();
        }
    }



}

class ControlFlowNodeComparator implements Comparator<ControlFlowNode>{
    public int compare(ControlFlowNode c1, ControlFlowNode c2){
        if(c1.lineNumber < c2.lineNumber){
            return 1;
        }else{
            return -1;
        }
    }
}
