package ast;

import java.util.ArrayList;
import java.util.List;

public final class ArrayAccessExpr extends Expr{
    public Expr name; // identifier a[10] -> expr[expr]
    public Expr index;

    public ArrayAccessExpr(Expr name, Expr index){
        this.name = name;
        this.index = index;
    }
    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(name);
        children.add(index);
        return children;
    }
}
