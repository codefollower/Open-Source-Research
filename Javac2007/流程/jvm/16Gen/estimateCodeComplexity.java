    /** Very roughly estimate the number of instructions needed for
     *  the given tree.
     */
    int estimateCodeComplexity(JCTree tree) {
		if (tree == null) return 0;
		class ComplexityScanner extends TreeScanner {
			int complexity = 0;
			public void scan(JCTree tree) {
				if (complexity > jsrlimit) return;
				super.scan(tree);
			}
			public void visitClassDef(JCClassDecl tree) {}
			public void visitDoLoop(JCDoWhileLoop tree)
				{ super.visitDoLoop(tree); complexity++; }
			public void visitWhileLoop(JCWhileLoop tree)
				{ super.visitWhileLoop(tree); complexity++; }
			public void visitForLoop(JCForLoop tree)
				{ super.visitForLoop(tree); complexity++; }
			public void visitSwitch(JCSwitch tree)
				{ super.visitSwitch(tree); complexity+=5; }
			public void visitCase(JCCase tree)
				{ super.visitCase(tree); complexity++; }
			public void visitSynchronized(JCSynchronized tree)
				{ super.visitSynchronized(tree); complexity+=6; }
			public void visitTry(JCTry tree)
				{ super.visitTry(tree);
			  if (tree.finalizer != null) complexity+=6; }
			public void visitCatch(JCCatch tree)
				{ super.visitCatch(tree); complexity+=2; }
			public void visitConditional(JCConditional tree)
				{ super.visitConditional(tree); complexity+=2; }
			public void visitIf(JCIf tree)
				{ super.visitIf(tree); complexity+=2; }
			// note: for break, continue, and return we don't take unwind() into account.
			public void visitBreak(JCBreak tree)
				{ super.visitBreak(tree); complexity+=1; }
			public void visitContinue(JCContinue tree)
				{ super.visitContinue(tree); complexity+=1; }
			public void visitReturn(JCReturn tree)
				{ super.visitReturn(tree); complexity+=1; }
			public void visitThrow(JCThrow tree)
				{ super.visitThrow(tree); complexity+=1; }
			public void visitAssert(JCAssert tree)
				{ super.visitAssert(tree); complexity+=5; }
			public void visitApply(JCMethodInvocation tree)
				{ super.visitApply(tree); complexity+=2; }
			public void visitNewClass(JCNewClass tree)
				{ scan(tree.encl); scan(tree.args); complexity+=2; }
			public void visitNewArray(JCNewArray tree)
				{ super.visitNewArray(tree); complexity+=5; }
			public void visitAssign(JCAssign tree)
				{ super.visitAssign(tree); complexity+=1; }
			public void visitAssignop(JCAssignOp tree)
				{ super.visitAssignop(tree); complexity+=2; }
			public void visitUnary(JCUnary tree)
				{ complexity+=1;
			  if (tree.type.constValue() == null) super.visitUnary(tree); }
			public void visitBinary(JCBinary tree)
				{ complexity+=1;
			  if (tree.type.constValue() == null) super.visitBinary(tree); }
			public void visitTypeTest(JCInstanceOf tree)
				{ super.visitTypeTest(tree); complexity+=1; }
			public void visitIndexed(JCArrayAccess tree)
				{ super.visitIndexed(tree); complexity+=1; }
			public void visitSelect(JCFieldAccess tree)
				{ super.visitSelect(tree);
			  if (tree.sym.kind == VAR) complexity+=1; }
			public void visitIdent(JCIdent tree) {
				if (tree.sym.kind == VAR) {
					complexity+=1;
					if (tree.type.constValue() == null &&
					tree.sym.owner.kind == TYP)
						complexity+=1;
				}
			}
			public void visitLiteral(JCLiteral tree)
				{ complexity+=1; }
			public void visitTree(JCTree tree) {}
			public void visitWildcard(JCWildcard tree) {
				throw new AssertionError(this.getClass().getName());
			}
		}
		ComplexityScanner scanner = new ComplexityScanner();
		tree.accept(scanner);
		return scanner.complexity;
    }