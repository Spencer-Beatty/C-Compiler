package lexer;

import lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;

/**
 * @author cdubach
 * @author sbeatt4
 */
public class Tokeniser {

    private Scanner scanner;

    private int error = 0;
    public int getErrorCount() {
	return this.error;
    }

    public Tokeniser(Scanner scanner) {
        this.scanner = scanner;
    }

    private void error(char c, int line, int col) {
        System.out.println("Lexing error: unrecognised character ("+c+") at "+line+":"+col);
	error++;
    }


    public Token nextToken() {
        Token result;
        try {
             result = next();
        } catch (EOFException eof) {
            // end of file, nothing to worry about, just return EOF token
            return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            // something went horribly wrong, abort
            System.exit(-1);
            return null;
        }
        return result;
    }

    private Token finishIdentifier(int line, int colunm, StringBuilder sb) throws IOException {
        // finish identifier is going to have to check for character following identifier is correct,
        char c = scanner.peek();
        while(Character.isLetterOrDigit(c) || c=='_'){
            sb.append(c);
            scanner.next();
            c = scanner.peek();
        }
        return new Token(TokenClass.IDENTIFIER, sb.toString() ,line, colunm);
    }


    private char validEscapeCharacter(char c) {
        switch (c) {
            case 't':
                return '\t';
            //insert tab here
            case 'b':
                return '\b';
            //insert backspace here
            case 'n':
                return '\n';
            //insert new line here
            case 'r':
                return '\r';
            //insert carriage return here
            case 'f':
                return '\f';
            //insert a form feed here
            case '\'':
                return '\'';
            //insert a apostrophe here
            case '"':
                return '\"';
            //insert a quotation
            case '\\':
                return '\\';
            //insert a backslash
            case '0':
                return '\0';
        }
        return '1';
    }
    /*
     * To be completed
     */
    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        // recognises the plus operator
        if (c == '+')
            return new Token(TokenClass.PLUS, line, column);

        // start for Identifiers, types, keywords
        if (Character.isLetter(c)){
            //types: int, void, char
            //keywords: if, else, while, return,
            // struct, sizeof
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            switch (c) {
                case 'i':
                    // check for int, if, or other
                    c = scanner.peek();

                    if (c == 'n') {
                        // expecting "int" or identifier
                        c = scanner.next();
                        sb.append(c);
                        c = scanner.peek();

                        if (c == 't') {
                            c = scanner.next();
                            sb.append(c);
                            c = scanner.peek();

                            if (! Character.isLetterOrDigit(c)) {
                                // if there is not another Character or letter or digit, then we return
                                // an int, this allows for stuff like int[] and int
                                return new Token(TokenClass.INT, line, column);
                            }
                        }
                    } else if (c == 'f') {
                        // expecting "if" or identifier
                        c = scanner.next();
                        sb.append(c);
                        c = scanner.peek();

                        if (! Character.isLetterOrDigit(c)) {
                            return new Token(TokenClass.IF, line, column);
                        }
                    }
                    //if nothing encountered call finish identifier
                    return finishIdentifier(line, column, sb);
                case 'v':
                    // check for void, or other
                    c = scanner.peek();
                    if (c == 'o') {
                        c = scanner.next();
                        sb.append(c);
                        c = scanner.peek();
                        if (c == 'i') {
                            c = scanner.next();
                            sb.append(c);
                            c = scanner.peek();
                            if (c == 'd') {
                                c = scanner.next();
                                sb.append(c);
                                c = scanner.peek();
                                if (! Character.isLetterOrDigit(c)) {
                                    return new Token(TokenClass.VOID, line, column);
                                }
                            }
                        }
                    }

                    return finishIdentifier(line, column, sb);
                case 'c':
                    //check for char, or other
                    c = scanner.peek();
                    if (c == 'h') {
                        c = scanner.next();
                        sb.append(c);
                        c = scanner.peek();
                        if (c == 'a') {
                            c = scanner.next();
                            sb.append(c);
                            c = scanner.peek();
                            if (c == 'r') {
                                c = scanner.next();
                                sb.append(c);
                                c = scanner.peek();
                                if (! Character.isLetterOrDigit(c)) {
                                    return new Token(TokenClass.CHAR, line, column);
                                }
                            }
                        }
                    }
                    return finishIdentifier(line, column, sb);
                case 'e':
                    //check for char, or other
                    c = scanner.peek();
                    if (c == 'l') {
                        c = scanner.next();
                        sb.append(c);
                        c = scanner.peek();
                        if (c == 's') {
                            c = scanner.next();
                            sb.append(c);
                            c = scanner.peek();
                            if (c == 'e') {
                                c = scanner.next();
                                sb.append(c);
                                c = scanner.peek();
                                if (! Character.isLetterOrDigit(c)) {
                                    return new Token(TokenClass.ELSE, line, column);
                                }
                            }
                        }
                    }
                    return finishIdentifier(line, column, sb);

                case 'w':
                    //check for while, or other
                    c = scanner.peek();
                    if (c == 'h') {
                        c = scanner.next();
                        sb.append(c);
                        c = scanner.peek();
                        if (c == 'i') {
                            c = scanner.next();
                            sb.append(c);
                            c = scanner.peek();
                            if (c == 'l') {
                                c = scanner.next();
                                sb.append(c);
                                c = scanner.peek();
                                if (c == 'e') {
                                    c = scanner.next();
                                    sb.append(c);
                                    c = scanner.peek();
                                    if (! Character.isLetterOrDigit(c)) {
                                        return new Token(TokenClass.WHILE, line, column);
                                    }
                                }
                            }
                        }
                    }
                    return finishIdentifier(line, column, sb);

                case 'r':
                    //check for return, or other
                    c = scanner.peek();
                    if (c == 'e') {
                        c = scanner.next();
                        sb.append(c);
                        c = scanner.peek();
                        if (c == 't') {
                            c = scanner.next();
                            sb.append(c);
                            c = scanner.peek();
                            if (c == 'u') {
                                c = scanner.next();
                                sb.append(c);
                                c = scanner.peek();
                                if (c == 'r') {
                                    c = scanner.next();
                                    sb.append(c);
                                    c = scanner.peek();
                                    if (c == 'n') {
                                        c = scanner.next();
                                        sb.append(c);
                                        c = scanner.peek();
                                        if (! Character.isLetterOrDigit(c)) {
                                            return new Token(TokenClass.RETURN, line, column);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return finishIdentifier(line, column, sb);
                case 's':
                    //check for struct sizeof, or other
                    c = scanner.peek();
                    if (c == 't') {
                        c = scanner.next();
                        sb.append(c);
                        c = scanner.peek();
                        if (c == 'r') {
                            c = scanner.next();
                            sb.append(c);
                            c = scanner.peek();
                            if (c == 'u') {
                                c = scanner.next();
                                sb.append(c);
                                c = scanner.peek();
                                if (c == 'c') {
                                    c = scanner.next();
                                    sb.append(c);
                                    c = scanner.peek();
                                    if (c == 't') {
                                        c = scanner.next();
                                        sb.append(c);
                                        c = scanner.peek();
                                        if (! Character.isLetterOrDigit(c)) {
                                            return new Token(TokenClass.STRUCT, line, column);
                                        }
                                    }
                                }
                            }
                        }
                    } else if (c == 'i') {
                        // check for sizeof
                        c = scanner.next();
                        sb.append(c);
                        c = scanner.peek();
                        if (c == 'z') {
                            c = scanner.next();
                            sb.append(c);
                            c = scanner.peek();
                            if (c == 'e') {
                                c = scanner.next();
                                sb.append(c);
                                c = scanner.peek();
                                if (c == 'o') {
                                    c = scanner.next();
                                    sb.append(c);
                                    c = scanner.peek();
                                    if (c == 'f') {
                                        c = scanner.next();
                                        sb.append(c);
                                        c = scanner.peek();
                                        if (!Character.isLetterOrDigit(c)) {
                                            return new Token(TokenClass.STRUCT, line, column);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return finishIdentifier(line, column, sb);


                default:

                    return finishIdentifier(line, column, sb);


            }

        }

        // deals with int literals, note figure out if 001 should be accepted
        if (Character.isDigit(c)){
            StringBuilder sb = new StringBuilder();
            sb.append(c);
            c = scanner.peek();
            while(Character.isDigit(c)){
                c = scanner.next();
                sb.append(c);
            }
            return new Token(TokenClass.INT_LITERAL,sb.toString(), line, column);
        }
        //start for symbols
        // will deal with:
        // = ( ) { } [ ] ; ,
        // && ||
        // == != < <= > >=
        // - * / % &
        // .
        // #include
        // /* and //
        switch(c){
            case '=':
                c = scanner.peek();
                if(c == '='){
                    scanner.next();
                    return new Token(TokenClass.EQ, line, column);
                }else{
                    return new Token(TokenClass.ASSIGN, line, column);
                }
            case '(':
                return new Token(TokenClass.LPAR, line, column);
            case ')':
                return new Token(TokenClass.RPAR, line, column);
            case '{':
                return new Token(TokenClass.LBRA, line, column);
            case '}':
                return new Token(TokenClass.RBRA, line, column);
            case '[':
                return new Token(TokenClass.LSBR, line, column);
            case ']':
                return new Token(TokenClass.RSBR, line, column);
            case ';':
                return new Token(TokenClass.SC, line, column);
            case ',':
                return new Token(TokenClass.COMMA, line, column);
            case '&':
                c = scanner.peek();
                if(c == '&'){
                    scanner.next();
                    return new Token(TokenClass.LOGAND, line, column);
                }
                return new Token(TokenClass.AND,line,column);
            case '|':
                c = scanner.peek();
                if(c == '|'){
                    scanner.next();
                    return new Token(TokenClass.LOGOR, line, column);
                }
                break;
            case '!':
                c = scanner.peek();
                if(c == '='){
                    scanner.next();
                    return new Token(TokenClass.NE, line, column);
                }
                break;
            case '<':
                c = scanner.peek();
                if(c == '='){
                    scanner.next();
                    return new Token(TokenClass.LE, line, column);
                }
                return new Token(TokenClass.LT, line, column);
            case '>':
                c = scanner.peek();
                if(c == '='){
                    scanner.next();
                    return new Token(TokenClass.GE, line, column);
                }
                return new Token(TokenClass.GT, line, column);
            case '-':
                return new Token(TokenClass.MINUS, line, column);
            case '*':
                return new Token(TokenClass.ASTERIX,line,column);
            case '/':
                c = scanner.peek();
                if(c == '/'){
                    // line comment
                    while(scanner.getLine() == line && scanner.hasNext()){
                        scanner.next();
                    }
                    return next();
                }else if(c == '*'){
                    //block comment start, search for end
                    while(scanner.hasNext()){
                            c = scanner.next();
                            if (c == '*') {
                                c = scanner.next();
                                if (c == '/') {
                                    scanner.next();
                                    return next();
                                }
                            }
                    }
                    break;
                }else{
                    //division
                    return new Token(TokenClass.DIV,line,column);
                }
            case '.':
                return new Token(TokenClass.DOT,line,column);
            case '%':
                return new Token(TokenClass.REM,line,column);
            case '#':
                c = scanner.next();
                if (c == 'i') {
                    c = scanner.next();
                    if (c == 'n') {
                        c = scanner.next();
                        if (c == 'c') {
                            c = scanner.next();
                            if (c == 'l') {
                                c = scanner.next();
                                if (c == 'u') {
                                    c = scanner.next();
                                    if (c == 'd') {
                                        c = scanner.next();
                                        if (c == 'e') {
                                            c = scanner.peek();
                                            if (Character.isWhitespace(c)) {
                                                return new Token(TokenClass.INCLUDE, line, column);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }

        //string literals here
        if (c == '\"'){
            StringBuilder sb = new StringBuilder();
            c = scanner.next();
            while(c != '\"' && scanner.hasNext()){
                if(c=='\\'){
                    c = scanner.next();
                    if(validEscapeCharacter(c) == '1'){
                        error('\\',line,column);
                        error(c, line, column);
                    }else{
                        sb.append(validEscapeCharacter(c));

                    }
                }else{
                    sb.append(c);
                }

                c = scanner.next();
            }
            if(c!='\"'){
                return new Token(TokenClass.INVALID, line, column);
            }
            return new Token(TokenClass.STRING_LITERAL,sb.toString(), line, column);

        }

        //char literals here
        if (c == '\''){
            StringBuilder sb = new StringBuilder();
            c = scanner.next();
            if(c == '\\'){
                //check for valid escape character
                c = scanner.next();
                if(validEscapeCharacter(c) == '1'){
                    error('\\',line,column);
                    error(c, line, column);
                }else if(scanner.next() == '\''){
                        sb.append(validEscapeCharacter(c));
                        return new Token(TokenClass.CHAR_LITERAL, sb.toString(), line, column);
                }
            }else if(Character.isDefined(c)){
                sb.append(c);
                if(scanner.next() == '\''){
                    return new Token(TokenClass.CHAR_LITERAL, sb.toString(), line, column);
                }
            }
            error(c, line, column);
            c = scanner.next();
            while(c!='\'' && scanner.hasNext()){
                c = scanner.next();
            }
            return new Token(TokenClass.INVALID, line, column);

        }


        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }


}
