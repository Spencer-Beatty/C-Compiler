package ast;

import java.util.ArrayList;
import java.util.List;

public final class ClassDecl extends Decl {
    //ClassDecl ::= (ClassType ClassType | ClassType) (VarDecl)* (FunDecl)*
    // Part V - First ClassType is for newly-declared class and second one is dedicated to optional parent name
    //ClassType thisClass
    ClassType parent; // optional
    ArrayList<VarDecl> varDecls;
    ArrayList<FunDecl> funDecls;

    public ClassDecl(ClassType thisClassType, ClassType parentClassType, ArrayList<VarDecl> pVarDecls, ArrayList<FunDecl> pFunDecls){
        this.type = thisClassType;
        if(parentClassType != null){
            this.parent = parentClassType;
        }
        this.varDecls = pVarDecls;
        this.funDecls = pFunDecls;
    }


    @Override
    public List<ASTNode> children() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.addAll(varDecls);
        children.addAll(funDecls);
        return children;
    }
}
