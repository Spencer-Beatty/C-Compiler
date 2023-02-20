package ast;

import java.util.ArrayList;
import java.util.List;

public final class ArrayType implements Type {
    Type type;
    int length;

    public ArrayType(Type type, int length){
        this.type = type;
        this.length = length;
    }
    @Override
    public List<ASTNode> children() {
        return new ArrayList<>();
    }
}
