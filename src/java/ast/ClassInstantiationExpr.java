package ast;

import java.util.ArrayList;
import java.util.List;

public final class ClassInstantiationExpr extends Expr {
    public ClassType classType;
    public ClassInstantiationExpr(ClassType type){
        this.classType = type;
    }
    @Override
    public List<ASTNode> children() {
        return new ArrayList<ASTNode>();
    }
}
