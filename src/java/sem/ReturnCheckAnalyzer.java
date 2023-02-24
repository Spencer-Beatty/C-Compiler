package sem;

import ast.ASTNode;
import ast.*;

public class ReturnCheckAnalyzer extends BaseSemanticAnalyzer{
    public Scope outer;
    private String ret = "return";
    public void visit(ASTNode node) {
        // define struct pass later
        switch (node) {
            case null -> {error("null value caught");}
            case FunDecl fd -> {
                Scope temp = outer;
                outer = new Scope(temp);
                visit(fd.block);

                if(fd.type != BaseType.VOID && outer.lookup(ret) == null){
                    error("function declaration does not contain return statement");
                }
                outer = temp;
            }
            case Return rt ->{
                if(outer.lookup(ret) == null) {
                    outer.put(new VarSymbol(new VarDecl(BaseType.INT, ret)));
                }

            }
            case ASTNode a ->{
                for(ASTNode b : a.children()){
                    visit(b);
                }
            }

        }
    }
}
