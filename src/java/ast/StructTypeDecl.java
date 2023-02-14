package ast;

import java.util.ArrayList;
import java.util.List;

public final class StructTypeDecl extends Decl {

    // to be completed
    StructType structType;
    List<VarDecl> vds;

    public StructTypeDecl(StructType structType, List<VarDecl> vds){
        this.structType = structType;
        this.vds = vds;
    }

    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(structType);
        children.addAll(vds);

        return children; // To change!
    }

}
