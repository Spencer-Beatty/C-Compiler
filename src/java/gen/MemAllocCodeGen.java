package gen;

import ast.*;
import gen.asm.AssemblyProgram;

/* This allocator should deal with all global and local variable declarations. */

public class MemAllocCodeGen extends CodeGen {

    public MemAllocCodeGen(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
    }

    boolean global = true;
    int fpOffset = 0;
    //AssemblyProgram.Section data = asmProg.sections.get(0);

    void visit(ASTNode n) {
        switch(n) {
            case null -> {throw new IllegalArgumentException("MemAlloc encountered null Astnode value");}
            case VarDecl vd ->{
                if(global){
                    // create label for it

                    System.out.println("global");
                }else{
                    System.out.println("local");
                    // put on the stack
                }
            }
            case FunDecl fd ->{
                visit(fd.block);
            }
            case Block bd ->{
                boolean scope = global;
                global = false;
                bd.vds.forEach((innerVarDecl) -> {
                    visit(innerVarDecl);
                });
                bd.stmts.forEach((innerStmt) -> {
                    visit(innerStmt);
                });
                global = scope;
            }
            case ASTNode astNode ->{
                astNode.children().forEach((child)->{
                    visit(child);
                });
            }
        }
    }

}
