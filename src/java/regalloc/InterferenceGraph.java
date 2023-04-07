package regalloc;

import gen.asm.AssemblyProgram;
import gen.asm.Label;
import gen.asm.Register;

import java.util.*;

public class InterferenceGraph {
    public ArrayList<Set<Register>> setList;
    public ArrayList<InterferenceMatrix> interferenceMatrices;
    public HashMap<Register, Register> archMapping;
    public HashMap<Register, Label> virtualMapping;

    /**
     * ControlFlowGraph should have passed through liveness analysis prior to
     * Interference Graph generation
     */
    public InterferenceGraph(ControlFlowGraph cfg, ArrayList<Set<Register>> generatedSetList){
        this.interferenceMatrices = new ArrayList<>();
        this.archMapping = new HashMap<>();
        this.virtualMapping = new HashMap<>();
        setList = generatedSetList;
        // one arrayList for each function
        for(int i = 0; i<cfg.sortedArrays.size(); i++){
            // generate interference matrix;
            Register[] a = new Register[setList.get(i).size()];
            InterferenceMatrix mat = new InterferenceMatrix(setList.get(i).size(), setList.get(i).toArray(a));
            // generate edges for each cf node in cfg
            for(ControlFlowNode cf : cfg.sortedArrays.get(i)){
                AddEdges(mat, cf.GetLiveIn());
                AddEdges(mat, cf.GetLiveOut());
            }
            UpdateEdgeMatrix(mat);
            interferenceMatrices.add(mat);
        }

        for(InterferenceMatrix mat : interferenceMatrices){
            archMapping = new HashMap<>();
            archMapping.putAll(ChaitansAlgorithm(mat));
        }
    }

    /**
     * Displays Interference Graph
     */
    public void DrawGraph(){
        for(InterferenceMatrix mat : interferenceMatrices){
            System.out.println("Adjacency Matrix : \n");

            int max = 0;
            for(int i = 0; i<mat.size; i++){
                if(mat.regArrayCopy[i].toString().length() > max){
                    max = mat.regArrayCopy[i].toString().length();
                }
            }
            LineUp(max);
            System.out.print("_"+ "_|");
            for(int i = 0; i<mat.size; i++){
                System.out.print(mat.regArrayCopy[i].toString());
                System.out.print(" |");
            }



            System.out.print("\n");
             for(int i = 0; i<mat.size; i++){
                 System.out.print(mat.regArrayCopy[i] + " |");
                 int size = max - mat.regArrayCopy[i].toString().length();
                 LineUp(size);
                 for(int j = 0; j< mat.size; j++){

                     LineUp(mat.regArrayCopy[j].toString().length());
                     System.out.print(mat.adjMatrix[i][j]);
                     System.out.print("|");
                 }
                 System.out.print("\n");
             }

        }

    }
    private void LineUp(int i){
        StringBuilder sb = new StringBuilder();
        for(int j = 0;j<i;j++){
            sb.append(" ");
        }
        System.out.print(sb);
    }

    public void UpdateEdgeMatrix(InterferenceMatrix mat){
        for(int i = 0; i< mat.adjMatrix.length; i ++){
            for(int j = 0; j< mat.adjMatrix.length ; j++){
                mat.edgeMatrix[i] += mat.adjMatrix[i][j];
            }
        }
    }
    /**
     * Updates matrix to include edges between registers
     * @param mat
     * @param registerSet
     */
    private void AddEdges(InterferenceMatrix mat, Set<Register> registerSet){

        Register[] regArray = new Register[registerSet.size()];
        registerSet.toArray(regArray);
        for(int i = 0; i< regArray.length; i ++){
            for(int j = 0; j< regArray.length ; j++){
                mat.AddEdge(regArray[i], regArray[j]);
            }
        }

    }

    public HashMap<Register, Register> ChaitansAlgorithm(InterferenceMatrix mat){
        Stack<Integer> regStack = new Stack<>();
        Set<Integer> regSet = new HashSet<>();
        int size = mat.size;
        int spillCounter = 0;
        while(regStack.size() + spillCounter < size){
            //Step 1:
            for(int i = 0; i< size; i++){
                // max edge represents the index of the virtual register within the array
                int maxEdge = mat.getMaxEdge(regSet);
                if(maxEdge == -1){
                    break;
                }
                regSet.add(maxEdge);
                regStack.push(maxEdge);
            }
            if(regStack.size() + spillCounter >= size){
                break;
            }
            //Step 2:
            int maxEdge = mat.Spill(regSet);
            regSet.add(maxEdge);
            virtualMapping.put(mat.regArrayCopy[maxEdge], Label.create(mat.regArrayCopy[maxEdge].toString()));
            spillCounter++;

            System.out.println("Spilt");
        }

        Map<Integer, Register> chosenRegisters = new HashMap<>();
        HashMap<Register, Register> registerMap = new HashMap<>();
        // note t0 and t1 and t2 is not included so it can be used to load and store virtual variables.
        HashSet<Register> archSet = new HashSet<>(Arrays.asList(
                Register.Arch.t2, Register.Arch.t3,Register.Arch.t4,Register.Arch.t5,Register.Arch.t6,
                Register.Arch.t7,Register.Arch.t8,Register.Arch.t9, Register.Arch.s0, Register.Arch.s0,
                Register.Arch.s1, Register.Arch.s2, Register.Arch.s3, Register.Arch.s4,Register.Arch.s5,
                Register.Arch.s6, Register.Arch.s7));

        //Step3:
        //update size
        size = regStack.size();
        for(int i = 0; i<size; i++){
            Integer regIndex = regStack.pop();
            Register reg = FindOpenRegister(mat, regIndex, chosenRegisters, (HashSet<Register>) archSet.clone());
            chosenRegisters.put(regIndex, reg);
            registerMap.put(mat.regArrayCopy[regIndex], reg);
        }
        return registerMap;

    }

    public Register FindOpenRegister(InterferenceMatrix mat, int regIndex, Map<Integer, Register> chosenRegisters, Set<Register> archSet){
        for(int i = 0;i<mat.size;i++){
            // go through adjacency matrix remove all options that are not avaliable
            if(mat.adjMatrix[regIndex][i] >= 1){
                if(chosenRegisters.get(i) != null){
                    archSet.remove(chosenRegisters.get(i));
                }
            }
        }
        // pick any option out of ArchSet
        Register chosenReg;
        if(archSet.iterator().hasNext()){
            chosenReg = archSet.iterator().next();
        }else{
            System.out.println("Error chosen Reg == null");
            chosenReg = Register.Arch.t1;
        }

        return chosenReg;
    }



}
