package ast;

import java.util.ArrayList;
import java.util.List;

public final class FunCallExpr extends Expr{
    public String name;
    public List<Expr> exprs;

    public FunCallExpr(String name, List<Expr> exprs){
        this.name = name;
        this.exprs = exprs;
    }
    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.addAll(exprs);
        return children;
    }
}
