package ast;

import java.util.ArrayList;
import java.util.List;
import gen.asm.Label;

public final class FunDecl extends Decl {
    public final List<VarDecl> params;
    public final Block block;
    public int returnValFpOffset;
    public int callSize;
    public int declSize;
    public Label endLabel;


    public FunDecl(Type type, String name, List<VarDecl> params, Block block) {
	    this.type = type;
	    this.name = name;
	    this.params = params;
	    this.block = block;
    }

    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.addAll(params);
        children.add(block);
        return children;
    }

}
