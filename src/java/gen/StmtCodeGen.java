package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.OpCode;
import gen.asm.Register;

import gen.asm.Label;

public class StmtCodeGen extends CodeGen {

    public StmtCodeGen(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
    }

    void visit(Stmt s) {
        switch (s) {
            case Block b -> {
                // no need to do anything with varDecl (memory allocator takes care of them)
                b.stmts.forEach((innerStmt) -> {
                    visit(innerStmt);
                });
            }
            // To complete other cases
            case Return aReturn -> {
                // should pop registers just after return
            }
            case ExprStmt exprStmt -> {
                ExprCodeGen ecg = new ExprCodeGen(asmProg);
                ecg.visit(exprStmt.expr);
            }
            case If anIf -> {
                AssemblyProgram.Section text = asmProg.getCurrentSection();
                text.emit("Start of if statement");
                ExprCodeGen ecg = new ExprCodeGen(asmProg);
                Register ifCondition = ecg.visit(anIf.expr);
                Label falseLabel = Label.create("False_Label");
                Label endLabel = Label.create("End_Label");
                text.emit(OpCode.BEQ, ifCondition, Register.Arch.zero, falseLabel);
                text.emit("Entering Statement of If clause");
                visit(anIf.stmt1);
                text.emit(OpCode.J,endLabel);
                text.emit(falseLabel);
                if(anIf.stmt2 != null){
                    text.emit("Entering Statement of Else clause");
                    visit(anIf.stmt2);
                }
                text.emit(OpCode.J, endLabel);
                text.emit("Exiting If statement");
                text.emit(endLabel);
            }
            case While aWhile -> {
            }
        }
    }
}
