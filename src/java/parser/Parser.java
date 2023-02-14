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
        System.out.println("Parsing error: expected ("+sb+") found ("+token+") at "+token.position);

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

        int cnt=1;
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
        // (structdecl | vardecl | fundecl)*
        while (accept(TokenClass.STRUCT, TokenClass.INT, TokenClass.CHAR, TokenClass.VOID)) {
            // structdecl
            if (token.tokenClass == TokenClass.STRUCT &&
                    lookAhead(1).tokenClass == TokenClass.IDENTIFIER &&
                    lookAhead(2).tokenClass == TokenClass.LBRA) {
                decls.add(parseStructDecl());

            }
            // fundecl where type != struct
            else if(lookAhead(1).tokenClass == TokenClass.IDENTIFIER &&
                    lookAhead(2).tokenClass == TokenClass.LPAR) {
                decls.add(parseFunDecl());
                // fundecl where type = structtype (struct ident) followed by ident
            }else if(token.tokenClass==TokenClass.STRUCT &&
                    lookAhead(1).tokenClass == TokenClass.IDENTIFIER &&
                    lookAhead(2).tokenClass == TokenClass.IDENTIFIER) {
                decls.add(parseFunDecl());
                // vardecl
            }else{
                decls.add(parseVarDecl());
            }
        }

        // to be completed ...


        // EOF
        expect(TokenClass.EOF);
        return new Program(decls);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private StructType parseStructType(){
        // checks struct twice, but is ok because accept does not consume
        // and will prevent calling parse struct type at the wrong time.
        if(accept(TokenClass.STRUCT)) {
            nextToken();
            String structType = token.data;
            expect(TokenClass.IDENTIFIER);
            return new StructType(structType);
        }else{
            error(TokenClass.STRUCT);
            nextToken();
        }
        return null;

    }

    private StructTypeDecl parseStructDecl(){
        if(accept(TokenClass.STRUCT)){
            StructType structType = parseStructType();
            ArrayList<VarDecl> vds = new ArrayList<>();

            expect(TokenClass.LBRA);
            // at least 1 var decl
            if(accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT)){
                vds.add(parseVarDecl());
                while(accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT)){
                    vds.add(parseVarDecl());
                }
            }else{
                error(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT);
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
    private FunDecl parseFunDecl(){
        Block block;
        Type type = parseType();
        String name = token.data;
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        ArrayList<VarDecl> params = parseParams();
        expect(TokenClass.RPAR);
        if(accept(TokenClass.LBRA)){
            block = parseBlock();
        }else{
            block = null;
            error(TokenClass.LBRA);
            nextToken();
        }
        return new FunDecl(type, name, params, block);


    }

    private ArrayList<VarDecl> parseParams(){
        ArrayList<VarDecl> params = new ArrayList<>();
        if (accept(TokenClass.RPAR)){
            // consume of RPAR dealt with after returning
            return params;
        }else{
            Type type = parseType();
            params.add(new VarDecl(type, token.data));
            expect(TokenClass.IDENTIFIER);
        }
        while(! accept(TokenClass.RPAR) ){
            expect(TokenClass.COMMA);
            Type type = parseType();
            params.add(new VarDecl(type, token.data));
            expect(TokenClass.IDENTIFIER);
        }
        return params;
    }

    private VarDecl parseVarDecl(){
        Type type = parseType();
        String name = token.data;
        // if name is not an identifier then error will be raised
        expect(TokenClass.IDENTIFIER);
        while(accept(TokenClass.LSBR)){
            // expect [ INT_LITERAL ]
            nextToken();
            expect(TokenClass.INT_LITERAL);
            expect(TokenClass.RSBR);
        }
        expect(TokenClass.SC);
        return new VarDecl(type,name);

    }


    private Type parseType(){
        Type type;
        if(accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID)){
            switch (token.tokenClass){
                case INT -> type = BaseType.INT;
                case CHAR -> type = BaseType.CHAR;
                case VOID -> type = BaseType.VOID;
                default -> type = null;
            }
            nextToken();
        }else if(accept(TokenClass.STRUCT)){
            type = parseStructType();
        }
        else{
            error(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT);
            nextToken(); // need to consusme otherwise get stuck in loop potentially
            return null;
        }
        if(accept(TokenClass.ASTERIX)){
            ArrayList<String> pointers = new ArrayList<>();
            while(accept(TokenClass.ASTERIX)){
                pointers.add(token.data);
                nextToken();
            }
            // do it in two lines to not run into errors or have to
            // add an additional statement
            PointerType pt = new PointerType(type, pointers);
            type = pt;
        }
        return type;
    }

    private Block parseBlock(){
        // "{" (vardecl)* (stmt)* "}"
        ArrayList<VarDecl> vds = new ArrayList<>();
        ArrayList<Stmt> stmts = new ArrayList<>();
        if(accept(TokenClass.LBRA)) {
            nextToken();
            // if starts with type then must be a var decl
            while (accept(TokenClass.INT,TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)) {
                vds.add(parseVarDecl());
            }

            while(! accept(TokenClass.RBRA,TokenClass.EOF)){
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

    private Stmt parseStmt(){
        if(accept(TokenClass.LBRA)){
            return parseBlock();
        }else if(accept(TokenClass.WHILE)){
            nextToken();
            expect(TokenClass.LPAR);
            Expr expr = parseExp();
            expect(TokenClass.RPAR);
            Stmt stmt = parseStmt();
            return new While(expr,stmt);
        }else if(accept(TokenClass.IF)){
            nextToken();
            expect(TokenClass.LPAR);
            Expr expr = parseExp();
            expect(TokenClass.RPAR);
            Stmt stmt1 = parseStmt();
            Stmt stmt2 = null;
            if(accept(TokenClass.ELSE)){
                nextToken();
                stmt2 = parseStmt();
            }
            return new If(expr, stmt1, stmt2);
        }else if(accept(TokenClass.RETURN)){
            nextToken();
            if(accept(TokenClass.SC)){
                nextToken();
                return new Return(null);
            }else{
                Expr expr = parseExp();
                expect(TokenClass.SC);
                return new Return(expr);
            }
        }else{
            Expr expr = parseExp();
            expect(TokenClass.SC);
            return new ExprStmt(expr);
        }

    }

    private Expr parseExp() {
        if (accept(TokenClass.LPAR)) {
            // "(" type ")" exp   || "(" exp ")"
            nextToken(); // consume (
            if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)) {
                // ( type ) exp
                parseType();
                expect(TokenClass.RPAR);
                parseExp();
            } else {
                // ( exp )
                parseExp();
                expect(TokenClass.RPAR);
            }


        } else if (accept(TokenClass.IDENTIFIER)) {
            // not consuming here in case of function
            if (lookAhead(1).tokenClass == TokenClass.LPAR) {
                //function call
                parseFuncCall();
            } else {
                // not function so we consume identifier
                nextToken();
            }

        } else if (accept(TokenClass.PLUS, TokenClass.MINUS)) {
            // expecting (+ | - ) exp
            // need to watch out for messing this up with binary expression
            // maybe this is the same as binary expression though
            nextToken();
            parseExp();

        } else if (accept(TokenClass.STRING_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.INT_LITERAL)) {
            //nothing else to do here, terminal state
            nextToken();

        } else if (accept(TokenClass.ASTERIX)) {
            //similar to (+ | -) consume the token and then
            // look for proceeding exp
            nextToken();
            parseExp();

        } else if (accept(TokenClass.AND)) {
            nextToken();
            parseExp();

        } else if (accept(TokenClass.SIZEOF)) {
            //"sizeof" "(" type ")"
            nextToken();
            expect(TokenClass.LPAR);
            parseType();
            expect(TokenClass.RPAR);


        } else {
            //raise errors
            // no expression was reached.
            error();
            nextToken();

        }


        // try this
        parseExpPrime();
        return null;

    }

    private void parseExpPrime(){
        // this ensures that an expression is finished before coming into this statement
        if(accept(TokenClass.ASSIGN)) {
            // deal with assign here
            // problem arises that we require = after exp,
            // solution use parseAssignExp
            parseAssignExp();
        }
        else if(accept(TokenClass.LT,TokenClass.GT,TokenClass.LE,TokenClass.GE,TokenClass.NE,
                TokenClass.EQ,TokenClass.PLUS,TokenClass.MINUS,TokenClass.DIV,TokenClass.ASTERIX,
                TokenClass.REM,TokenClass.LOGOR,TokenClass.LOGAND)){
            nextToken();
            parseExp();
            // exp (">" | "<" | ">=" | "<=" | "!=" | "==" | "+" | "-" | "/" | "*" | "%" | "||" | "&&") exp  # binary operators
        }else if(accept(TokenClass.LSBR)){
            nextToken();
            // deal with array, bin, and field
            //arrayaccess  ::= exp "[" exp "]"                  # array access
            //fieldaccess  ::= exp "." IDENT
            parseExp();
            expect(TokenClass.RSBR);

        }
        // this is an espression so has potential for assign or some other to come after it.
        else if(accept(TokenClass.DOT)){
            nextToken();

            if (lookAhead(1).tokenClass == TokenClass.LPAR) {
                //function call
                parseFuncCall();
            } else {
                // not function so we consume identifier
                nextToken();
            }
            // what kind of stuff can come after a field?
            // IDENT[ exp ]
            // IDENT( ) # function call
            // IDENT =
            // IDENT BinOP
            // Lesson, after

        }else{
            return;
        }

        parseExpPrime();
    }

    private void parseAssignExp(){
        // just to make sure check again
        if(accept(TokenClass.ASSIGN)){
            nextToken();
            parseExp();
        }else{
            error(TokenClass.ASSIGN);
            nextToken();
        }
    }
    private void parseFuncCall(){
        // IDENT "(" [ exp ("," exp)* ] ")"
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        //consume initial [ exp ]
        if(! accept(TokenClass.RPAR)){
            parseExp();
            while(! accept(TokenClass.RPAR, TokenClass.EOF)){
                //  [ ("," exp)* ]
                if(accept(TokenClass.COMMA)){
                    nextToken();
                    parseExp();
                }else{
                    error(TokenClass.COMMA);
                    nextToken();
                }
            }
        }

        //consume the RPAR or raise error is EOF
        expect(TokenClass.RPAR);
        return;

    }


    // to be completed ...
}
