package gen;

import ast.*;
import gen.asm.AssemblyProgram;
import gen.asm.Label;
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
            case FieldAccessExpr fieldAccessExpr -> {
                text.emit("Start of Field Access Expr in Addr Code Gen");
                // now we need to find out if structs are locally or globally declared
                text.emit("Get address of struct variable");
                // this will get the variable declaration within local scope or global
                // structVar is an address
                Register structVarAddress = visit(fieldAccessExpr.expr);
                text.emit("Get address of Original Struct Declaration");
                Register v1 = Register.Virtual.create();
                text.emit(OpCode.LA, v1, Label.get(fieldAccessExpr.structType.name));
                text.emit("Get offset of struct field");
                Register v2 = Register.Virtual.create();
                Register v3 = Register.Virtual.create();
                // this should always be positive, h
                text.emit(OpCode.LW, v2, v1, GetFieldNumber(fieldAccessExpr.structType,fieldAccessExpr.field)*4);
                // offset of struct field now stored withing v1
                // v1 should be a negative number
                text.emit("Access offset from struct address");
                // add offset to address of struct var
                if(IsLocal(fieldAccessExpr.expr) == 1){
                    text.emit(OpCode.ADD, v3, structVarAddress, v2);
                }else{
                    text.emit(OpCode.SUB, v3, structVarAddress, v2);
                }
                //return address
                yield v3;

            }
            case StrLiteral strLiteral -> null;
            case FunCallExpr funCallExpr -> null;
            case SizeOfExpr sizeOfExpr -> null;
            case AddressOfExpr addressOfExpr -> null;
            case ValueAtExpr valueAtExpr -> null;
            case ArrayAccessExpr arrayAccessExpr -> {
                Register resReg = Register.Virtual.create();
                Register accessReg = visit(arrayAccessExpr.name);
                text.emit("ArrayAccessExpr address generation");
                Register index = (new ExprCodeGen(asmProg)).visit(arrayAccessExpr.index);
                text.emit("Load negative size, to multiply by index number to find start point result");
                text.emit(OpCode.LI, resReg, -getSize(arrayAccessExpr.type));
                text.emit(OpCode.MUL,resReg,resReg, index);
                // subbing from access reg to move to the correct index
                text.emit(OpCode.SUB, accessReg, accessReg, resReg);
                // at this point index will look something like -8 if storing ints and index was 2
                text.emit("Load address of array at index into result register");
                text.emit(OpCode.ADD, resReg,Register.Arch.zero, accessReg);
                yield resReg;
            }
            case TypecastExpr typecastExpr -> null;

            default -> throw new IllegalStateException("Unexpected value: " + e);
        };


    }

    private int GetFieldNumber(StructType st, String name){
        int counter = 0;
        // names are guarenteed by name analyzer to be unique
        for(VarDecl vd : st.fields){
            if(vd.name.equals(name)){
                return counter;
            }
            counter++;
        }
        throw new RuntimeException("Field " + name + " is not found within struct st");
    }

}
