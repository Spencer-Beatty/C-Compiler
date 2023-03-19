import ast.ASTPrinter;
import ast.Program;
import gen.CodeGenerator;
import gen.asm.AssemblyParser;
import gen.asm.AssemblyProgram;
import lexer.Scanner;
import lexer.Token;
import lexer.Tokeniser;
import parser.Parser;
import gen.asm.AssemblyPass;
import regalloc.GraphColouringRegAlloc;
import regalloc.NaiveRegAlloc;
import sem.SemanticAnalyzer;

import java.io.*;


/**
 * This is the entry point to the compiler. This files should not be modified.
 */
public class Main {
    private static final int FILE_NOT_FOUND = 2;
    private static final int IO_EXCEPTION   = 3;
    private static final int MODE_FAIL      = 254;
    private static final int LEXER_FAIL     = 250;
    private static final int PARSER_FAIL    = 245;
    private static final int SEM_FAIL       = 240;
    private static final int PASS           = 0;
    
    private enum Mode {
        LEXER, PARSER, AST, SEMANTICANALYSIS, GEN, REGALLOC
    }

    private enum RegAllocMode {
        NONE, NAIVE, GRAPH_COLOURING
    }

    private static void usage() {
        System.out.println("Usage: java "+ Main.class.getSimpleName()+" pass inputfile [outputfile]");
        System.out.println("where pass is either: -lexer, -parser, -ast, -sem, -gen [naive|colour], -regalloc naive|colour");
        System.out.println("if -ast, -gen or -regalloc is chosen, the output file must be specified");
        System.exit(-1);
    }

    private static void ensureArgExists(String[] args, int num) {
        if (num >= args.length)
            usage();
    }

    public static void main(String[] args) throws FileNotFoundException {

        ensureArgExists(args, 0);

        Mode mode = null;
        RegAllocMode regAllocMode = RegAllocMode.NONE;
        int curArgCnt = 0;
        switch (args[curArgCnt]) {
            case "-lexer":
                mode = Mode.LEXER;
                curArgCnt++;
                break;
            case "-parser":
                mode = Mode.PARSER;
                curArgCnt++;
                break;
            case "-ast":
                if (args.length < 3)
                    usage();
                mode = Mode.AST;
                curArgCnt++;
                break;
            case "-sem":
                mode = Mode.SEMANTICANALYSIS;
                curArgCnt++;
                break;
            case "-gen":
                mode = Mode.GEN;
                curArgCnt++;
                ensureArgExists(args, curArgCnt);
                if (args[curArgCnt].equals("naive")) {
                    regAllocMode = RegAllocMode.NAIVE;
                    curArgCnt++;
                } else if (args[curArgCnt].equals("colour")) {
                    regAllocMode = RegAllocMode.GRAPH_COLOURING;
                    curArgCnt++;
                }
                break;
            case "-regalloc":
                mode = Mode.REGALLOC;
                curArgCnt++;
                ensureArgExists(args, curArgCnt);
                if (args[curArgCnt].equals("naive")) {
                    regAllocMode = RegAllocMode.NAIVE;
                    curArgCnt++;
                } else if (args[curArgCnt].equals("colour")) {
                    regAllocMode = RegAllocMode.GRAPH_COLOURING;
                    curArgCnt++;
                }
                break;
            default:
                usage();
                break;
        }

        ensureArgExists(args, curArgCnt);
        File inputFile = new File(args[curArgCnt]);
        curArgCnt++;

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
            ensureArgExists(args, curArgCnt);
            File outputFile = new File(args[curArgCnt]);
            curArgCnt++;

            Parser parser = new Parser(tokeniser);
            Program programAst = parser.parse();
            if (parser.getErrorCount() == 0) {
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

        else if (mode == Mode.GEN) {
            ensureArgExists(args, curArgCnt);
            File outputFile = new File(args[curArgCnt]);
            curArgCnt++;

            Parser parser = new Parser(tokeniser);
            Program programAst = parser.parse();
            if (parser.getErrorCount() > 0)
                System.exit(PARSER_FAIL);
            SemanticAnalyzer sem = new SemanticAnalyzer();
            int errors = sem.analyze(programAst);
            if (errors > 0)
                System.exit(SEM_FAIL);

            AssemblyPass regAlloc = AssemblyPass.NOP;
            switch(regAllocMode) {
                case NONE:
                    regAlloc = AssemblyPass.NOP;
                    break;
                case NAIVE:
                    regAlloc = NaiveRegAlloc.INSTANCE;
                    break;
                case GRAPH_COLOURING:
                    regAlloc = GraphColouringRegAlloc.INSTANCE;
                    break;
            }
            CodeGenerator codegen = new CodeGenerator(regAlloc);
            try {
                codegen.emitProgram(programAst, outputFile);
            } catch (FileNotFoundException e) {
                System.out.println("File "+outputFile.toString()+" does not exist.");
                System.exit(FILE_NOT_FOUND);
            }
        }

        else if (mode == Mode.REGALLOC) {
            ensureArgExists(args, curArgCnt);
            File outputFile = new File(args[curArgCnt]);
            curArgCnt++;

            AssemblyPass regAlloc = AssemblyPass.NOP;
            switch (regAllocMode) {
                case NONE:
                    regAlloc = AssemblyPass.NOP;
                    break;
                case NAIVE:
                    regAlloc = NaiveRegAlloc.INSTANCE;
                    break;
                case GRAPH_COLOURING:
                    regAlloc = GraphColouringRegAlloc.INSTANCE;
                    break;
            }

            AssemblyProgram program;
            try {
                var reader = new FileReader(inputFile);
                program = AssemblyParser.readAssemblyProgram(new BufferedReader(reader));
                reader.close();
            } catch (FileNotFoundException e) {
                System.out.println("File " + inputFile + " does not exist.");
                System.exit(FILE_NOT_FOUND);
                return;
            } catch (IOException e) {
                System.out.println("An I/O exception occurred when reading " + inputFile + ".");
                System.exit(IO_EXCEPTION);
                return;
            }

            var programWithoutVRegs = regAlloc.apply(program);

            PrintWriter writer;
            try {
                writer = new PrintWriter(outputFile);
            } catch (FileNotFoundException e) {
                System.out.println("Cannot write to output file " + outputFile + ".");
                System.exit(FILE_NOT_FOUND);
                return;
            }
            programWithoutVRegs.print(writer);
            writer.close();

        }

        else {
        	System.exit(MODE_FAIL);
        }
    }
}
