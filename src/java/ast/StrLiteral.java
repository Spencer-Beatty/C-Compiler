package ast;

import java.util.ArrayList;
import java.util.List;

public final class StrLiteral extends Expr{
    public String strLiteral;

    public StrLiteral(String strLiteral){
        this.strLiteral = strLiteral;
    }
    public List<ASTNode> children() {
        return new ArrayList<ASTNode>();
    }
}
