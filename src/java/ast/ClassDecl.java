package ast;

import java.util.ArrayList;
import java.util.List;

public final class ClassDecl extends Decl {
    //ClassDecl ::= (ClassType ClassType | ClassType) (VarDecl)* (FunDecl)*
    // Part V - First ClassType is for newly-declared class and second one is dedicated to optional parent name
    //ClassType thisClass

    public ClassType classType;
    public ClassType parent; // optional
    public ArrayList<VarDecl> varDecls;
    public ArrayList<FunDecl> funDecls;

    public ClassDecl(ClassType thisClassType, ClassType parentClass, ArrayList<VarDecl> pVarDecls, ArrayList<FunDecl> pFunDecls){

        this.name = thisClassType.className;
        this.classType = thisClassType;
        this.parent = parentClass;
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

    public ArrayList<VarDecl> getAllVarDecl(){
        ArrayList<VarDecl> allVarDecl = new ArrayList<VarDecl>();
        allVarDecl.addAll(this.varDecls);
        if(parent!=null){
            allVarDecl.addAll(parent.classDecl.getAllVarDecl());
        }

        return allVarDecl;
    }

    public ArrayList<FunDecl> getAllFunDecl(){
        ArrayList<FunDecl> allFunDecl = new ArrayList<FunDecl>();
        ArrayList<String> allFunDeclNames = new ArrayList<String>();
        allFunDecl.addAll(this.funDecls);
        for(FunDecl fd : this.funDecls){
            allFunDeclNames.add(fd.name);
        }
        if(parent!=null){
            for(FunDecl fd : parent.classDecl.getAllFunDecl()){
                if(!allFunDeclNames.contains(fd)){
                    allFunDecl.add(fd);
                    allFunDeclNames.add(fd.name);
                }
            }

        }

        return allFunDecl;
    }
}
