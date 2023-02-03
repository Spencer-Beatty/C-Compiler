package sem;

public class SemanticAnalyzer {
	
	public int analyze(ast.Program prog) {

		int errors = 0;

		NameAnalyzer na = new NameAnalyzer();
		na.visit(prog);
		errors += na.getErrorCount();

		TypeAnalyzer tc = new TypeAnalyzer();
		tc.visit(prog);
		errors += tc.getErrorCount();

		// To complete

		// Return the number of errors.
		return errors;
	}
}
