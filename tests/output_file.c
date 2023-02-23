Program(VarDecl(ArrayType(ArrayType(INT,1),2),a),FunDecl(VOID,main,
	Block(ExprStmt(Assign(ArrayAccessExpr(ArrayAccessExpr(VarExpr(a),IntLiteral(1)),BinOp(IntLiteral(2),ADD,IntLiteral(2))),IntLiteral(1))),
		ExprStmt(ArrayAccessExpr(ArrayAccessExpr(ArrayAccessExpr(VarExpr(a),IntLiteral(100)),Assign(VarExpr(a),VarExpr(b))),VarExpr(c)))
	)))