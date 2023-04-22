package gen;

import ast.*;
import gen.asm.AssemblyProgram;

public abstract class CodeGen {
    protected AssemblyProgram asmProg;

    public int getSize(Type type){
        // returns padded size of variable.
        int size = 0;
        switch (type){
            case null -> throw new IllegalArgumentException("getSize type is of null argument");

            case BaseType baseType -> {
                if(baseType == BaseType.INT){
                    size = 4;
                }else if(baseType == BaseType.CHAR){
                    // size maybe should be 4 to account for padding
                    // but only access 1 bit. lb
                    size = 4;
                }else if(baseType == BaseType.VOID){
                    size = 0;
                }else{
                    throw new IllegalArgumentException("type: " + baseType + " ,does not have size");
                }
            }
            case ArrayType arrayType -> {
                int typeSize = getSize(arrayType.type);
                size = typeSize * arrayType.length;
                size = size + (size % 4);
            }
            case StructType structType -> {
                for(VarDecl field : structType.fields){
                    size += getSize(field.type);
                }
            }
            case PointerType pointerType -> {
                size = 4;
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
        return size;


    }
    public int IsLocal(Expr e){
        // Outputs 1 if declared in the data section
        // Outputs -1 if declared on the stack
        switch (e){
            case VarExpr ve -> {
                if(ve.vd.isStaticAllocated()){
                    return 1;
                }else{
                    return -1;
                }
            }
            case ArrayAccessExpr ae ->{
                IsLocal(ae);
            }
            case default ->{}
        }
        return -1;
    }
}
