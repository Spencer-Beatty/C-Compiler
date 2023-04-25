package sem;

import ast.*;

import java.util.ArrayList;

public class TypeAnalyzer extends BaseSemanticAnalyzer {

	private boolean CompareType(Type t1, Type t2){
		switch (t1) {
			case null -> {
				return false;
			}
			case PointerType pt -> {
				switch (t2) {
					case null -> {
						return false;
					}
					case PointerType pt2 -> {
						return CompareType(pt.type,pt2.type);
					}
					case default ->{
						return false;
					}
				}
			}
			case ArrayType at -> {
				switch (t2) {
					case null -> {
						return false;
					}
					case ArrayType at2 -> {
						return CompareType(at.type,at2.type);
					}
					case default ->{
						return false;
					}
				}
			}
			case StructType st -> {
				switch (t2){
					case null -> {
						return false;
					}
					case StructType st2 -> {
						return st.name.equals(st2.name);
					}
					case default ->{
						return false;
					}
				}
			}
			case ClassType ct ->{
				switch (t2){
					case null -> {
						return false;
					}
					case ClassType ct2 ->{
						return ct.className.equals(ct2.className);
					}
					case default ->{
						return false;
					}
				}
			}
			case default ->{
				if(t1 == t2){
					return true;
				}else{
					return false;
				}
			}
		}
	}

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
				for (ASTNode c : p.children()) {
					visit(c);
				}
				yield BaseType.NONE;
			}

			//------------------
			// Declarations
			//------------------

			case (VarDecl vd) -> {
				if (vd.type == BaseType.VOID)
					error("variable declaration " + vd.name + " has type void");
				yield BaseType.NONE;
			}

			case FunDecl fd -> {
				// yield none type here because should not be type checked against other things
				// need to  type declaraitons
				yield visit(fd.block);
				//yield BaseType.NONE;
			}

			//------------------
			// Expressions
			//------------------
			case (IntLiteral i) -> {
				i.type = BaseType.CHAR;
				yield BaseType.INT;
			}
			case (ChrLiteral c) -> {
				c.type = BaseType.CHAR;
				yield BaseType.CHAR;
			}
			case (StrLiteral s) -> {
				int length = s.strLiteral.length() + 1;
				Type t1 = new ArrayType(BaseType.CHAR, length);
				s.type = t1;
				yield t1;
			}
			case (VarExpr v) -> {
				// if v.vd is not initializaed then problem lies in name analyzer

				v.type = v.vd.type;
				yield v.vd.type;



			}
			case (FunCallExpr fc) -> {
				// if all expressions are of correct type, then fc is of type of declaration
				if (fc.exprs.size() != fc.fd.params.size()) error("Params size does not match exprs");

				for (int i = 0; i < fc.exprs.size(); i++) {
					// cant compare types with ==

					if (CompareType(visit(fc.exprs.get(i)), fc.fd.params.get(i).type)) {
						continue;
					} else {
						error(fc.name);
						error(visit(fc.exprs.get(i)).toString() + " Not equal " + fc.fd.params.get(i).type.toString());
					}
				}
				fc.type = fc.fd.type;
				yield fc.fd.type;
			}
			case (BinOp binOp) -> {
				// Note: two types of binops,  EQ and NE are in there own bin
				if (binOp.op == Op.EQ || binOp.op == Op.NE) {
					Type t1 = visit(binOp.lhs);
					switch (t1) {
						case ArrayType at -> {
							error(" lhs of Type struct type");
							yield BaseType.UNKNOWN;
						}
						case StructType st -> {
							error(" lhs of Type Struct type");
							yield BaseType.UNKNOWN;
						}
						case default -> {
							if (t1 == BaseType.VOID) {
								error(" lhs of Type void");
							}
							Type t2 = visit(binOp.rhs);
							// not sure if I can just set these equality?
							// test but might have to add second switch statement to make sure
							// t2 is of type basetype;
							if (t1 == t2) {
								binOp.type = BaseType.INT;
								yield BaseType.INT;
							} else {
								error("rhs of different type to lhs");
								yield BaseType.UNKNOWN;
							}
						}

					}
				} else {
					if (visit(binOp.lhs) == BaseType.INT && visit(binOp.rhs) == BaseType.INT) {
						binOp.type = BaseType.INT;
						yield binOp.type;
					} else {
						binOp.type = BaseType.UNKNOWN;
						yield BaseType.UNKNOWN;
					}

				}
			}

			case (ArrayAccessExpr a) -> {
				// should there be a differnt case for pointer
				a.name.type = visit(a.name);
				switch (a.name.type) {
					case null -> {
						error("Array access expr has no type");
						yield BaseType.UNKNOWN;
					}
					case PointerType p -> {
						a.index.type = visit(a.index);
						if (a.index.type == BaseType.INT) {
							a.type = p.type;
							yield p.type;
						} else {
							a.type = BaseType.UNKNOWN;
							yield BaseType.UNKNOWN; // not sure if this should be unknown or none
						}
					}
					case ArrayType ar -> {
						a.index.type = visit(a.index);
						if (a.index.type == BaseType.INT) {
							a.type = ar.type;
							yield ar.type;
						} else {
							a.type = BaseType.UNKNOWN;
							yield BaseType.UNKNOWN;
						}

					}
					case default -> {
						error("array access expression not of type pointer or array");
						yield BaseType.UNKNOWN;
					}


				}

			}

			case (FieldAccessExpr f) -> {
				Type t1 = visit(f.expr);
				switch (t1) {
					case null -> {
						error("field access expression null");
						yield BaseType.UNKNOWN;
					}
					case StructType st -> {
						// make sure field exisits within struct
						boolean exists = false;
						VarDecl vd = null;
						for (VarDecl s : st.fields) {
							if (s.name.equals(f.field)) {
								exists = true;
								vd = s;
								break;
							}
						}
						if (exists) {
							// indicates vd was also initialized
							f.structType = st;
							f.type = vd.type;
							yield vd.type;
						} else {
							error("field " + f.field + " does not exist within struct");
							yield BaseType.UNKNOWN;
						}
						// Struct type st should be unique
					}case ClassType ct ->{
						for(VarDecl vd : ct.classDecl.varDecls){
							if(f.field.equals(vd.name)){
								yield(vd.type);
							}
						}
						error("Field " + f.field + " does not exist within class " + ct.className);
						yield BaseType.UNKNOWN;
					}
					case default -> {
						error("field access expression not of type struct type");
						yield BaseType.UNKNOWN;
					}
				}
			}

			case (ValueAtExpr v) -> {
				Type t1 = visit(v.expr);
				switch (t1) {
					case null -> {
						error("type null");
						yield BaseType.UNKNOWN;
					}
					case PointerType pt -> {
						v.type = pt.type;
						yield pt.type;
					}
					case default -> {
						error("not pointer type");
						yield BaseType.UNKNOWN;
					}
				}


			}
			case (AddressOfExpr a) -> {
				Type t1 = visit(a.expr);
				Type pt = new PointerType(t1, 1);
				a.type = pt;
				yield pt;
			}
			case (SizeOfExpr s) -> {
				yield BaseType.INT;
			}
			case (TypecastExpr t) -> {
				Type t1 = visit(t.expr);
				if (t1 == BaseType.CHAR) {
					//char to int
					if(t.castType == BaseType.INT){
						t.type = BaseType.INT;
						yield BaseType.INT;
					}else{
						error("unknown type to cast");
						yield BaseType.UNKNOWN;
					}
				}

				switch (t1) {
					case null -> {
						error("type of expr is null");
						yield BaseType.UNKNOWN;
					}
					case ArrayType ar -> {
						Type p1 = new PointerType(t.castType, 1);
						t.type = p1;
						yield p1;
					}
					case PointerType pt -> {
						Type p1 = new PointerType(t.castType, 1);
						t.type = p1;
						yield p1;
					}
					case ClassType classType -> {
						boolean ancestor = false;
						ClassType ct = classType;
						while(ct.classDecl.parent != null){
							if(CompareType(ct.classDecl.parent,t.castType)){
								ancestor = true;
								break;
							}
						}
						if(!ancestor){
							error("cast is of incorrect type");
							yield BaseType.UNKNOWN;
						}
						t.type = t.castType;
						yield t.type;
					}
					default -> {
						error("didnt find type to cast to");
						yield BaseType.UNKNOWN;
					}
				}
			}

			case (Assign a) -> {
				// make sure that lhs is lval
				Type t1 = visit(a.expr1);
				Type t2 = visit(a.expr2);

				if(t1 == BaseType.VOID || t2 == BaseType.VOID || t1 == null || t2 == null){
					error("unknown");
					yield BaseType.UNKNOWN;
				}
				switch (t1) {
					case ArrayType at ->{
						error("unknown");
						yield BaseType.UNKNOWN;
					}
					default -> {
						switch(t2){
							case null -> {
								error("unknwon");
								yield BaseType.UNKNOWN;
							}
							case ArrayType at ->{
								error("unknown");
								yield BaseType.UNKNOWN;
							}
							default -> {
								if(CompareType(t1,t2) ){
									a.type = t1;
									yield t1;
								}else{
									error("Assignment left side type " + t1.toString() + " not equal " + t2.toString());
									yield BaseType.UNKNOWN;
								}


							}
						}
					}

				}

			}
			//----------------
			// Statements
			//----------------

			case(While w) -> {
				if(visit(w.expr) != BaseType.INT){
					error("While unknown");
					yield BaseType.UNKNOWN;
				}
				visit(w.stmt);
				yield BaseType.NONE;
			}
			case (If a) -> {

			if(a.stmt2 == null)
			{
				if(visit(a.expr) != BaseType.INT){
					error("While unknown");
					yield BaseType.UNKNOWN;
				}
				visit(a.stmt1);
				yield BaseType.NONE;
			}else{
				if(visit(a.expr) != BaseType.INT){
					error("While unknown");
					yield BaseType.UNKNOWN;
				}
				visit(a.stmt1);
				visit(a.stmt2);
				yield BaseType.NONE;
			}


			}
			case(Return r) -> {
				// return nothing or else?
				// link up return to function
				if(r.expr == null){
					if(r.fd.type == BaseType.VOID){
						yield BaseType.NONE;
					}// no return
				}else{
					if(CompareType(visit(r.expr),r.fd.type)){
						yield r.fd.type;
					}
				}
				error("Return type of " + r.fd.name.toString() + " does not match function decl");

				yield BaseType.UNKNOWN;

			}
			//----------------
			// Struct Decl
			//----------------

			case (StructTypeDecl std) -> {
				// special case
				// to complete
				yield BaseType.NONE; // to change
			}

			case (Type t) -> {
				yield t;
			}

			case ExprStmt e -> {
				yield visit(e.expr);
			}

			//---------------
			// Class
			//---------------

			/*default -> {
				error("unknown ast node");
				yield BaseType.UNKNOWN; // or unknown?
			}*/

			case ClassDecl classDecl -> {
				yield BaseType.NONE;
			}
			case ClassFunCallExpr classFunCallExpr -> {
				Type type = visit(classFunCallExpr.expr);
				try{
					ClassType ct = ((ClassType) type);
					for(FunDecl fd : ct.classDecl.getAllFunDecl()){
						if(classFunCallExpr.funCallExpr.name.equals(fd.name)){
							yield fd.type;
						}
					}

					error("FunCall not defined in classdecl funcall");
					yield BaseType.UNKNOWN;

				}catch(Exception e){
					error("ClassFunCallExpr expr of wrong type");
					yield BaseType.UNKNOWN;
				}

			}
			case ClassInstantiationExpr classInstantiationExpr -> {
				yield classInstantiationExpr.classType;
			}
		};

	}


}
