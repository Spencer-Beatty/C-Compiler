package ast;

import java.util.ArrayList;
import java.util.List;

public final class If extends Stmt {
    public Expr expr;
    public Stmt stmt1;
    public Stmt stmt2;

    public If(Expr expr,Stmt stmt1,Stmt stmt2){
        this.expr = expr;
        this.stmt1 = stmt1;
        this.stmt2 = stmt2;
    }


    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(expr);
        children.add(stmt1);
        if(stmt2!=null){
            children.add(stmt2);
        }
        return children;
    }
}
