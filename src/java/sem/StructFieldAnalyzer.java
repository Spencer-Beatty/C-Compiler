package sem;

import ast.*;

public class StructFieldAnalyzer extends BaseSemanticAnalyzer{
    Scope global;
    public void visit(ASTNode node) {
        switch (node) {
            case null -> {
                throw new IllegalStateException("Unexpected null value");
            }
            case StructTypeDecl std->{
                if(global.lookup(std.structType.name) != null){
                    //struct already defined
                    error("unique struct defined twice");
                }
                else{
                    for(VarDecl vd : std.fields){
                        switch (vd.type){
                            case null ->{}
                            case StructType st -> {
                                visit(st);
                            }case ArrayType at -> {
                                visit(at.type);
                            }
                            default -> {}
                        }
                    }
                    std.structType.fields = std.fields;
                    global.put(new VarSymbol(new VarDecl(std.structType,std.structType.name)));
                }
            }
            case VarExpr ve ->{
                switch (ve.type) {
                    case null -> {}
                    case StructType st->{

                    }
                    default -> {}
                }
            }
            case StructType st -> {
                Symbol sym = global.lookup(st.name);
                switch (sym){
                    case null -> {error("");}
                    case VarSymbol vs -> {
                        switch (vs.vd.type){
                            case null -> {}
                            case StructType std ->{
                                st.fields = std.fields;
                            }
                            default -> {}
                        }
                    }
                    default -> {}
                }
            }
            case ArrayType at -> {visit(at.type);}
            case Program p ->{
                global = new Scope();
                for(ASTNode b : p.children()){
                    visit(b);
                }
            }
            case ASTNode a -> {
                for(ASTNode b : a.children()){
                    visit(b);
                }
            }
        }
    }
}
