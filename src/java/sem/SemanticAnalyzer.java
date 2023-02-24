package sem;

public class SemanticAnalyzer {
	
	public int analyze(ast.Program prog) {

		int errors = 0;

		NameAnalyzer na = new NameAnalyzer();
		na.visit(prog);
		errors += na.getErrorCount();

		ReturnCheckAnalyzer rc = new ReturnCheckAnalyzer();
		rc.visit(prog);
		errors += rc.getErrorCount();

		StructFieldAnalyzer st = new StructFieldAnalyzer();
		st.visit(prog);
		errors += st.getErrorCount();

		TypeAnalyzer tc = new TypeAnalyzer();
		tc.visit(prog);
		errors += tc.getErrorCount();

		LeftValueAnalyzer lc = new LeftValueAnalyzer();
		lc.visit(prog);
		errors += lc.getErrorCount();

		UniqueStructAnalyzer uc = new UniqueStructAnalyzer();
		uc.visit(prog);
		errors += uc.getErrorCount();
		// To complete

		// Return the number of errors.
		return errors;
	}
}
