package ast;

import java.util.ArrayList;
import java.util.List;

public final class Return extends Stmt{
    public Expr expr;

    public Return(Expr expr){
        this.expr = expr;
    }

    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        if(expr != null){
            children.add(expr);
        }
        return children;
    }
}
