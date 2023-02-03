package ast;

import java.util.ArrayList;
import java.util.List;

public final class Program implements ASTNode {

    public final List<StructTypeDecl> structTypeDecls;
    public final List<VarDecl> varDecls;
    public final List<FunDecl> funDecls;

    public Program(List<StructTypeDecl> structTypeDecls, List<VarDecl> varDecls, List<FunDecl> funDecls) {
        this.structTypeDecls = structTypeDecls;
	    this.varDecls = varDecls;
	    this.funDecls = funDecls;
    }

    public List<ASTNode> children() {
        List children = new ArrayList<ASTNode>();
        children.addAll(structTypeDecls);
        children.addAll(varDecls);
        children.addAll(funDecls);
        return children;
    }

}
