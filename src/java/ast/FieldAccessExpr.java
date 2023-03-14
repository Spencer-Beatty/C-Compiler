package ast;

import java.util.ArrayList;
import java.util.List;


public final class FieldAccessExpr extends Expr {
    public Expr expr; //  expr.field
    public String field;
    public StructType structType;

    public FieldAccessExpr(Expr expr, String field){
        this.expr = expr;
        this.field = field;
    }
    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(expr);
        return children;
    }
}
