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
            }
            expect(TokenClass.RBRA);
            return;
            // to be completed ...
        }
    }

    // type IDENT "(" params ")" block
    private void parseFunDecl(){
        parseType();
        expect(TokenClass.IDENTIFIER);
        expect(TokenClass.LPAR);
        parseExp();
        expect(TokenClass.RPAR);
        if(accept(TokenClass.LBRA)){
            parseBlock();
        }else{
            error(TokenClass.LBRA);
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

            while(! accept(TokenClass.RBRA)){
                parseStmt();
            }

            // if we reach here we have found the '}'
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
                expect(TokenClass.LPAR);
                parseExp();
                expect(TokenClass.RPAR);
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
        return;
    }
    // to be completed ...
}
