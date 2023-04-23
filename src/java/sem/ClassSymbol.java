package sem;

import ast.ClassDecl;

public class ClassSymbol extends Symbol{
    public ClassDecl cd;
    public ClassSymbol(ClassDecl classDecl){
        super(classDecl.name);
        this.cd = classDecl;
    }
}
