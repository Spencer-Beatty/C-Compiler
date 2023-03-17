package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.Label;
import gen.asm.OpCode;
import gen.asm.Register;

/**
 * This visitor should produce a program.
 */
public class ProgramCodeGen extends CodeGen {


    private final AssemblyProgram.Section dataSection ;

    public ProgramCodeGen(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
        this.dataSection = asmProg.newSection(AssemblyProgram.Section.Type.DATA);
    }

    void generate(Program p) {
        // allocate all variables
        MemAllocCodeGen allocator = new MemAllocCodeGen(asmProg);
        allocator.visit(p);


        // idea: make text section here, each function named after themselves
        asmProg.newSection(AssemblyProgram.Section.Type.TEXT);
        asmProg.getCurrentSection().emit(OpCode.J, Label.get("main"));
        // no need to visit other things because memalloc deals with them.
        // generate the code for each function
        p.decls.forEach((d) -> {
            switch(d) {
                case FunDecl fd -> {
                    Label endLabel = Label.create(fd.name + "_EndLabel");
                    fd.endLabel = endLabel;
                    FunCodeGen fcg = new FunCodeGen(asmProg);
                    fcg.visit(fd);


                }
                default -> {}// nothing to do
            }});
    }





}
