package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.Register;


/**
 * Generates code to evaluate an expression and return the result in a register.
 */
public class ExprCodeGen extends CodeGen {

    public ExprCodeGen(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
    }

    public Register visit(Expr e) {
        // TODO: to complete
        return null;
    }
}
