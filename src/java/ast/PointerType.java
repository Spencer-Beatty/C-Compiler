package ast;

import java.util.ArrayList;
import java.util.List;

public final class PointerType implements Type{
    public Type type;
    public List<String> pointers;

    public PointerType(Type type, ArrayList<String> pointers){
        this.type = type;
        this.pointers = pointers;
    }

    @Override
    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(type);
        return children;
    }
}
