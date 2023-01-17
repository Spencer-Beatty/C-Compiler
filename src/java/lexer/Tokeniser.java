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
        while(Character.isLetterOrDigit(c)){
            sb.append(c);
            scanner.next();
            c = scanner.peek();
        }
        return new Token(TokenClass.IDENTIFIER, sb.toString() ,line, colunm);
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
            switch (c) {
                case 'i':
                    // check for int, if, or other
                    sb.append(c);
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

                            if (Character.isLetterOrDigit(c)) {
                                // then this is an identifier
                                return finishIdentifier(line, column, sb);
                            } else {
                                // if there is not another Character or letter or digit, then we return
                                // an int, this allows for stuff like int[] and int
                                return new Token(TokenClass.INT, line, column);
                            }
                        } else {
                            //catches "in" followed by something
                            return finishIdentifier(line, column, sb);
                        }
                    } else if (c == 'f') {
                        // expecting "if" or identifier
                        c = scanner.next();
                        sb.append(c);
                        c = scanner.peek();

                        if (Character.isLetterOrDigit(c)) {
                            return finishIdentifier(line, column, sb);
                        } else {
                            return new Token(TokenClass.IF, line, column);
                        }
                    } else {
                        // catches letter "i" followed by whitespace or symbol
                        // let finishIdentifier deal with problems
                        return finishIdentifier(line, column, sb);
                    }
                case 'v':
                    // check for void, or other
                    sb.append(c);
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
                                if (Character.isLetterOrDigit(c)) {
                                    return finishIdentifier(line, column, sb);
                                } else {
                                    return new Token(TokenClass.VOID, line, column);
                                }
                            } else {
                                return finishIdentifier(line, column, sb);
                            }
                        } else {
                            return finishIdentifier(line, column, sb);
                        }
                    } else {
                        return finishIdentifier(line, column, sb);
                    }
                case 'c':
                    //check for char, or other
                    sb.append(c);
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
                                if (Character.isLetterOrDigit(c)) {
                                    return finishIdentifier(line, column, sb);
                                } else {
                                    return new Token(TokenClass.CHAR, line, column);
                                }
                            } else {
                                return finishIdentifier(line, column, sb);
                            }
                        } else {
                            return finishIdentifier(line, column, sb);
                        }
                    } else {
                        return finishIdentifier(line, column, sb);
                    }
                case 'e':
                    //check for char, or other
                    sb.append(c);
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
                                if (Character.isLetterOrDigit(c)) {
                                    return finishIdentifier(line, column, sb);
                                } else {
                                    return new Token(TokenClass.ELSE, line, column);
                                }
                            } else {
                                return finishIdentifier(line, column, sb);
                            }
                        } else {
                            return finishIdentifier(line, column, sb);
                        }
                    } else {
                        return finishIdentifier(line, column, sb);
                    }
                case 'w':
                    //check for while, or other
                    sb.append(c);
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
                                    if (Character.isLetterOrDigit(c)) {
                                        return finishIdentifier(line, column, sb);
                                    } else {
                                        return new Token(TokenClass.WHILE, line, column);
                                    }
                                } else {
                                    return finishIdentifier(line, column, sb);
                                }
                            } else {
                                return finishIdentifier(line, column, sb);
                            }
                        } else {
                            return finishIdentifier(line, column, sb);
                        }
                    } else {
                        return finishIdentifier(line, column, sb);
                    }
                case 'r':
                    //check for return, or other
                    sb.append(c);
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
                                        if (Character.isLetterOrDigit(c)) {
                                            return finishIdentifier(line, column, sb);
                                        } else {
                                            return new Token(TokenClass.RETURN, line, column);
                                        }
                                    } else {
                                        return finishIdentifier(line, column, sb);
                                    }
                                } else {
                                    return finishIdentifier(line, column, sb);
                                }
                            } else {
                                return finishIdentifier(line, column, sb);
                            }
                        } else {
                            return finishIdentifier(line, column, sb);
                        }
                    } else {
                        return finishIdentifier(line, column, sb);
                    }
                case 's':
                    //check for struct sizeof, or other
                    sb.append(c);
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
                                        if (Character.isLetterOrDigit(c)) {
                                            return finishIdentifier(line, column, sb);
                                        } else {
                                            return new Token(TokenClass.STRUCT, line, column);
                                        }
                                    } else {
                                        return finishIdentifier(line, column, sb);
                                    }
                                } else {
                                    return finishIdentifier(line, column, sb);
                                }
                            } else {
                                return finishIdentifier(line, column, sb);
                            }
                        } else {
                            return finishIdentifier(line, column, sb);
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
                                        if (Character.isLetterOrDigit(c)) {
                                            return finishIdentifier(line, column, sb);
                                        } else {
                                            return new Token(TokenClass.STRUCT, line, column);
                                        }
                                    } else {
                                        return finishIdentifier(line, column, sb);
                                    }
                                } else {
                                    return finishIdentifier(line, column, sb);
                                }
                            } else {
                                return finishIdentifier(line, column, sb);
                            }
                        } else {
                            return finishIdentifier(line, column, sb);
                        }
                    }else{
                        return finishIdentifier(line, column, sb);
                    }

                default:
                    sb.append(c);
                    return finishIdentifier(line, column, sb);


            }

        }
        // ... to be completed


        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }


}
