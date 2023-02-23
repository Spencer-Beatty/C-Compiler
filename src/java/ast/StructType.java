package ast;

import java.util.ArrayList;
import java.util.List;

public final class StructType implements Type {
    public String name;
    public List<VarDecl> fields;
    public StructType(String structType){
        this.name = structType;
    }
    @Override
    public List<ASTNode> children() {
        return new ArrayList<ASTNode>();
    }
}
