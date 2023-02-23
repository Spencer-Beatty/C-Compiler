package ast;

import java.util.ArrayList;
import java.util.List;

public final class StructTypeDecl extends Decl {

    // to be completed
    public StructType structType;
    public List<VarDecl> fields;

    public StructTypeDecl(StructType structType, List<VarDecl> fields){
        this.structType = structType;
        this.fields = fields;
    }

    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(structType);
        children.addAll(fields);
        return children;
    }

}
