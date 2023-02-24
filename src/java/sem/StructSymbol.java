package sem;

import ast.StructTypeDecl;

public class StructSymbol extends Symbol{
    public StructTypeDecl sd;
    public StructSymbol(StructTypeDecl sd){
        super(sd.structType.name);
        this.sd = sd;
    }
}
