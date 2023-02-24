package sem;

import ast.FunDecl;

public class FunSymbol extends Symbol{
    public FunDecl fd;
    public FunSymbol(FunDecl fd){
        super(fd.name);
        this.fd = fd;
    }
}
