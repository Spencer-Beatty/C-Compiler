package ast;

import java.util.ArrayList;
import java.util.List;

public final class BinOp extends Expr{
    public Expr lhs;
    public Op op;
    public Expr rhs;

    public BinOp(Expr lhs, Op op, Expr rhs){
        this.lhs = lhs;
        this.op = op;
        this.rhs = rhs;
    }

    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(lhs);
        children.add(rhs);
        return children;
    }
}
