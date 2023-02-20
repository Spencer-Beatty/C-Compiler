package ast;

import java.util.ArrayList;
import java.util.List;

public final class ExprStmt extends Stmt{
    public Expr expr;

    public ExprStmt(Expr expr){
        this.expr = expr;
    }


    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(expr);
        return children;
    }
}
