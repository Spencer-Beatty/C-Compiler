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

            }
            case PointerType pointerType -> {

            }
        }
        return size;


    }
}
