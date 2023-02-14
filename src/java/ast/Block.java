package ast;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class Block extends Stmt {

    public List<VarDecl> vds;
    public List<Stmt> stmts;

    public Block(List<VarDecl> vds, List<Stmt> stmts){
        this.vds = vds;
        this.stmts = stmts;
    }

    @Override
    public List<ASTNode> children() {
        List children = new ArrayList<ASTNode>();
        children.addAll(vds);
        children.addAll(stmts);
        return children;
    }


}
