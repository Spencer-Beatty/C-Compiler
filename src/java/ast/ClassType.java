package ast;

import java.util.ArrayList;
import java.util.List;

public final class ClassType implements Type {
    //ClassType	::= String
    public String className;
    public ClassDecl classDecl;
    public boolean intantiated = false;
    public ClassType(String pClassName){
        this.className = pClassName;
    }

    @Override
    public List<ASTNode> children() {

        return new ArrayList<ASTNode>();
    }
}
