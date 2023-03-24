package regalloc;

import ast.VarDecl;
import gen.asm.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ControlFlowGraph {
    private List<ControlFlowNode> functionStartNodes;
    public ControlFlowGraph(){
        functionStartNodes = new ArrayList<ControlFlowNode>();
    }
    public void CreateControlFlowGraph(AssemblyProgram asmProg){
        int totalLineCounter = 0;
        ArrayList<ControlFlowNode> labelNodes = new ArrayList<ControlFlowNode>();
        for(AssemblyProgram.Section section : asmProg.sections){
            if(section.type != AssemblyProgram.Section.Type.TEXT){
                continue;
            }

            if(totalLineCounter == 0){
                // skips j main
                totalLineCounter ++;
                continue;
            }

            int lineNumber = 0;
            ControlFlowNode predNode = null;
            ControlFlowNode startNode = null;
            for(AssemblyItem item : section.items){
                String name = "";
                Boolean labelBool = false;
                String branchLabelName = null;
                ArrayList<Register> defRegs = new ArrayList<Register>();
                ArrayList<Register> useRegs = new ArrayList<Register>();
                switch (item){
                    case Directive directive ->{
                        throw new IllegalArgumentException("Directive in .Text section");
                    }
                    case Comment comment -> {
                        continue;
                    }
                    // for each branch store label in node.label information
                    case Instruction.BinaryBranch binaryBranch-> {
                        branchLabelName = binaryBranch.label.name;
                    }
                    case Instruction.UnaryBranch unaryBranch->{
                        branchLabelName = unaryBranch.label.name;
                    }
                    case Instruction.Jump j->{
                        name = "$j";
                        branchLabelName = j.label.name;
                    }
                    //
                    case Instruction i ->{
                        name = i.opcode.mnemonic;
                        AddIgnoreNull(defRegs, i.def());
                        useRegs.addAll(i.uses());
                    }
                    case Label label -> {
                        // check if label was already made, in case of branch instruction
                        labelBool = true;
                        name = label.name;
                    }
                    default -> System.out.println("Unexpected value: " + item);
                }


                if(lineNumber == 0){

                    startNode = new ControlFlowNode(name, useRegs, defRegs,  branchLabelName);
                    functionStartNodes.add(startNode);
                    predNode = startNode;
                } else{
                    ControlFlowNode nextNode = new ControlFlowNode(name, useRegs, defRegs, branchLabelName);
                    predNode.AddDescendent(nextNode);
                    nextNode.AddPredecessor(predNode);
                    predNode = nextNode;
                }

                predNode.totalLineCounter = totalLineCounter;
                predNode.lineNumber = lineNumber;
                predNode.stringNode = lineNumber + ": " + item;
                if(labelBool){
                    labelNodes.add(predNode);
                }
                //System.out.print(lineNumber + ":");
                //System.out.println(item.toString());
                lineNumber ++;
                totalLineCounter ++;

            }

        }

        for(ControlFlowNode cf : functionStartNodes){
            cf.ConnectControlFlowGraph(labelNodes);
        }
    }
    public void Display(){
        try{
            PrintWriter printWriter = new PrintWriter("tests/graph.gv");
            printWriter.write("digraph "+"Control_Flow_Graph" + " {\n");
            printWriter.write("node [style=filled,color=green]\n");
            printWriter.write("edge [color=red]\n");
            // set features of digraph
            for(ControlFlowNode cf : functionStartNodes){
                if(cf.name == "j"){ continue;}
                printWriter.write("subgraph "+cf.name + " {\n");
                cf.DisplayLabel(printWriter);
                cf.Display(printWriter, "->");
                //DisplayPred shows same graph backwards
                //cf.DisplayPred(printWriter, "->");
                printWriter.write("}\n\n");
            }
            printWriter.write("}\n");
            printWriter.flush();
        }catch (FileNotFoundException e){
            System.out.println("File not found");
        }
    }

    private void AddIgnoreNull(List<Register> list, Register r1){
        if(r1 != null){
            list.add(r1);
        }
    }
}
