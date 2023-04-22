package ast;
import gen.asm.Label;

public abstract sealed class Decl implements ASTNode
        permits FunDecl, StructTypeDecl, VarDecl, ClassDecl {

    public Type type;
    public String name;
    public Label label;
    public int size;
    public int fpOffset;
}
