package parser;


import ast.*;
import lexer.Token;
import lexer.Token.TokenClass;
import lexer.Tokeniser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * @author cdubach
 * @author sbeatt4
 */
public class Parser {

    private Token token;

    private Queue<Token> buffer = new LinkedList<>();

    private final Tokeniser tokeniser;


    public Parser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }

    public Program parse() {
        // get the first token
        nextToken();

        return parseProgram();
    }

    public int getErrorCount() {
        return error;
    }

    private int error = 0;
    private Token lastErrorToken;

    private void error(TokenClass... expected) {

        if (lastErrorToken == token) {
            // skip this error, same token causing trouble
            return;
        }

        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (TokenClass e : expected) {
            sb.append(sep);
            sb.append(e);
            sep = "|";
        }
        System.out.println("Parsing error: expected (" + sb + ") found (" + token + ") at " + token.position);

        error++;
        lastErrorToken = token;
    }

    /*
     * Look ahead the i^th element from the stream of token.
     * i should be >= 1
     */
    private Token lookAhead(int i) {
        // ensures the buffer has the element we want to look ahead
        while (buffer.size() < i)
            buffer.add(tokeniser.nextToken());

        int cnt = 1;
        for (Token t : buffer) {
            if (cnt == i)
                return t;
            cnt++;
        }

        assert false; // should never reach this
        return tokeniser.nextToken();
    }


    /*
     * Consumes the next token from the tokeniser or the buffer if not empty.
     */
    private void nextToken() {
        if (!buffer.isEmpty())
            token = buffer.remove();
        else
            token = tokeniser.nextToken();
    }

    /*
     * If the current token is equals to the expected one, then skip it, otherwise report an error.
     */
    private void expect(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass) {
                nextToken();
                return;
            }
        }
        error(expected);
    }

    /*
     * Returns true if the current token is equals to any of the expected ones.
     */
    private boolean accept(TokenClass... expected) {
        for (TokenClass e : expected) {
            if (e == token.tokenClass)
                return true;
        }
        return false;
    }


    // includes are ignored, so does not need to return an AST node


    private Program parseProgram() {
        // (Include)*
        parseIncludes();

        List<Decl> decls = new ArrayList<>();
        // (structdecl | vardecl | fundecl | classdecl)*
        while (accept(TokenClass.STRUCT, TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.CLASS)) {
            // structdecl
            if(token.tokenClass == TokenClass.CLASS){
                if(lookAhead(1).tokenClass == TokenClass.IDENTIFIER){
                    decls.add(parseClassDecl());
                }else{
                    error(TokenClass.CLASS);
                }
            }
            else if (token.tokenClass == TokenClass.STRUCT &&
                    lookAhead(1).tokenClass == TokenClass.IDENTIFIER &&
                    lookAhead(2).tokenClass == TokenClass.LBRA) {
                decls.add(parseStructDecl());
                //continue;
            }
            // fundecl where type != struct
            else if (lookAhead(1).tokenClass == TokenClass.IDENTIFIER &&
                    lookAhead(2).tokenClass == TokenClass.LPAR) {
                decls.add(parseFunDecl());
                // fundecl where type = structtype (struct ident) followed by ident
            } else if (token.tokenClass == TokenClass.STRUCT &&
                    lookAhead(1).tokenClass == TokenClass.IDENTIFIER &&
                    lookAhead(2).tokenClass == TokenClass.IDENTIFIER &&
                    lookAhead(3).tokenClass == TokenClass.LPAR) {
                decls.add(parseFunDecl());
                // vardecl
            } else {
                decls.add(parseVarDecl());
            }
        }

        // to be completed ...


        // EOF
        expect(TokenClass.EOF);
        return new Program(decls);
    }
    private ClassType parseClassType(){
        if(accept(TokenClass.CLASS)){
            nextToken();
            String classType = token.data;
            expect(TokenClass.IDENTIFIER);
            return new ClassType(classType);
        }else{
            error(TokenClass.CLASS);
            nextToken();
        }
        return null;
    }

    private ClassDecl parseClassDecl(){
        //classtype ["extends" IDENT] "{" (vardecl)* (fundecl)* "}"

        ClassType c = parseClassType();
        String parentClass = null;
        // extends goes here
        if(token.tokenClass == TokenClass.EXTENDS){
            nextToken();
            parentClass = token.data;
            expect(TokenClass.IDENTIFIER);
        }
        ArrayList<VarDecl> varDecls = new ArrayList<>();
        ArrayList<FunDecl> funDecls = new ArrayList<>();
        expect(TokenClass.LBRA);
        boolean doneVarDecl = false;
        while(accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT, TokenClass.CLASS)){
            if(doneVarDecl){
                funDecls.add(parseFunDecl());
            }else{
                // case of int a() or int a;
                if(accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID)){
                    if(lookAhead(1).tokenClass == TokenClass.IDENTIFIER && lookAhead(2).tokenClass == TokenClass.LPAR){
                        doneVarDecl = true;
                        continue;
                    }
                }else{ // of type struct t a() or class t a() or struct t a; or class t a;
                    if(lookAhead(2).tokenClass == TokenClass.IDENTIFIER && lookAhead(3).tokenClass == TokenClass.LPAR){
                        // function is made
                        doneVarDecl = true;
                        continue;
                    }
                }
                varDecls.add(parseVarDecl());
            }
        }
        expect(TokenClass.RBRA);
        if(parentClass == null){
            return new ClassDecl(c, null, varDecls, funDecls);
        }else{
            return new ClassDecl(c, new ClassType(parentClass), varDecls, funDecls);
        }

    }
    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private StructType parseStructType() {
        // checks struct twice, but is ok because accept does not consume
        // and will prevent calling parse struct type at the wrong time.
        if (accept(TokenClass.STRUCT)) {
            nextToken();
            String structType = token.data;
            expect(TokenClass.IDENTIFIER);
            return new StructType(structType);
        } else {
            error(TokenClass.STRUCT);
            nextToken();
        }
        return null;

    }

    private StructTypeDecl parseStructDecl() {
        if (accept(TokenClass.STRUCT)) {
            StructType structType = parseStructType();
            ArrayList<VarDecl> vds = new ArrayList<>();

            expect(TokenClass.LBRA);
            if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT, TokenClass.CLASS)) {
                vds.add(parseVarDecl());
                while (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT, TokenClass.CLASS)) {
                    vds.add(parseVarDecl());
                }
            } else {
                error(TokenClass.INT);
                nextToken();
            }
            expect(TokenClass.RBRA);
            expect(TokenClass.SC);
            return new StructTypeDecl(structType, vds);
            // to be completed ...
        }
        error(TokenClass.STRUCT);
        return null;
    }

    // type IDENT "(" params ")" block
    private FunDecl parseFunDecl() {
        Block block;
        Type type = parseType();
        String name = token.data;
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        ArrayList<VarDecl> params = parseParams();
        expect(TokenClass.RPAR);
        if (accept(TokenClass.LBRA)) {
            block = parseBlock();
        } else {
            block = null;
            error(TokenClass.LBRA);
            nextToken();
        }
        return new FunDecl(type, name, params, block);


    }

    private ArrayList<VarDecl> parseParams() {
        ArrayList<VarDecl> params = new ArrayList<>();
        if (accept(TokenClass.RPAR)) {
            // consume of RPAR dealt with after returning
            return params;
        } else {
            Type type = parseType();
            params.add(new VarDecl(type, token.data));
            expect(TokenClass.IDENTIFIER);
        }
        while (!accept(TokenClass.RPAR, TokenClass.EOF)) {
            expect(TokenClass.COMMA);
            Type type = parseType();
            params.add(new VarDecl(type, token.data));
            expect(TokenClass.IDENTIFIER);
        }
        return params;
    }

    private VarDecl parseVarDecl() {
        Type type = parseType();
        int length;

        String name = token.data;
        expect(TokenClass.IDENTIFIER);
        // if name is not an identifier then error will be raised
        while (accept(TokenClass.LSBR)) {
            // expect [ INT_LITERAL ]
            nextToken();
            if (token.tokenClass == TokenClass.INT_LITERAL) {
                length = Integer.parseInt(token.data);
                nextToken();
            } else {
                error(TokenClass.INT_LITERAL);
                length = -1000;
            }
            expect(TokenClass.RSBR);
            type = new ArrayType(type, length);
        }

        expect(TokenClass.SC);
        return new VarDecl(type, name);

    }

    private Type parseType() {
        Type type;
        if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID)) {
            switch (token.tokenClass) {
                case INT -> type = BaseType.INT;
                case CHAR -> type = BaseType.CHAR;
                case VOID -> type = BaseType.VOID;
                default -> type = null;
            }
            nextToken();
        } else if (accept(TokenClass.STRUCT)) {
            type = parseStructType();
        } else if(accept(TokenClass.CLASS)) {
            type = parseClassType();
        }else {
            error(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT, TokenClass.CLASS);
            nextToken(); // need to consusme otherwise get stuck in loop potentially
            return null;
        }
        if (accept(TokenClass.ASTERIX)) {
            int pointers = 0;
            while (accept(TokenClass.ASTERIX)) {
                pointers++;
                nextToken();
            }
            // do it in two lines to not run into errors or have to
            // add an additional statement
            PointerType pt = new PointerType(type, pointers);
            type = pt;
        }
        return type;
    }

    private Block parseBlock() {
        // "{" (vardecl)* (stmt)* "}"
        ArrayList<VarDecl> vds = new ArrayList<>();
        ArrayList<Stmt> stmts = new ArrayList<>();
        if (accept(TokenClass.LBRA)) {
            nextToken();
            // if starts with type then must be a var decl
            while (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT, TokenClass.CLASS)) {
                vds.add(parseVarDecl());
            }

            while (!accept(TokenClass.RBRA, TokenClass.EOF)) {
                // need to make sure that parseStmt always consumes a character
                stmts.add(parseStmt());
            }

            // if we reach here we have found the '}' or EOF
            expect(TokenClass.RBRA);
            return new Block(vds, stmts);
        }
        error(TokenClass.LBRA);
        return null;
    }

    private Stmt parseStmt() {
        if (accept(TokenClass.LBRA)) {
            return parseBlock();
        } else if (accept(TokenClass.WHILE)) {
            nextToken();
            expect(TokenClass.LPAR);
            Expr expr = parseExpr();
            expect(TokenClass.RPAR);
            Stmt stmt = parseStmt();
            return new While(expr, stmt);
        } else if (accept(TokenClass.IF)) {
            nextToken();
            expect(TokenClass.LPAR);
            Expr expr = parseExpr();
            expect(TokenClass.RPAR);
            Stmt stmt1 = parseStmt();
            Stmt stmt2 = null;
            if (accept(TokenClass.ELSE)) {
                nextToken();
                stmt2 = parseStmt();
            }
            return new If(expr, stmt1, stmt2);
        } else if (accept(TokenClass.RETURN)) {
            nextToken();
            if (accept(TokenClass.SC)) {
                nextToken();
                return new Return(null);
            } else {
                Expr expr = parseExpr();
                expect(TokenClass.SC);
                return new Return(expr);
            }
        } else {
            Expr expr = parseExpr();
            expect(TokenClass.SC);
            return new ExprStmt(expr);
        }

    }

    // note change this to parseExp at the end
    private Expr parseExpr() {
        Expr lhs = parseAssign();
        return lhs;
    }

    // precedence level 9
    private Expr parseAssign() {
        // (A ('=') )* A
        Expr lhs = parseOr();
        // parse or should move the token forward or return an error
        if (accept(TokenClass.ASSIGN)) {
            nextToken();
            if (accept(TokenClass.NEW)){
                // a = new class comp();
                nextToken();
                ClassType type = parseClassType();
                expect(TokenClass.LPAR);
                expect(TokenClass.RPAR);
                return new Assign(lhs, new ClassInstantiationExpr(type));
            }else{
                Expr rhs = parseAssign();
                return new Assign(lhs, rhs);
            }
        } else {
            return lhs;
        }
    }

    // precedence level 8
    private Expr parseOr() {
        Expr lhs = parseAnd();
        // parse and should move forward similar to above,
        // left associative so we can use iteration
        while (accept(TokenClass.LOGOR)) {
            nextToken();
            Expr rhs = parseAnd();
            lhs = new BinOp(lhs, Op.OR, rhs);
        }
        return lhs;
    }

    // precedence level 7
    private Expr parseAnd() {
        Expr lhs = parseEquality();
        while (accept(TokenClass.LOGAND)) {
            nextToken();
            Expr rhs = parseEquality();
            lhs = new BinOp(lhs, Op.AND, rhs);
        }
        return lhs;
    }

    // precedence level 6
    // == !=
    private Expr parseEquality() {
        Expr lhs = parseInEquality();
        while (accept(TokenClass.EQ, TokenClass.NE)) {
            Op op;
            if (token.tokenClass == TokenClass.EQ) {
                op = Op.EQ;
            } else {
                // must be != because passed accept
                op = Op.NE;
            }
            nextToken();

            Expr rhs = parseInEquality();
            lhs = new BinOp(lhs, op, rhs);
        }
        return lhs;
    }

    // precedence level 5
    private Expr parseInEquality() {
        Expr lhs = parseTerm();
        while (accept(TokenClass.LT, TokenClass.GT, TokenClass.LE, TokenClass.GE)) {
            Op op;
            if (token.tokenClass == TokenClass.LT) {
                op = Op.LT;
            } else if (token.tokenClass == TokenClass.GT) {
                op = Op.GT;
            } else if (token.tokenClass == TokenClass.LE) {
                op = Op.LE;
            } else {
                op = Op.GE;
            }
            nextToken();
            Expr rhs = parseTerm();
            lhs = new BinOp(lhs, op, rhs);
        }
        return lhs;
    }

    // precedence level 4
    private Expr parseTerm() {
        Expr lhs = parseFactor();
        while (accept(TokenClass.PLUS, TokenClass.MINUS)) {
            Op op;
            if (token.tokenClass == TokenClass.PLUS) {
                op = Op.ADD;
            } else {
                op = Op.SUB;
            }
            nextToken();

            Expr rhs = parseFactor();
            lhs = new BinOp(lhs, op, rhs);
        }
        return lhs;
    }

    // precedence level 3
    private Expr parseFactor() {
        Expr lhs = parsePre();
        while (accept(TokenClass.ASTERIX, TokenClass.DIV, TokenClass.REM)) {
            Op op;
            if (token.tokenClass == TokenClass.ASTERIX) {
                op = Op.MUL; // think about other cases for * could they arise? a* + b
                // they are prevented by left hand going forward first
            } else if (token.tokenClass == TokenClass.REM) {
                op = Op.MOD;
            } else {
                op = Op.DIV;
            }
            nextToken();

            Expr rhs = parsePre();
            lhs = new BinOp(lhs, op, rhs);
        }
        return lhs;
    }

    // precedence level 2
    private Expr parsePre() {
        // right to left associative, use recursion
        // & * (type) + -
        Expr rhs;
        if (token.tokenClass == TokenClass.AND) {
            nextToken();
            rhs = parsePre();
            // parse pre again because of &(&(&(a)))
            return new AddressOfExpr(rhs);
        } else if (token.tokenClass == TokenClass.ASTERIX) {
            nextToken();
            rhs = parsePre();
            // parse pre again because of *(*(*(a)))
            return new AddressOfExpr(rhs);
        } else if (token.tokenClass == TokenClass.LPAR &&
                lookAhead(1).tokenClass == TokenClass.INT ||
                lookAhead(1).tokenClass == TokenClass.CHAR ||
                lookAhead(1).tokenClass == TokenClass.STRUCT ||
                lookAhead(1).tokenClass == TokenClass.VOID
        ) {
            nextToken();
            Type type = parseType();
            // should find RPAR
            expect(TokenClass.RPAR);
            rhs = parsePre();
            return new TypecastExpr(type, rhs);
        } else if (token.tokenClass == TokenClass.PLUS) {
            // indicating +a
            nextToken();
            Expr zero = new IntLiteral(0);
            rhs = parsePre();
            return new BinOp(zero, Op.ADD, rhs);
        } else if (token.tokenClass == TokenClass.MINUS) { // MINUS
            // indicating -a
            nextToken();
            Expr zero = new IntLiteral(0);
            rhs = parsePre();
            return new BinOp(zero, Op.SUB, rhs);
        }
        // rejected by the accept
        rhs = parseAttribute();
        return rhs;
    }

    // precedence level 1
    private Expr parseAttribute() {
        // left to right but possibility for special cases.
        Expr lhs;
        if (accept(TokenClass.IDENTIFIER)) {
            String name = token.data;
            lhs = new VarExpr(name);
            nextToken();
            // a.Ident a[expr] a(expr)
            while (accept(TokenClass.DOT, TokenClass.LSBR, TokenClass.LPAR)) {
                if (token.tokenClass == TokenClass.DOT) {
                    nextToken();
                    if (accept(TokenClass.IDENTIFIER)) {
                        // check if ()
                        String field = token.data;
                        ArrayList<Expr> exprs = new ArrayList<>();
                        nextToken();
                        if (token.tokenClass ==TokenClass.LPAR ) {
                            // skip a ( expr* )
                            nextToken();
                            if (!accept(TokenClass.RPAR)) {
                                exprs.add(parseExpr());
                                while (!accept(TokenClass.RPAR, TokenClass.EOF)) {
                                    expect(TokenClass.COMMA);
                                    exprs.add(parseExpr());
                                }
                            }
                            nextToken();
                            lhs = new ClassFunCallExpr(lhs, new FunCallExpr(field, exprs));
                        }else{
                            lhs = new FieldAccessExpr(lhs, field);
                        }
                    }
                } else if (token.tokenClass == TokenClass.LSBR) {
                    nextToken();
                    Expr rhs = parseExpr(); // ?? seems like should go back and parse expr again
                    expect(TokenClass.RSBR);
                    lhs = new ArrayAccessExpr(lhs, rhs);
                } else {
                    //LPAR
                    nextToken();
                    ArrayList<Expr> exprs = new ArrayList<>();
                    if (!accept(TokenClass.RPAR)) {
                        exprs.add(parseExpr());
                        while (!accept(TokenClass.RPAR, TokenClass.EOF)) {
                            expect(TokenClass.COMMA);
                            exprs.add(parseExpr());
                        }
                    }
                    // type checker will check?
                    nextToken(); // consume rpar

                    lhs = new FunCallExpr(name, exprs);

                }
            }
        } else {
            lhs = parseImmediate();
        }
        return lhs;
    }

    // precedence level 0
    private Expr parseImmediate() {
        // note that we will need a way for empty to be passed afterwords.
        // 1 solution: rewrite everything with parseExprPrime
        // 2 solution: have global bool gate, expr parsed
        //identifier should be impossible
        if (accept(TokenClass.LPAR)) {
            nextToken();
            Expr lhs = parseExpr();
            // flawed logic, becuase when RPAR is encountered in parseExpr();
            // it cannot be proccessed; this is where we need parse expr ';
            expect(TokenClass.RPAR);
            return lhs;
        } else if (accept(TokenClass.INT_LITERAL)) {
            Expr lhs = new IntLiteral(Integer.parseInt(token.data));
            nextToken();
            return lhs;
        } else if (accept(TokenClass.CHAR_LITERAL)) {
            Expr lhs = new ChrLiteral(token.data.charAt(0));
            nextToken();
            return lhs;
        } else if (accept(TokenClass.STRING_LITERAL)) {
            Expr lhs = new StrLiteral(token.data);
            nextToken();
            return lhs;
        } else {
            error(TokenClass.LPAR);
            nextToken();
            return null;
        }

    }


}
