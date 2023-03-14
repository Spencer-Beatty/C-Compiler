package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.Label;
import gen.asm.OpCode;
import gen.asm.Register;

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
        // 1) emit the prolog
        Label label = Label.get(fd.name);
        fd.label = label;
        text.emit(label);
        // stack pointer will be pushed down for params by callee
        // but function should store the frame pointer
        // frame pointer will be at this point 4($sp)
        text.emit(OpCode.SW, Register.Arch.fp, Register.Arch.sp, -4);
        text.emit(OpCode.ADD, Register.Arch.fp, Register.Arch.zero, Register.Arch.sp);

        // 2) emit the body of the function
        StmtCodeGen scd = new StmtCodeGen(asmProg);
        scd.visit(fd.block);

        // 3) emit the epilogue
        text.emit(OpCode.LW, Register.Arch.sp, Register.Arch.fp, -4);

    }



}
