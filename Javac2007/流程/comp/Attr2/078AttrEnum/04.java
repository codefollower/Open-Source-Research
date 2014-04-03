    public void visitSelect(JCFieldAccess tree) {
    	DEBUG.P(this,"visitSelect(1)");
    	DEBUG.P("tree.name="+tree.name);
    	/*对于像Qualident = Ident { DOT Ident }这样的语法，
    	如果最后一个Ident是“this”、“super”、“class”，那么前
    	一个Ident的符号类型(symbol kind)只能是TYP，也就是说只有
    	类型名后面才能跟“this”、“super”、“class”；
    	
    	如果最后一个Ident符号类型是PCK，那么前一个Ident的符号类型
    	也是PCK，因为包名前面只能是包名；
    	
    	如果最后一个Ident符号类型是TYP，那么前一个Ident的符号类型
    	可以是TYP或PCK，因为类型名可以是内部类，这时前一个Ident
    	的符号类型就是TYP，否则只能是PCK；
    	
    	如果最后一个Ident符号类型是VAL或MTH，也就是当它是
    	变量或非变量表达式(variables or non-variable expressions)
    	或者是方法名的时候，那么前一个Ident的符号类型
    	可以是VAL或TYP。
    	*/
    	
        // Determine the expected kind of the qualifier expression.
        int skind = 0;
        if (tree.name == names._this || tree.name == names._super ||
            tree.name == names._class)
        {
            skind = TYP;
        } else {
            if ((pkind & PCK) != 0) skind = skind | PCK;
            if ((pkind & TYP) != 0) skind = skind | TYP | PCK;
            if ((pkind & (VAL | MTH)) != 0) skind = skind | VAL | TYP;
        }

        // Attribute the qualifier expression, and determine its symbol (if any).
        Type site = attribTree(tree.selected, env, skind, Infer.anyPoly);
        
        DEBUG.P("site.tag="+TypeTags.toString(site.tag));
        
        DEBUG.P("pkind="+Kinds.toString(pkind));
        DEBUG.P("skind="+Kinds.toString(skind));
        if ((pkind & (PCK | TYP)) == 0)
            site = capture(site); // Capture field access

        // don't allow T.class T[].class, etc
        if (skind == TYP) {
            Type elt = site;
            while (elt.tag == ARRAY)
                elt = ((ArrayType)elt).elemtype;
            if (elt.tag == TYPEVAR) {
                log.error(tree.pos(), "type.var.cant.be.deref");
                result = syms.errType;
                return;
            }
        }

        // If qualifier symbol is a type or `super', assert `selectSuper'
        // for the selection. This is relevant for determining whether
        // protected symbols are accessible.
        Symbol sitesym = TreeInfo.symbol(tree.selected);
        boolean selectSuperPrev = env.info.selectSuper;
        
        DEBUG.P("sitesym="+sitesym);
        DEBUG.P("selectSuperPrev="+selectSuperPrev);
        
        env.info.selectSuper =
            sitesym != null &&
            sitesym.name == names._super;

        // If selected expression is polymorphic, strip
        // type parameters and remember in env.info.tvars, so that
        // they can be added later (in Attr.checkId and Infer.instantiateMethod).
        if (tree.selected.type.tag == FORALL) {
            ForAll pstype = (ForAll)tree.selected.type;
            env.info.tvars = pstype.tvars;
            site = tree.selected.type = pstype.qtype;
        }

        // Determine the symbol represented by the selection.
        env.info.varArgs = false;
        Symbol sym = selectSym(tree, site, env, pt, pkind);
        
        DEBUG.P("sym.exists()="+sym.exists());
        DEBUG.P("isType(sym)="+isType(sym));
        DEBUG.P("pkind="+Kinds.toString(pkind));
        
        if (sym.exists() && !isType(sym) && (pkind & (PCK | TYP)) != 0) {
            site = capture(site);
            sym = selectSym(tree, site, env, pt, pkind);
        }
        boolean varArgs = env.info.varArgs;
        tree.sym = sym;
        
        DEBUG.P("env.info.varArgs="+env.info.varArgs);
        DEBUG.P("tree.sym="+tree.sym);
        DEBUG.P("site.tag="+TypeTags.toString(site.tag));
        
        if (site.tag == TYPEVAR && !isType(sym) && sym.kind != ERR)
            site = capture(site.getUpperBound());

        // If that symbol is a variable, ...
        if (sym.kind == VAR) {
            VarSymbol v = (VarSymbol)sym;

            // ..., evaluate its initializer, if it has one, and check for
            // illegal forward reference.
            checkInit(tree, env, v, true);

            // If we are expecting a variable (as opposed to a value), check
            // that the variable is assignable in the current environment.
            if (pkind == VAR)
                checkAssignable(tree.pos(), v, tree.selected, env);
        }
        
        DEBUG.P("isType(sym)="+isType(sym));
        DEBUG.P("sitesym="+sitesym);
        if(sitesym!=null) DEBUG.P("sitesym.kind="+Kinds.toString(sitesym.kind));
        
        // Disallow selecting a type from an expression
        if (isType(sym) && (sitesym==null || (sitesym.kind&(TYP|PCK)) == 0)) {
            tree.type = check(tree.selected, pt,
                              sitesym == null ? VAL : sitesym.kind, TYP|PCK, pt);
        }
        
        DEBUG.P("isType(sitesym)="+isType(sitesym));
        
        if (isType(sitesym)) {
        	DEBUG.P("sym.name="+sym.name);
            if (sym.name == names._this) {
                // If `C' is the currently compiled class, check that
                // C.this' does not appear in a call to a super(...)
                if (env.info.isSelfCall &&
                    site.tsym == env.enclClass.sym) {
                    chk.earlyRefError(tree.pos(), sym);
                }
            } else {
                // Check if type-qualified fields or methods are static (JLS)
                if ((sym.flags() & STATIC) == 0 &&
                    sym.name != names._super &&
                    (sym.kind == VAR || sym.kind == MTH)) {
                    rs.access(rs.new StaticError(sym),
                              tree.pos(), site, sym.name, true);
                }
            }
        }
        
        DEBUG.P("env.info.selectSuper="+env.info.selectSuper);
        DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));
        // If we are selecting an instance member via a `super', ...
        if (env.info.selectSuper && (sym.flags() & STATIC) == 0) {

            // Check that super-qualified symbols are not abstract (JLS)
            rs.checkNonAbstract(tree.pos(), sym);

            if (site.isRaw()) {
                // Determine argument types for site.
                Type site1 = types.asSuper(env.enclClass.sym.type, site.tsym);
                if (site1 != null) site = site1;
            }
        }

        env.info.selectSuper = selectSuperPrev;
        result = checkId(tree, site, sym, env, pkind, pt, varArgs);
        env.info.tvars = List.nil();
        
        DEBUG.P(0,this,"visitSelect(1)");
    }