package ast;

import java.util.ArrayList;
import java.util.List;

public final class ClassFunCallExpr extends Expr {

    public Expr expr;
    public FunCallExpr funCallExpr;
    public ClassFunCallExpr(Expr e, FunCallExpr fe){
        this.expr = e;
        this.funCallExpr = fe;
    }
    public List<ASTNode> children() {
        ArrayList<ASTNode> children = new ArrayList<>();
        children.add(funCallExpr);
        return children;
    }
}
