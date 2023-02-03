package ast;

import java.io.PrintWriter;

public class ASTPrinter {

    private final PrintWriter writer;

    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    public void visit(ASTNode node) {
        switch(node) {
            case null -> {
                throw new IllegalStateException("Unexpected null value");
            }

            case Block ignored -> {
                writer.print("Block(");
                // to complete
                writer.print(")");
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
                for (StructTypeDecl std : p.structTypeDecls) {
                    writer.print(delimiter);
                    delimiter = ",";
                    visit(std);
                }
                for (VarDecl vd : p.varDecls) {
                    writer.print(delimiter);
                    delimiter = ",";
                    visit(vd);
                }
                for (FunDecl fd : p.funDecls) {
                    writer.print(delimiter);
                    delimiter = ",";
                    visit(fd);
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
                // to complete ...
            }

            case (StructTypeDecl std) -> {
                // to complete
            }

            // to complete ...
        }

    }


    
}
