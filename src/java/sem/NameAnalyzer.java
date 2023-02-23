package sem;

import ast.*;

import java.util.ArrayList;

public class NameAnalyzer extends BaseSemanticAnalyzer {
	static Scope current; // static?

	public void visit(ASTNode node) {
		switch(node) {
			case null -> {
				throw new IllegalStateException("Unexpected null value");
			}

			case Block b -> {
				Scope outer = current;
				current = new Scope(outer);
				for( VarDecl vd :b.vds){
					visit(vd);
				}
				for( Stmt stmt : b.stmts ){
					visit(stmt);
				}
				current = outer;
				// to complete
			}

			case FunDecl fd -> {
				// to complete
				Symbol sym = current.lookupCurrent(fd.name);
				switch (sym){
					// null is good
					case null -> {
						current.put(new FunSymbol(fd));
						// think about current variables declared in block
						// here a will represent variable declarations in the function
						// plus the variables declarations in the block
						// we change the blocks vds to a, such that the nama analyzer will add these
						// to the new scope
						// right at the start.
						ArrayList<VarDecl> a = new ArrayList<>();
						a.addAll(fd.params);
						a.addAll(fd.block.vds);
						fd.block.vds = a;
						visit(fd.block);
					}
					// not null means declared within scope
					default -> {
						error("Function " + fd.name + " already declared within this scope");
						break;
					}
				}


			}

			case Program p -> {
				current = new Scope();
				for(ASTNode child : p.decls){
					visit(child);
				}
			}

			case (VarDecl vd) -> {
				// Just lookup within current, because variable can be shadowed if current == null
				Symbol sym = current.lookupCurrent(vd.name);
				switch (sym){
					// null is good
					case null -> {
						switch(vd.type){
							case null -> { error(vd.name + " has no type");}
							case StructType st -> {
								Symbol sd =  current.lookup(st.name);
								switch (sd) {
									case null -> { error("struct not defined");}
									case StructSymbol ss ->{
										st.fields = ss.sd.fields;
										current.put(new VarSymbol(vd));
									}default -> error("strange ast");
								}
							}
							default -> {
								current.put(new VarSymbol(vd));
							}
						}

					}
					// not null means declared within scope
					default -> {
						error("Variable " + vd.name + " already declared within this scope");
						break;
					}
				}

			}

			case (VarExpr v) -> {
				Symbol sym = current.lookup(v.name);
				switch (sym){
					case null -> {
						error(v.name + " not defined");
					}
					// we want to find something in global scope
					case VarSymbol vs -> {
						v.vd = vs.vd;
					}
					case StructSymbol sd ->{
						visit(v.type); // we want to store data in type
					}
					case default -> {
						error("Variable " + v.name + " not defined within scope");
					}
				}
			}

			case (StructTypeDecl std) -> {
				// check for previous declaration, if null add new symbol
				// make sure fields are not declared twice, lookup every time
				// initial lookup for struct
				Symbol sym = current.lookupCurrent(std.structType.name);
				switch (sym){
					// we dont want to find anything
					case null -> {
						// name not defined, now watch out for double field definition
						// create new scope
						// need to add name to the scope
						current.put(new StructSymbol(std));
						Scope scope = new Scope(current);

						// temporary scope
						for(VarDecl field : std.fields){
							if(scope.lookupCurrent(field.name) == null){
								scope.put(new VarSymbol(field));
							}else{
								error("Struct field " + field.name + " already defined within struct");
							}
						}
						// didn't change the scope so we don't need too change it back


					}
					case default -> {
						error("struct " + std.name + " already defined within scope");
					}
				}

			}


			case (Type t) -> {
				// deal with struct expression linking to struct;
				// check for struct type? think more about this

			}

			case (FunCallExpr fc) -> {
				Symbol sym = current.lookup(fc.name);
				switch (sym){
					// we want to find something in global scope
					case null -> {
						error("Function " + fc.name + " not defined within scope");
					}
					case FunSymbol f -> {
						fc.fd = f.fd;
					}
					case default -> {
						error("Strange lookup");
					}
				}


			}
			case (ArrayAccessExpr a) -> {
				// visit will check for declaration
				// have to visit because expression

				// if we get to this point
				if( a.name != null){
					visit(a.name);
					a.type = a.name.type;
				}

			}
			case (ExprStmt e) -> {
				visit(e.expr);
			}

			// to complete ...
			case SizeOfExpr sizeOfExpr -> {

			}
			case ValueAtExpr valueAtExpr -> {
				visit(valueAtExpr.expr);

			}
			case Assign assign -> {
				visit(assign.expr1);
				visit(assign.expr2);
			}
			case If anIf -> {
				visit(anIf.expr);
				visit(anIf.stmt1);
				visit(anIf.stmt2);
			}
			case TypecastExpr typecastExpr -> {
				visit(typecastExpr.expr);
			}
			case AddressOfExpr addressOfExpr -> {
				visit(addressOfExpr.expr);
			}
			case BinOp binOp -> {
				visit(binOp.lhs);
				visit(binOp.rhs);
			}
			case While aWhile -> {
				visit(aWhile.stmt);
				visit(aWhile.expr);
			}
			case Return aReturn -> {
				// have to be careful with optional arguments
				if(aReturn.expr != null){
					visit(aReturn.expr);
				}

			}

			case FieldAccessExpr fieldAccessExpr -> {
				visit(fieldAccessExpr.expr);

				// check if field actually exists
				// no way to reach the structure
				// must happen in type analysis

			}

			case default -> {}


		};

	}




}
