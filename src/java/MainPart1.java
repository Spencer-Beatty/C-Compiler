import lexer.Scanner;
import lexer.Token;
import lexer.Tokeniser;
import parser.Parser;

import java.io.File;
import java.io.FileNotFoundException;


/**
 * The Main file implies an interface for the subsequent components, e.g.
 *   * The Tokeniser must have a constructor which accepts a Scanner,
 *     moreover the Tokeniser must provide a public method getErrorCount
 *     which returns the total number of lexing errors.
 */
public class MainPart1 {
	private static final int FILE_NOT_FOUND = 2;
    private static final int MODE_FAIL      = 254;
    private static final int LEXER_FAIL     = 250;
    private static final int PARSER_FAIL    = 245;
    private static final int PASS           = 0;
    
    private enum Mode {
        LEXER, PARSER
    }

    private static void usage() {
        System.out.println("Usage: java "+ MainPart1.class.getSimpleName()+" pass inputfile");
        System.out.println("where pass is either: -lexer, -parser");
        System.exit(-1);
    }

    public static void main(String[] args) {

        if (args.length != 2)
            usage();

        Mode mode = null;
        switch (args[0]) {
            case "-lexer":  mode = Mode.LEXER; break;
            case "-parser": mode = Mode.PARSER; break;
            default:
                usage();
                break;
        }

        File inputFile = new File(args[1]);

        Scanner scanner;
        try {
            scanner = new Scanner(inputFile);
        } catch (FileNotFoundException e) {
            System.out.println("File "+inputFile+" does not exist.");
            System.exit(FILE_NOT_FOUND);
            return;
        }

        Tokeniser tokeniser = new Tokeniser(scanner);
        if (mode == Mode.LEXER) {
            for (Token t = tokeniser.nextToken(); t.tokenClass != Token.TokenClass.EOF; t = tokeniser.nextToken()) 
            	System.out.println(t);
            if (tokeniser.getErrorCount() == 0)
        		System.out.println("Lexing: pass");
    	    else
        		System.out.println("Lexing: failed ("+tokeniser.getErrorCount()+" errors)");	
            System.exit(tokeniser.getErrorCount() == 0 ? PASS : LEXER_FAIL);
        } else if (mode == Mode.PARSER) {
            Parser parser = new Parser(tokeniser);
		    parser.parse();
		    if (parser.getErrorCount() == 0)
		    	System.out.println("Parsing: pass");
		    else
		    	System.out.println("Parsing: failed ("+parser.getErrorCount()+" errors)");
		    System.exit(parser.getErrorCount() == 0 ? PASS : PARSER_FAIL);
        } else {
        	System.exit(MODE_FAIL);
        }
    }
}