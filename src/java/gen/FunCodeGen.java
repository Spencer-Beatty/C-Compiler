package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.Label;
import gen.asm.OpCode;
import gen.asm.Register;
import regalloc.NaiveRegAlloc;

/**
 * A visitor that produces code for a single function declaration
 */
public class FunCodeGen extends CodeGen {


    public FunCodeGen(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
    }

    void visit(FunDecl fd) {
        // Each function should be produced in its own section.
        // This is necessary for the register allocator.
        asmProg.newSection(AssemblyProgram.Section.Type.TEXT);
        AssemblyProgram.Section text = asmProg.getCurrentSection();

        // TODO: to complete
        Label label = Label.get(fd.name);
        fd.label = label;
        text.emit(label);
        // 1) emit the prolog
        /*
        prologue:
            • push frame pointer onto the stack
            • initialise the frame pointer
            • reserve space on the stack for local
                variables
            • save all the saved registers onto the
                stack
         */

        // push fp
        text.emit(OpCode.SW, Register.Arch.fp, Register.Arch.sp, 0);
        // fp = $sp
        text.emit(OpCode.ADD, Register.Arch.fp, Register.Arch.zero, Register.Arch.sp);
        // move stack pointer down past frame pointer
        text.emit(OpCode.ADDI, Register.Arch.sp, Register.Arch.sp, -4);
        // move stack pointer down past local variables
        text.emit(OpCode.ADDI, Register.Arch.sp, Register.Arch.sp, fd.declSize);
        // push registers
        text.emit(OpCode.PUSH_REGISTERS);

        // 2) emit the body of the function
        StmtCodeGen scd = new StmtCodeGen(asmProg);
        scd.visit(fd.block);

        // 3) emit the epilogue
        /*
        • epilogue:
        • restore saved registers from the stack
        • restore the stack pointer
        • restore the frame pointer from the stack
         */


        text.emit(fd.endLabel);
        // pop registers
        text.emit(OpCode.POP_REGISTERS);
        // restore stack pointer
        //text.emit(OpCode.ADDI, Register.Arch.sp, Register.Arch.sp, fd.declSize);
        text.emit(OpCode.ADD, Register.Arch.sp, Register.Arch.zero, Register.Arch.fp);
        // restore frame pointer from the stack
        text.emit(OpCode.LW, Register.Arch.fp, Register.Arch.sp, 0);

        if(!fd.name.equals("main")){
            asmProg.getCurrentSection().emit(OpCode.JR, Register.Arch.ra);
        }

    }



}
