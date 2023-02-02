package parser;


import lexer.Token;
import lexer.Token.TokenClass;
import lexer.Tokeniser;

import java.util.LinkedList;
import java.util.Queue;


/**
 * @author cdubach
 * @author spencerbeatty 
 */
public class Parser {

    private Token token;

    private Queue<Token> buffer = new LinkedList<>();

    private final Tokeniser tokeniser;



    public Parser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }

    public void parse() {
        // get the first token
        nextToken();

        parseProgram();
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


    private void parseProgram() {
        // (Include)*
        parseIncludes();

        // (structdecl | vardecl | fundecl)*
        while (accept(TokenClass.STRUCT, TokenClass.INT, TokenClass.CHAR, TokenClass.VOID)) {
            // structdecl
            if (token.tokenClass == TokenClass.STRUCT &&
                    lookAhead(1).tokenClass == TokenClass.IDENTIFIER &&
                    lookAhead(2).tokenClass == TokenClass.LBRA) {
                parseStructDecl();

            }
            // fundecl where type != struct
            else if(lookAhead(1).tokenClass == TokenClass.IDENTIFIER &&
                    lookAhead(2).tokenClass == TokenClass.LPAR) {
                parseFunDecl();
            // fundecl where type = structtype (struct ident) followed by ident
            }else if(token.tokenClass==TokenClass.STRUCT &&
                    lookAhead(1).tokenClass == TokenClass.IDENTIFIER &&
                    lookAhead(2).tokenClass == TokenClass.IDENTIFIER) {
                parseFunDecl();
            // vardecl
            }else{
                parseVarDecl();
            }
        }

        // to be completed ...


        // EOF
        expect(TokenClass.EOF);
    }

    // includes are ignored, so does not need to return an AST node
    private void parseIncludes() {
        if (accept(TokenClass.INCLUDE)) {
            nextToken();
            expect(TokenClass.STRING_LITERAL);
            parseIncludes();
        }
    }

    private void parseStructType(){
        // checks struct twice, but is ok because accept does not consume
        // and will prevent calling parse struct type at the wrong time.
        if(accept(TokenClass.STRUCT)) {
            nextToken();
            expect(TokenClass.IDENTIFIER);

        }else{
            error(TokenClass.STRUCT);
            nextToken();
        }
        return;

    }

    private void parseStructDecl(){
        if(accept(TokenClass.STRUCT)){
            parseStructType();

            expect(TokenClass.LBRA);
            // at least 1 var decl
            if(accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT)){
                parseVarDecl();
                while(accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT)){
                    parseVarDecl();
                }
            }else{
                error(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT);
                nextToken();
            }
            expect(TokenClass.RBRA);
            expect(TokenClass.SC);
            return;
            // to be completed ...
        }
    }

    // type IDENT "(" params ")" block
    private void parseFunDecl(){
        parseType();
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        parseParams();
        expect(TokenClass.RPAR);
        if(accept(TokenClass.LBRA)){
            parseBlock();
        }else{
            error(TokenClass.LBRA);
            nextToken();
        }
        return;

        
    }

    private void parseParams(){
        if (accept(TokenClass.RPAR)){
            // consume of RPAR dealt with after returning
            return;
        }else{
            parseType();
            expect(TokenClass.IDENTIFIER);
        }
        while(! accept(TokenClass.RPAR) ){
            expect(TokenClass.COMMA);
            parseType();
            expect(TokenClass.IDENTIFIER);
        }
        return;
    }

    private void parseVarDecl(){

        parseType();
        expect(TokenClass.IDENTIFIER);
        while(accept(TokenClass.LSBR)){
            // expect [ INT_LITERAL ]
            nextToken();
            expect(TokenClass.INT_LITERAL);
            expect(TokenClass.RSBR);
        }
        expect(TokenClass.SC);
        return;

    }

    private void parseType(){
        if(accept(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID)){
            nextToken();
        }else if(accept(TokenClass.STRUCT)){
            parseStructType();
        }
        else{
            error(TokenClass.INT,TokenClass.CHAR,TokenClass.VOID,TokenClass.STRUCT);
            nextToken(); // need to consusme otherwise get stuck in loop potentially
            return;
        }
        while(accept(TokenClass.ASTERIX)){
            nextToken();
        }
        return;
    }

    private void parseBlock(){
        // "{" (vardecl)* (stmt)* "}"
        if(accept(TokenClass.LBRA)) {
            nextToken();
            // if starts with type then must be a var decl
            while (accept(TokenClass.INT,TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)) {
                parseVarDecl();
            }

            while(! accept(TokenClass.RBRA,TokenClass.EOF)){
                // need to make sure that parseStmt always consumes a character
                parseStmt();
            }

            // if we reach here we have found the '}' or EOF
            expect(TokenClass.RBRA);
            return;
        }
    }

    private void parseStmt(){
        if(accept(TokenClass.LBRA)){
            parseBlock();
        }else if(accept(TokenClass.WHILE)){
            nextToken();
            expect(TokenClass.LPAR);
            parseExp();
            expect(TokenClass.RPAR);
            parseStmt();
        }else if(accept(TokenClass.IF)){
            nextToken();
            expect(TokenClass.LPAR);
            parseExp();
            expect(TokenClass.RPAR);
            parseStmt();
            if(accept(TokenClass.ELSE)){
                nextToken();
                parseStmt();
            }
        }else if(accept(TokenClass.RETURN)){
            nextToken();
            if(accept(TokenClass.SC)){
                nextToken();
            }else{
                parseExp();
                expect(TokenClass.SC);
            }
        }else{
            parseExp();
            expect(TokenClass.SC);
        }
        return;
    }

    private void parseExp(){
        if(accept(TokenClass.LPAR)){
            // "(" type ")" exp   || "(" exp ")"
            nextToken(); // consume (
            if(accept(TokenClass.INT,TokenClass.CHAR, TokenClass.VOID, TokenClass.STRUCT)){
                // ( type ) exp
                parseType();
                expect(TokenClass.RPAR);
                parseExp();
            }else{
                // ( exp )
                parseExp();
                expect(TokenClass.RPAR);
            }


        }else if(accept(TokenClass.IDENTIFIER)){
            // not consuming here in case of function
            if(lookAhead(1).tokenClass == TokenClass.LPAR){
                //function call
                parseFuncCall();
            }else{
                // not function so we consume identifier
                nextToken();
            }

        }else if(accept(TokenClass.PLUS, TokenClass.MINUS)){
            // expecting (+ | - ) exp
            // need to watch out for messing this up with binary expression
            // maybe this is the same as binary expression though
            nextToken();
            parseExp();

        }else if(accept(TokenClass.STRING_LITERAL,TokenClass.CHAR_LITERAL,TokenClass.INT_LITERAL)){
            //nothing else to do here, terminal state
            nextToken();

        }else if(accept(TokenClass.ASTERIX)){
            //similar to (+ | -) consume the token and then
            // look for proceeding exp
            nextToken();
            parseExp();

        }else if(accept(TokenClass.AND)){
            nextToken();
            parseExp();

        }else if(accept(TokenClass.SIZEOF)){
            //"sizeof" "(" type ")"
            nextToken();
            expect(TokenClass.LPAR);
            parseType();
            expect(TokenClass.RPAR);


        }else{
            //raise errors
            // no expression was reached.
            error();
            nextToken();

        }

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

        }else if(accept(TokenClass.DOT)){
            nextToken();
            expect(TokenClass.IDENTIFIER);
        }

        return;
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

        //consume the LPAR or raise error is EOF
        expect(TokenClass.RPAR);
        return;

    }
    // to be completed ...
}
