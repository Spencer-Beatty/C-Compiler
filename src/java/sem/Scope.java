package sem;

import ast.*;
import lexer.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Scope {
	private Scope outer;
	private Map<String, Symbol> symbolTable;
	
	public Scope(Scope outer) { 
		this.outer = outer;
		symbolTable = new HashMap<String, Symbol>();
	}
	
	public Scope() {
		this(null);
		symbolTable = new HashMap<String, Symbol>();

		// when outer scope is created add global functions
		//
		/*
		void print_s(char* s);
		void print_i(int i);
		void print_c(char c);
		char read_c();
		int read_i();
		void* mcmalloc(int size);
		 */
		VarDecl s = new VarDecl((new PointerType(BaseType.CHAR, 1)), "s");
		VarDecl i = new VarDecl(BaseType.INT, "i");
		VarDecl c = new VarDecl(BaseType.CHAR, "c");
		VarDecl size = new VarDecl(BaseType.INT, "size");
		Block block = new Block(new ArrayList<VarDecl>(), new ArrayList<Stmt>()); //  empty block
		ArrayList<VarDecl> params1 = new ArrayList<VarDecl>();
		ArrayList<VarDecl> params2 = new ArrayList<VarDecl>();
		ArrayList<VarDecl> params3 = new ArrayList<VarDecl>();
		ArrayList<VarDecl> params4 = new ArrayList<VarDecl>();
		params1.add(s);
		FunSymbol print_s = new FunSymbol(
				new FunDecl(BaseType.VOID, "print_s",params1,block));

		params2.add(i);
		FunSymbol print_i = new FunSymbol(
				new FunDecl(BaseType.VOID, "print_i", params2, block));

		params3.add(c);
		FunSymbol print_c = new FunSymbol(
				new FunDecl(BaseType.VOID, "print_c", params3, block));

		// next two functions take no params


		FunSymbol read_c = new FunSymbol(
				new FunDecl(BaseType.CHAR, "read_c",  new ArrayList<VarDecl>(), block));
		FunSymbol read_i = new FunSymbol(
				new FunDecl(BaseType.INT, "read_i",  new ArrayList<VarDecl>(), block));
		// add back into params for mcmalloc
		params4.add(size);
		FunSymbol mcmalloc = new FunSymbol(
				new FunDecl(new PointerType(BaseType.VOID, 1),"mcmalloc", params4, block));

		symbolTable.put(print_s.name, print_s);
		symbolTable.put(print_i.name, print_i);
		symbolTable.put(print_c.name, print_c);
		symbolTable.put(read_c.name, read_c);
		symbolTable.put(read_i.name, read_i);
		symbolTable.put(mcmalloc.name, mcmalloc);

		// table set up
		// approprate place to be set up here, as long as only null argument call of
		// Symbol constructor is from the Program initialization.




	}
	
	public Symbol lookup(String name) {
		// move out until encounter or finds outer == null
		// To be completed...
		if(lookupCurrent(name) != null){
			return lookupCurrent(name);
		}else if(outer != null){
			return outer.lookup(name);
		}else{
			return null;
		}

	}

	// declaration should make sure lookupCurrent is null,
	// calling should get lookup current.
	//
	public Symbol lookupCurrent(String name) {
		if(symbolTable.get(name) == null){
			// no mapping for this key
			return null;
		}else{
			return symbolTable.get(name);
		}
		// lookup in scope
		// To be completed...
	}
	
	public void put(Symbol sym) {
		symbolTable.put(sym.name, sym);
	}
}
