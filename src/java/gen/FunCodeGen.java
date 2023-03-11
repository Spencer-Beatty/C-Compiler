package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.Label;

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


        // TODO: to complete
        // 1) emit the prolog
        Label label = Label.create(fd.name);
        fd.label = label;
        asmProg.getCurrentSection().emit(label);
        // create space for all the params
        // push the frame pointer,
        // set frame pointer to the stack pointer

        // 2) emit the body of the function
        StmtCodeGen scd = new StmtCodeGen(asmProg);
        scd.visit(fd.block);

        // 3) emit the epilogue

    }



}
