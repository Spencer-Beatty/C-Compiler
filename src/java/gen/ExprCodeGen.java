package gen;

import ast.*;
import gen.asm.*;


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
                text.emit("Binary Operation");
                switch (binOp.op){
                    case null -> {}
                    case ADD -> {text.emit("Add");text.emit(OpCode.ADD, res, lhs, rhs);}
                    case SUB -> {text.emit("Sub");text.emit(OpCode.SUB, res, lhs, rhs);}

                    case EQ -> {
                        text.emit("Equality");
                        Register one = Register.Virtual.create();
                        text.emit(OpCode.LI, one, 1);
                        text.emit(OpCode.XOR , res, lhs, rhs);
                        text.emit(OpCode.SLTU, res, res, one);
                    }
                    case NE -> {
                        text.emit("Inequality");
                        text.emit(OpCode.XOR , res, lhs, rhs);
                        text.emit(OpCode.SLTU, res, Register.Arch.zero, res);
                    }

                    case GE -> {
                        text.emit("Greater than or Equal");
                        text.emit(OpCode.SLT, res, lhs, rhs);
                        text.emit(OpCode.XORI, res, res, 1);
                    }
                    case LE -> {
                        text.emit("Less than or Equal");
                        text.emit(OpCode.SLT, res, rhs, lhs);
                        text.emit(OpCode.XORI, res, res, 1);
                    }

                    case GT -> {text.emit("Greater than"); text.emit(OpCode.SLT, res, rhs, lhs);}
                    case LT -> {text.emit("Less than");text.emit(OpCode.SLT, res, lhs, rhs);}

                    case OR -> {
                        text.emit("Or");
                        // check first condition then second if first is false
                        // if lhs != 0 then must be true
                        Label trueLabel = Label.create("TRUE");
                        Label falseLabel = Label.create("FALSE");
                        text.emit(OpCode.BNE, lhs, Register.Arch.zero, trueLabel);
                        text.emit(OpCode.BNE, rhs, Register.Arch.zero, trueLabel);
                        text.emit("Set result to false, because both lhs and rhs are false");
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
                        text.emit("And");
                        // if first condition is false don't evaluate second
                        Label trueLabel = Label.create("TRUE");
                        Label falseLabel = Label.create("FALSE");
                        text.emit(OpCode.BEQ, lhs, Register.Arch.zero, falseLabel);
                        text.emit(OpCode.BEQ, rhs, Register.Arch.zero, falseLabel);
                        // neither jumped to false label so and is true
                        text.emit("Set result to true, because both lhs and rhs are true");
                        text.emit(OpCode.ADDI, res, Register.Arch.zero, 1 );
                        text.emit(OpCode.J, trueLabel );

                        text.emit(falseLabel);
                        text.emit(OpCode.ADDI, res, Register.Arch.zero, 0);
                        text.emit(trueLabel);



                    }

                    case DIV -> {
                        text.emit("Div");
                        text.emit(OpCode.DIV, lhs, rhs);
                        text.emit(OpCode.MFLO, res);
                    }
                    case MOD -> {
                        text.emit("Mod");
                        text.emit(OpCode.DIV, lhs, rhs);
                        text.emit(OpCode.MFHI, res);
                    }
                    case MUL -> {text.emit("Mul");text.emit(OpCode.MUL, res, lhs, rhs);}
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
                text.emit("Loading register with int literal");
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
                        text.emit("Function Call print_i");
                        Register res = visit(funCallExpr.exprs.get(0));
                        text.emit("Loading result into a0 and printing");
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
