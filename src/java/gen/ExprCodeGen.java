package gen;

import ast.*;
import gen.asm.*;

import java.util.ArrayList;
import java.util.List;


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
                // expr1 is lhs, expr2 is rhs
                Register addrReg = (new AddrCodeGen(asmProg)).visit(assign.expr1);
                Register valReg = visit(assign.expr2);
                text.emit(OpCode.SW, valReg, addrReg, 0);
                return valReg;
            }
            case VarExpr varExpr -> {
                // check whether statically defined or if on the stack
                Register v1 = Register.Virtual.create();
                if(varExpr.vd.isStaticAllocated()){
                    // get label and sw
                    text.emit("Loading address " + varExpr.vd.label.toString() + " into register");
                    text.emit(OpCode.LA, v1, varExpr.vd.label);
                    text.emit("Loading word from address into register");
                    text.emit(OpCode.LW, v1, v1, 0);
                }else{
                    // deal with stack
                    text.emit("Loading word from the stack");
                    text.emit(OpCode.LW,v1,Register.Arch.fp,varExpr.vd.fpOffset);

                }
                return v1;

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

                return visit(typecastExpr.expr);
            }
            case ArrayAccessExpr arrayAccessExpr -> {
            }
            case FunCallExpr funCallExpr -> {
                // check if funCall is predefined ( print_i )
                // otherwise should jump to function call with link
                // pushing parameters to the stack
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
                    case "print_s" -> {
                        // first argument char *
                        text.emit("Function call print_s");
                        // res should be address of label pointing to string or char[]
                        Register res = visit(funCallExpr.exprs.get(0));
                        text.emit(OpCode.ADD, Register.Arch.a0, Register.Arch.zero, res);
                        text.emit(OpCode.LI, Register.Arch.v0, 4);
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
                // should create a label here

                Register res = Register.Virtual.create();
                Label label = Label.create("string");
                asmProg.sections.get(0).emit(label);
                asmProg.sections.get(0).emit(new Directive("asciiz " +"\""+ReplaceEscape(strLiteral.strLiteral)+"\""));
                // pad the space
                System.out.println("Hello World".length());
                System.out.println("Hello World\n".length());
                if(((strLiteral.strLiteral.length()+1) % 4)!= 0) {
                    asmProg.sections.get(0).emit(new Directive("space " + (4-((strLiteral.strLiteral.length()+1) % 4))));
                }




                text.emit(OpCode.LA,res, label);
                return res;
            }
            case ValueAtExpr valueAtExpr -> {
            }
        }
        return null;
    }

    private String ReplaceEscape(String string){
        // function is not very efficient, look into regular expression potentially
        string = string.replace("\n", "\\n");

        string  = string.replace("\t", "\\t");
        string = string.replace("\b", "\\b");

        string = string.replace("\r", "\\r");
        string = string.replace("\f", "\\f");
        string = string.replace("\f", "\\f");
        string = string.replace("\'", "\\'");
        string = string.replace("\"", "\\\"");
        string = string.replace("\0", "\\0");

        return string;

    }

}
