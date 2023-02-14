package ast;

import java.util.ArrayList;
import java.util.List;

public final class StructType implements Type {
    String structType;
    public StructType(String structType){
        this.structType = structType;
    }
    @Override
    public List<ASTNode> children() {
        return new ArrayList<ASTNode>();
    }
}
