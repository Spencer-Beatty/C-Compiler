package ast;

import java.util.ArrayList;
import java.util.List;

public final class TypeCastExpr extends Expr {
    public Type type;
    public Expr expr;

    public TypeCastExpr(Type type, Expr expr){
        this.type = type;
        this.expr = expr;
    }
    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(type);
        children.add(expr);
        return children;
    }
}
