package ast;

import java.util.List;

public final class ClassType implements Type {
    //ClassType	::= String
    String className;
    public ClassType(String pClassName){
        this.className = pClassName;
    }

    @Override
    public List<ASTNode> children() {
        return null;
    }
}
