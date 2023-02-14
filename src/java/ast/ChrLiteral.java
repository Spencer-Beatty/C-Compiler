package ast;

import java.util.ArrayList;
import java.util.List;

public final class ChrLiteral extends Expr{
    public char chrLiteral;

    public ChrLiteral(char chrLiteral){
        this.chrLiteral = chrLiteral;
    }
    public List<ASTNode> children() {
        return new ArrayList<ASTNode>();
    }
}
