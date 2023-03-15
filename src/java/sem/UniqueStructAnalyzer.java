package sem;

import ast.*;

public class UniqueStructAnalyzer extends BaseSemanticAnalyzer{
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
                    global.put(new VarSymbol(new VarDecl(std.structType,std.structType.name)));
                }
            }
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
