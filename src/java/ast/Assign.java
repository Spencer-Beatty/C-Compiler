package ast;

import java.util.ArrayList;
import java.util.List;

public final class Assign extends Expr{
    public Expr expr1;
    public Expr expr2;

    public Assign(Expr expr1, Expr expr2){
        this.expr1 = expr1;
        this.expr2 = expr2;
    }
    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(expr1);
        children.add(expr2);
        return children;
    }
}
