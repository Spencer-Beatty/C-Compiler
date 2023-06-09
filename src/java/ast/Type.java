package ast;

public sealed interface Type extends ASTNode
        permits BaseType, StructType, PointerType, ArrayType, ClassType {
}