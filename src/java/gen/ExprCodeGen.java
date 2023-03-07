package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.Label;
import gen.asm.OpCode;
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
        AssemblyProgram.Section text = asmProg.getCurrentSection();
        switch (e) {
            case null -> {
                throw new IllegalArgumentException("Expression is null");
            }

            case BinOp binOp -> {
                Register lhs = visit(binOp.lhs);
                Register rhs = visit(binOp.rhs);
                Register res = Register.Virtual.create();
                switch (binOp.op){
                    case null -> {}
                    case ADD -> {text.emit(OpCode.ADD, res, lhs, rhs);}
                    case SUB -> {text.emit(OpCode.SUB, res, lhs, rhs);}

                    case EQ -> {
                        Register one = Register.Virtual.create();
                        text.emit(OpCode.LI, one, 1);
                        text.emit(OpCode.XOR , res, lhs, rhs);
                        text.emit(OpCode.SLTU, res, res, one);
                    }
                    case NE -> {
                        text.emit(OpCode.XOR , res, lhs, rhs);
                        text.emit(OpCode.SLTU, res, Register.Arch.zero, res);
                    }

                    case GE -> {
                        text.emit(OpCode.SLT, res, lhs, rhs);
                        text.emit(OpCode.XORI, res, res, 1);
                    }
                    case LE -> {
                        text.emit(OpCode.SLT, res, rhs, lhs);
                        text.emit(OpCode.XORI, res, res, 1);
                    }

                    case GT -> {text.emit(OpCode.SLT, res, rhs, lhs);}
                    case LT -> {text.emit(OpCode.SLT, res, lhs, rhs);}

                    case OR -> {
                        // check first condition then second if first is false
                        // if lhs != 0 then must be true
                        Label trueLabel = Label.create("TRUE");
                        Label falseLabel = Label.create("FALSE");
                        text.emit(OpCode.BNE, lhs, Register.Arch.zero, trueLabel);
                        text.emit(OpCode.BNE, rhs, Register.Arch.zero, trueLabel);
                        text.emit(OpCode.ADDI, res, Register.Arch.zero, 0 );
                        text.emit(OpCode.J, falseLabel);
                        // label is emitted later on here
                        text.emit(trueLabel);
                        // place 1 in the res register indicating true
                        text.emit(OpCode.ADDI, res, Register.Arch.zero, 1);
                        text.emit(falseLabel);
                        // place 0 in the res register indicating or was false


                    }
                    case AND -> {
                        // if first condition is false don't evaluate second
                        Label trueLabel = Label.create("TRUE");
                        Label falseLabel = Label.create("FALSE");
                        text.emit(OpCode.BEQ, lhs, Register.Arch.zero, falseLabel);
                        text.emit(OpCode.BEQ, rhs, Register.Arch.zero, falseLabel);
                        // neither jumped to false label so and is true
                        text.emit(OpCode.ADDI, res, Register.Arch.zero, 1 );
                        text.emit(OpCode.J, trueLabel );

                        text.emit(falseLabel);
                        text.emit(OpCode.ADDI, res, Register.Arch.zero, 0);
                        text.emit(trueLabel);



                    }

                    case DIV -> {
                        text.emit(OpCode.DIV, lhs, rhs);
                        text.emit(OpCode.MFLO, res);
                    }
                    case MOD -> {
                        text.emit(OpCode.DIV, lhs, rhs);
                        text.emit(OpCode.MFHI, res);
                    }
                    case MUL -> {text.emit(OpCode.MUL, res, lhs, rhs);}
                }
                return res;

            }
            case Assign assign -> {
                // come back to later to deal with store words
                Register lhs = visit(assign.expr1);
                Register rhs = visit(assign.expr2);
                text.emit(OpCode.ADD, lhs, Register.Arch.zero, rhs);
                return lhs;
            }
            case VarExpr varExpr -> {
            }
            case SizeOfExpr sizeOfExpr -> {
            }
            case IntLiteral intLiteral -> {
                Register v1 = Register.Virtual.create();
                text.emit(OpCode.LI, v1, intLiteral.intLiteral);
                return v1;

            }
            case TypecastExpr typecastExpr -> {
            }
            case ArrayAccessExpr arrayAccessExpr -> {
            }
            case FunCallExpr funCallExpr -> {
                // check if funCall is predefined ( print_i )
                switch(funCallExpr.name){
                    case "print_i" -> {
                        // first argument contains integer expression argument
                        Register res = visit(funCallExpr.exprs.get(0));
                        text.emit(OpCode.ADD, Register.Arch.a0, Register.Arch.zero , res);
                        text.emit(OpCode.LI, Register.Arch.v0, 1);
                        text.emit(OpCode.SYSCALL);
                    }
                }
            }
            case FieldAccessExpr fieldAccessExpr -> {
            }
            case AddressOfExpr addressOfExpr -> {
            }
            case ChrLiteral chrLiteral -> {
            }
            case StrLiteral strLiteral -> {
            }
            case ValueAtExpr valueAtExpr -> {
            }
        }
        return null;
    }
}
