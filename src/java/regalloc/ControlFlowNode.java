package regalloc;

import ast.VarDecl;
import ast.VarExpr;
import gen.asm.AssemblyProgram;
import gen.asm.Register;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ControlFlowNode {
    public boolean displayed = false;
    public String branchLabelName;
    public int totalLineCounter;
    public int lineNumber;
    public String name;
    public String stringNode;
    // predecessors
    private List<ControlFlowNode> pred;
    // descendents
    private List<ControlFlowNode> desc;

    private List<Register> useVars; // point to original definition of decl
    private List<Register> defVars; // point to use of decl

    public ControlFlowNode(String pName, List<Register> pUseVars, List<Register> pDecVars, String pBranchLabelName){
        this.name = pName;
        this.useVars = pUseVars;
        this.defVars = pDecVars;
        this.pred = new ArrayList<ControlFlowNode>();
        this.desc = new ArrayList<ControlFlowNode>();
        this.branchLabelName = pBranchLabelName;
    }

    public List<Register> Use(){
        return useVars;
    }

    public List<Register> Def() {
        return defVars;
    }

    public void AddDescendent(ControlFlowNode cf){
            desc.add(cf);
    }

    public void AddPredecessor(ControlFlowNode cf){
        if(cf!=null)pred.add(cf);
    }

    public void RemovePredecessor(ControlFlowNode cf){
        pred.remove(cf);
    }

    public void DisplayInstruction(PrintWriter pw){
        for(ControlFlowNode cf : desc){
            pw.write(name + "--" + cf.name + "\n");
            if(cf.lineNumber > lineNumber){
                cf.DisplayInstruction(pw);
            }
        }
    }

    public void DisplayLabel(PrintWriter pw){
        pw.write("node_" + totalLineCounter + " [label=\""+stringNode+"\"]\n" );
        for(ControlFlowNode cf : desc){
            if(cf.lineNumber > lineNumber){
                cf.DisplayLabel(pw);
            }
        }
    }
    public void Display(PrintWriter pw, String line){
        if(! displayed){
            displayed = true;
            for(ControlFlowNode cf : desc){
                pw.write("node_"+totalLineCounter + line + "node_"+cf.totalLineCounter+"\n");
                // if lineNumber > cf.linenumber then cf has already been visited
                if(cf.lineNumber > lineNumber){
                    cf.Display(pw, line);
                }
            }
        }
    }

    public void SetDisplay(){
        displayed = false;
        for(ControlFlowNode cf : desc){
            cf.SetDisplay();
        }
    }

    public void DisplayPred(PrintWriter pw, String line){
        for(ControlFlowNode cf : desc){
            if(cf.lineNumber > lineNumber){
                cf.DisplayPred(pw, line);
            }
        }
        for(ControlFlowNode cf : pred){
            pw.write("node_"+totalLineCounter + line + "node_"+cf.totalLineCounter+"\n");
        }
    }
    public ControlFlowNode FindLabelNode(List<ControlFlowNode> labelNodes, String branchLabelName){
        for(ControlFlowNode cf : labelNodes){
            if(cf.name.equals(branchLabelName)){
                return cf;
            }
        }
        throw new IllegalArgumentException("Label + " + branchLabelName + " not found in label list");
    }
    public void ConnectControlFlowGraph(List<ControlFlowNode> labelNodes) {
        //at this point there should only be one descendent or no descendents
        ControlFlowNode current = this;
        ControlFlowNode next = null;
        while(! current.desc.isEmpty()){
            next = current.desc.get(0);
            CleanNode(current, labelNodes);
            current = next;
        }
        CleanNode(current, labelNodes);
    }

    private void CleanNode(ControlFlowNode current, List<ControlFlowNode> labelNodes){
        if(current.branchLabelName != null){
            if(current.name.equals("$j")){
                for(ControlFlowNode cf : current.desc){
                    cf.RemovePredecessor(current);
                }
                current.desc = new ArrayList<ControlFlowNode>();
            }
            ControlFlowNode labelNode = FindLabelNode(labelNodes, current.branchLabelName);
            current.AddDescendent(labelNode);
            labelNode.AddPredecessor(current);
        }
    }

}

