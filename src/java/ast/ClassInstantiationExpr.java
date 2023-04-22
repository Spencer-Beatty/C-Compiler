package ast;

import java.util.List;

public final class ClassInstantiationExpr extends Expr {
    ClassType classType;
    public ClassInstantiationExpr(ClassType type){
        this.classType = type;
    }
    @Override
    public List<ASTNode> children() {
        return null;
    }
}
