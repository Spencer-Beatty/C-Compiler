package ast;

import java.util.ArrayList;
import java.util.List;

public final class While extends Stmt{
    public Expr expr;
    public Stmt stmt;

    public While(Expr expr, Stmt stmt){
        this.expr = expr;
        this.stmt = stmt;
    }

    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(expr);
        children.add(stmt);
        return children;
    }
}
