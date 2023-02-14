package ast;

import java.util.ArrayList;
import java.util.List;

public final class IntLiteral extends Expr {
    public int intLiteral;

    public IntLiteral(int intLiteral){
        this.intLiteral = intLiteral;
    }
    public List<ASTNode> children() {
        return new ArrayList<ASTNode>();
    }
}
