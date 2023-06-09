import ast.ASTPrinter;
import ast.Program;
import lexer.Scanner;
import lexer.Token;
import lexer.Tokeniser;
import parser.Parser;
import sem.SemanticAnalyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


/**
 * The Main file implies an interface for the subsequent components, e.g.
 *   * The Tokeniser must have a constructor which accepts a Scanner,
 *     moreover the Tokeniser must provide a public method getErrorCount
 *     which returns the total number of lexing errors.
 */
public class MainPart2 {
    private static final int FILE_NOT_FOUND = 2;
    private static final int MODE_FAIL      = 254;
    private static final int LEXER_FAIL     = 250;
    private static final int PARSER_FAIL    = 245;
    private static final int SEM_FAIL       = 240;
    private static final int PASS           = 0;
    
    private enum Mode {
        LEXER, PARSER, AST, SEMANTICANALYSIS
    }

    private static void usage() {
        System.out.println("Usage: java "+ Main.class.getSimpleName()+" pass inputfile [outputfile]");
        System.out.println("where pass is either: -lexer, -parser, -ast, -sem");
        System.out.println("if -ast is chosen, the output fill must be specified");
        System.exit(-1);
    }

    public static void main(String[] args) throws FileNotFoundException {

        if (args.length < 2)
            usage();

        Mode mode = null;
        switch (args[0]) {
            case "-lexer":
                mode = Mode.LEXER;
                break;
            case "-parser":
                mode = Mode.PARSER;
                break;
            case "-ast":
                if (args.length < 3)
                    usage();
                mode = Mode.AST;
                break;
            case "-sem":
                mode = Mode.SEMANTICANALYSIS;
                break;
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
        }

        else if (mode == Mode.PARSER) {
            Parser parser = new Parser(tokeniser);
		    parser.parse();
		    if (parser.getErrorCount() == 0)
		    	System.out.println("Parsing: pass");
		    else
		    	System.out.println("Parsing: failed ("+parser.getErrorCount()+" errors)");
		    System.exit(parser.getErrorCount() == 0 ? PASS : PARSER_FAIL);
        }

        else if (mode == Mode.AST) {
            Parser parser = new Parser(tokeniser);
            Program programAst = parser.parse();
            if (parser.getErrorCount() == 0) {
                File outputFile = new File(args[2]);
                PrintWriter writer = new PrintWriter(outputFile);
                new ASTPrinter(writer).visit(programAst);
                writer.close();
            } else
                System.out.println("Parsing: failed (" + parser.getErrorCount() + " errors)");
            System.exit(parser.getErrorCount() == 0 ? PASS : PARSER_FAIL);
        }

        else if (mode == Mode.SEMANTICANALYSIS) {
            Parser parser = new Parser(tokeniser);
            Program programAst = parser.parse();
            if (parser.getErrorCount() == 0) {
                SemanticAnalyzer sem = new SemanticAnalyzer();
                int errors = sem.analyze(programAst);
                if (errors == 0)
                    System.out.println("Semantic analysis: Pass");
                else
                    System.out.println("Semantic analysis: Failed (" + errors + ")");
                System.exit(errors == 0 ? PASS : SEM_FAIL);
            } else
                System.exit(PARSER_FAIL);
        }

        else {
        	System.exit(MODE_FAIL);
        }
    }
}
