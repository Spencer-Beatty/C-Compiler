package ast;

import java.util.ArrayList;
import java.util.List;

public final class SizeOfExpr extends Expr{
    public Type type;

    public SizeOfExpr(Type type){
        this.type = type;
    }
    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(type);
        return children;
    }
}
