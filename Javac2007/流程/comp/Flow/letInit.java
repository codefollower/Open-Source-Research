    /** Record an initialization of a trackable variable.
     */
    void letInit(DiagnosticPosition pos, VarSymbol sym) {
		DEBUG.P(this,"letInit(2)");
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.adr="+sym.adr);
		DEBUG.P("firstadr="+firstadr);
		DEBUG.P("inits="+inits);
		if (sym.adr >= firstadr && trackable(sym)) {
			if ((sym.flags() & FINAL) != 0) {
				if ((sym.flags() & PARAMETER) != 0) {
					/*例子:
					void myMethod(final int i) {
						i++;
					}
					*/
					log.error(pos, "final.parameter.may.not.be.assigned",
						  sym);
				} else if (!uninits.isMember(sym.adr)) {
					log.error(pos,
						  loopPassTwo
						  ? "var.might.be.assigned.in.loop"
						  : "var.might.already.be.assigned",
						  sym);
				} else if (!inits.isMember(sym.adr)) {
					DEBUG.P("sym.adr="+sym.adr);
					DEBUG.P("uninits   前="+uninits);
					DEBUG.P("uninitsTry前="+uninitsTry);
					// reachable assignment
					uninits.excl(sym.adr);
					uninitsTry.excl(sym.adr);
					
					DEBUG.P("uninits   后="+uninits);
					DEBUG.P("uninitsTry后="+uninitsTry);
				} else {
					//log.rawWarning(pos, "unreachable assignment");//DEBUG
					uninits.excl(sym.adr);
				}
			}
			inits.incl(sym.adr);
		} else if ((sym.flags() & FINAL) != 0) {
			log.error(pos, "var.might.already.be.assigned", sym);
		}
		
		DEBUG.P("inits="+inits);
		DEBUG.P(0,this,"letInit(2)");
    }

    /** If tree is either a simple name or of the form this.name or
     *	C.this.name, and tree represents a trackable variable,
     *	record an initialization of the variable.
     */
    void letInit(JCTree tree) {
		DEBUG.P(this,"letInit(1)");
		tree = TreeInfo.skipParens(tree);
		DEBUG.P("tree.tag="+tree.myTreeTag());
		if (tree.tag == JCTree.IDENT || tree.tag == JCTree.SELECT) {
			Symbol sym = TreeInfo.symbol(tree);
			letInit(tree.pos(), (VarSymbol)sym);
		}
		DEBUG.P(0,this,"letInit(1)");
    }