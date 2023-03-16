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
                if(aReturn.expr != null){
                    asmProg.getCurrentSection().emit("Evaluating address of return value");
                    Register regValue = (new ExprCodeGen(asmProg)).visit(aReturn.expr);
                    // push value into a
                    asmProg.getCurrentSection().emit("Transfering Value into stack");
                    //for(int i = aReturn.fd.returnValFpOffset; i>4; i++){
                    asmProg.getCurrentSection().emit(OpCode.SW, regValue, Register.Arch.sp, aReturn.fd.returnValFpOffset);
                    //}
                }
                asmProg.getCurrentSection().emit(OpCode.JR, Register.Arch.ra);
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
                AssemblyProgram.Section text = asmProg.getCurrentSection();
                text.emit("Start of While loop");
                ExprCodeGen ecg = new ExprCodeGen(asmProg);
                Register whileCondition = ecg.visit(aWhile.expr);
                Register timeOut = Register.Virtual.create();
                Label timeoutLabel = Label.create("Timeout");
                Label bodyLabel = Label.create("Body");
                Label endLabel = Label.create("End_Label");
                text.emit("Initilize timeout register: for 50001 iterations");
                text.emit(OpCode.ADDI, timeOut, Register.Arch.zero, 50001);

                // if while condition false skip body
                text.emit(OpCode.BEQ, whileCondition, Register.Arch.zero, endLabel);
                text.emit(OpCode.J, bodyLabel);
                text.emit("Start of Timout Label");
                text.emit(timeoutLabel);
                text.emit(OpCode.LI, Register.Arch.a0, 124);
                text.emit(OpCode.LI, Register.Arch.v0, 17);
                text.emit(OpCode.SYSCALL);
                text.emit("Start of Body Label");
                text.emit(bodyLabel);
                text.emit("Entering Body of While Loop");
                visit(aWhile.stmt);
                // check if condition is still true
                // update the expression
                text.emit("Add to timeout register");
                text.emit(OpCode.ADDI, timeOut, timeOut, -1);
                text.emit("Evaluate timeout: current condition > 50 000 loop iterations");
                text.emit(OpCode.BLEZ, timeOut, timeoutLabel);
                text.emit("Reevaluating while condition");
                whileCondition = ecg.visit(aWhile.expr);
                text.emit(OpCode.BNE, whileCondition, Register.Arch.zero, bodyLabel);
                text.emit(endLabel);
            }
        }
    }
}
