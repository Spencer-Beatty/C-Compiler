package ast;

import java.io.PrintWriter;

public class ASTPrinter {

    private final PrintWriter writer;
    private String tab = "";

    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    private void tabInc(){
        tab = tab + "\t";
    }
    private void tabDec(){
        tab = tab.replaceFirst("\t","");
    }

    public void visit(ASTNode node) {
        switch(node) {
            case null -> {
                throw new IllegalStateException("Unexpected null value");
            }

            case Block b -> {
                tabInc();
                writer.print("\n"+tab+"Block(");
                tabInc();
                String delimiter = "";
                for(VarDecl vd: b.vds){
                    writer.print(delimiter);
                    delimiter = ",\n"+tab;
                    visit(vd);
                }
                for(Stmt stmt: b.stmts){
                    writer.print(delimiter);
                    delimiter = ",\n"+tab;
                    visit(stmt);
                }
                tabDec();
                writer.print("\n"+tab+")");

            }

            case FunDecl fd -> {
                writer.print("FunDecl(");
                visit(fd.type);
                writer.print(","+fd.name+",");
                for (VarDecl vd : fd.params) {
                    visit(vd);
                    writer.print(",");
                }
                visit(fd.block);
                writer.print(")");
            }

            case Program p -> {
                writer.print("Program(");
                String delimiter = "";
                for (Decl d : p.decls) {
                    writer.print(delimiter);
                    delimiter = ",";
                    visit(d);
                }
                writer.print(")");
                writer.flush();
            }

            case (VarDecl vd) -> {
                writer.print("VarDecl(");
                visit(vd.type);
                writer.print(","+vd.name);
                writer.print(")");
            }

            case (VarExpr v) -> {
                writer.print("VarExpr(");
                writer.print(v.name);
                writer.print(")");
            }

            case (BaseType bt) -> {
                writer.print(bt);
            }

            case (StructTypeDecl std) -> {
                writer.print("StructTypeDecl(");
                visit(std.structType);
                for(VarDecl vd : std.fields){
                    writer.print(",");
                    visit(vd);
                }
                writer.print(")");
            }
            case (StructType structType) -> {
                writer.print("StructType(");
                writer.print(structType.name);
                writer.print(")");
            }

            // to complete ...
            case IntLiteral intLiteral -> {
                writer.print("IntLiteral(");
                writer.print(intLiteral.intLiteral);
                writer.print(")");
            }
            case While aWhile -> {
                writer.print("While(");
                visit(aWhile.expr);
                writer.print(",");
                visit(aWhile.stmt);
                writer.print(")");
            }
            case Assign assign -> {
                writer.print("Assign(");
                visit(assign.expr1);
                writer.print(",");
                visit(assign.expr2);
                writer.print(")");
            }
            case If anIf -> {
                writer.print("If(");
                visit(anIf.expr);
                writer.print(",");
                visit(anIf.stmt1);
                if(anIf.stmt2!=null)
                {
                    writer.print(",");
                    visit(anIf.stmt2);
                }
                writer.print(")");
            }
            case BinOp binOp -> {
                writer.print("BinOp(");
                visit(binOp.lhs);
                writer.print(",");
                writer.print(binOp.op.toString());
                writer.print(",");
                visit(binOp.rhs);
                writer.print(")");

            }
            case TypecastExpr typecastExpr -> {
                writer.print("TypecastExpr(");
                visit(typecastExpr.type);
                writer.print(",");
                visit(typecastExpr.expr);
                writer.print(")");
            }
            case ChrLiteral chrLiteral -> {
                writer.print("ChrLiteral(");
                writer.print(chrLiteral.chrLiteral);
                writer.print(")");
            }
            case ValueAtExpr valueAtExpr -> {
                writer.print("ValueAtExpr(");
                visit(valueAtExpr.expr);
                writer.print(")");
            }
            case FunCallExpr funCallExpr -> {
                writer.print("FunCallExpr(");
                writer.print(funCallExpr.name);
                for(Expr expr : funCallExpr.exprs){
                    writer.print(",");
                    visit(expr);
                }
                writer.print(")");


            }
            case Return aReturn -> {
                writer.print("Return(");
                if(aReturn.expr != null){
                    visit(aReturn.expr);
                }
                writer.print(")");
            }
            case ArrayType arrayType -> {
                writer.print("ArrayType(");
                visit(arrayType.type);
                writer.print(",");
                writer.print(arrayType.length);
                writer.print(")");
            }
            case FieldAccessExpr fieldAccessExpr -> {
                writer.print("FieldAccess(");
                visit(fieldAccessExpr.expr);
                writer.print(",");
                writer.print(fieldAccessExpr.field);
                writer.print(")");
            }
            case SizeOfExpr sizeOfExpr -> {
                writer.print("SizeOfExpr(");
                visit(sizeOfExpr.type);
                writer.print(")");
            }
            case ExprStmt exprStmt -> {
                writer.print("ExprStmt(");
                visit(exprStmt.expr);
                writer.print(")");
            }
            case ArrayAccessExpr arrayAccessExpr -> {
                writer.print("ArrayAccessExpr(");
                visit(arrayAccessExpr.name);
                writer.print(",");
                visit(arrayAccessExpr.index);
                writer.print(")");
            }
            case AddressOfExpr addressOfExpr -> {
                writer.print("AddressOfExpr(");
                visit(addressOfExpr.expr);
                writer.print(")");
            }
            case StrLiteral strLiteral -> {
                writer.print("StrLiteral(");
                writer.print(strLiteral.strLiteral);
                writer.print(")");
            }
            case PointerType pointerType -> {
                writer.print("PointerType(");
                visit(pointerType.type);
                writer.print(")");
            }
            case ClassType classType ->{
                writer.print("ClassType(");
                writer.print(classType.className);
                writer.print(")");
            }
            case ClassInstantiationExpr classInstantiationExpr -> {
                writer.print("ClassInstantiationExpr(") ;
            visit(classInstantiationExpr.classType);
                writer.print(")");
            }
            case ClassDecl classDecl -> {
                writer.print("ClassDecl(");
                if(classDecl.parent != null){
                    writer.print("Parent(" + classDecl.parent.className + ")");
                }
                for( VarDecl i : classDecl.varDecls){
                    visit(i);
                    writer.print(",");
                }
                for( FunDecl i : classDecl.funDecls){
                    visit(i);
                    writer.print(",");
                }
            }
            case ClassFunCallExpr classFunCallExpr -> {
                writer.print("ClassFunCallExpr(");
                visit(classFunCallExpr.expr);
                visit(classFunCallExpr.funCallExpr);
                writer.print(")");
            }
        }

    }


    
}
