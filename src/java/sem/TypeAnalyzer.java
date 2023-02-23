package sem;

import ast.*;

public class TypeAnalyzer extends BaseSemanticAnalyzer {

	public Type visit(ASTNode node) {
		// define struct pass later
		return switch(node) {
			case null -> {
				throw new IllegalStateException("Unexpected null value");
			}

			case Block b -> {
				for (ASTNode c : b.children())
					visit(c);
				yield BaseType.NONE;
			}



			case Program p -> {
				for (ASTNode c : p.children()){
					visit(c);
				}
				yield BaseType.NONE;
			}

			//------------------
			// Declarations
			//------------------
/*
			case (VarDecl vd) -> {
				if(vd.type == BaseType.VOID)
					error("variable declaration " + vd.name + " has type void");
				yield BaseType.NONE;
			}

			case FunDecl fd -> {

				yield BaseType.NONE;
			}

			//------------------
			// Expressions
			//------------------
			case (IntLiteral i) -> {

			}
			case (ChrLiteral i) -> {

			}
			case (StrLiteral i) -> {

			}
			case (VarExpr v) -> {
				v.type = v.vd.type;
				yield v.vd.type;
			}
			case (FunCallExpr fc) -> {

			}
			case (BinOp binOp) -> {
				// Note: two types of binops,  EQ and NE are in there own bin
			}
			case (ArrayAccessExpr a) -> {

			}
			case (FieldAccessExpr f) -> {

			}
			case (ValueAtExpr v) -> {

			}
			case (AddressOfExpr a) -> {

			}
			case (SizeOfExpr s) -> {

			}
			case (TypecastExpr t) -> {
				//char to int ( Basetype to baseType)

				//array to pointer

				//pointer to pointer
			}
			case (Assign a) -> {

			}
			//----------------
			// Statements
			//----------------
			case(While w) -> {

			}
			case (If a) -> {
				// else

				// no else

			}
			case(Return r) -> {
				// return nothing or else?
			}
			//----------------
			// Struct Decl
			//----------------
			case (StructTypeDecl std) -> {
				// special case
				// to complete
				yield BaseType.UNKNOWN; // to change
			}

			case (Type t) -> {
				yield t;
			}

*/
			default -> {
				yield null;
			}

		};

	}


}
