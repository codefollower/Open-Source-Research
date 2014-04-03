    public void visitSelect(JCFieldAccess tree) {
		DEBUG.P(this,"visitSelect(1)");
		Symbol sym = tree.sym;
		
		DEBUG.P("tree.name="+tree.name);
		if (tree.name == names._class) {
			assert target.hasClassLiterals();
			code.emitop2(ldc2, makeRef(tree.pos(), tree.selected.type));
			result = items.makeStackItem(pt);
			return;
		}

		Symbol ssym = TreeInfo.symbol(tree.selected);
		
		DEBUG.P("ssym="+ssym);
		if(ssym != null) {
			DEBUG.P("ssym.kind="+Kinds.toString(ssym.kind));
			DEBUG.P("ssym.name="+ssym.name);
		}

		// Are we selecting via super?
		boolean selectSuper =
			ssym != null && (ssym.kind == TYP || ssym.name == names._super);
		
		DEBUG.P("");
		DEBUG.P("selectSuper="+selectSuper);
		
		// Are we accessing a member of the superclass in an access method
		// resulting from a qualified super?
		boolean accessSuper = isAccessSuper(env.enclMethod);
		
		DEBUG.P("accessSuper="+accessSuper);
		
		Item base = (selectSuper)
			? items.makeSuperItem()
			: genExpr(tree.selected, tree.selected.type);
		
		DEBUG.P("");
		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
		if (sym.kind == VAR && ((VarSymbol) sym).getConstValue() != null) {
			// We are seeing a variable that is constant but its selecting
			// expression is not.
			if ((sym.flags() & STATIC) != 0) {
				if (!selectSuper && (ssym == null || ssym.kind != TYP))
					base = base.load();
				base.drop();
			} else {
				base.load();
				genNullCheck(tree.selected.pos());
			}
			result = items.
			makeImmediateItem(sym.type, ((VarSymbol) sym).getConstValue());
		} else {
			if (!accessSuper)
				sym = binaryQualifier(sym, tree.selected.type);
			if ((sym.flags() & STATIC) != 0) {
				if (!selectSuper && (ssym == null || ssym.kind != TYP))
					base = base.load();
				base.drop();
				result = items.makeStaticItem(sym);
			} else {
			base.load();
				if (sym == syms.lengthVar) {
					code.emitop0(arraylength);
					result = items.makeStackItem(syms.intType);
				} else {
					result = items.
					makeMemberItem(sym,
							   (sym.flags() & PRIVATE) != 0 ||
							   selectSuper || accessSuper);
				}
			}
		}
		DEBUG.P(0,this,"visitSelect(1)");
    }