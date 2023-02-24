package sem;

import ast.*;

public class LeftValueAnalyzer extends BaseSemanticAnalyzer{


    public void visit(ASTNode node) {
        switch (node) {

            case null -> {
                throw new IllegalStateException("Unexpected null value");
                    }
            case Assign a -> {
                // expr1 = lhs
                visit(a.expr2);
                switch (a.expr1){
                    case null -> {
                        break;
                    }
                    case VarExpr ve ->{
                        // good!

                    }
                    case ArrayAccessExpr aa->{

                    }
                    case ValueAtExpr ve ->{
                        visit(ve.expr);
                    }
                    case FieldAccessExpr fe -> {
                        visit(fe.expr);
                    }
                    default -> {
                        error("Not an l-value");
                    }

                }
            }
            case ASTNode a -> {
                for(ASTNode b : a.children()){
                    visit(b);
                }
            }
        }
    }
}

