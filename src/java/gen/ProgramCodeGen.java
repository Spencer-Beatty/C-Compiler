package gen;

import ast.*;
import gen.asm.AssemblyProgram;

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

        // generate the code for each function
        FunCodeGen fcg = new FunCodeGen(asmProg);
        p.decls.forEach((d) -> {
            switch(d) {
                case FunDecl fd -> fcg.visit(fd);
                default -> {}// nothing to do
            }});
    }





}
