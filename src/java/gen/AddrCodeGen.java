package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.Register;

/**
 * Generates code to calculate the address of an expression and return the result in a register.
 */
public class AddrCodeGen extends CodeGen {

    public AddrCodeGen(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
    }

    public Register visit(Expr e) {
        // TODO: to complete
        return null;
    }

}
