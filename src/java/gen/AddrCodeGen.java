package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.OpCode;
import gen.asm.Register;

/**
 * Generates code to calculate the address of an expression and return the result in a register.
 */
public class AddrCodeGen extends CodeGen {

    public AddrCodeGen(AssemblyProgram asmProg) {
        this.asmProg = asmProg;
    }

    public Register visit(Expr e) {
        AssemblyProgram.Section text = asmProg.getCurrentSection();
        return switch (e){
            case null -> throw new IllegalArgumentException("Expression " + e.toString() + " in AddrCodeGen is null");
            case BinOp bo ->{
                System.out.println("error binop not a valid addr");
                yield null;
            }
            case IntLiteral intLiteral ->{
                System.out.println("error int_literal not a valid addr");
                yield null;
            }
            case VarExpr ve -> {
                Register resReg = Register.Virtual.create();
                if(ve.vd.isStaticAllocated()){
                    text.emit(OpCode.LA, resReg, ve.vd.label);
                }else{
                    text.emit(OpCode.ADDI, resReg, Register.Arch.fp, ve.vd.fpOffset);
                }
                yield resReg;
            }
            case ChrLiteral chrLiteral -> null;
            case Assign assign -> null;
            case FieldAccessExpr fieldAccessExpr -> null;
            case StrLiteral strLiteral -> null;
            case FunCallExpr funCallExpr -> null;
            case SizeOfExpr sizeOfExpr -> null;
            case AddressOfExpr addressOfExpr -> null;
            case ValueAtExpr valueAtExpr -> null;
            case ArrayAccessExpr arrayAccessExpr -> null;
            case TypecastExpr typecastExpr -> null;

        };


    }

}
