package ast;

import java.util.ArrayList;
import java.util.List;

public final class TypecastExpr extends Expr {
    public Type castType;
    public Expr expr;

    public TypecastExpr(Type type, Expr expr){
        this.castType = type;
        this.expr = expr;
    }
    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(castType);
        children.add(expr);
        return children;
    }
}