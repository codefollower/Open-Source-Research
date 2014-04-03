	public void visitSelect(JCFieldAccess tree) {
		/*************************************************************
		pkind表示当前期待tree.type.tsym是pkind指定的类型
		例如pkind=PCK，就表示tree.type.tsym代表的是一个包(如:my.test)
		**************************************************************/
        // <editor-fold defaultstate="collapsed">
		try {
    	DEBUG.P(this,"visitSelect(1)");
    	DEBUG.P("tree.name="+tree.name);
		DEBUG.P("tree="+tree);
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
			//注意:如果pkind=VAR，那么(pkind & (VAL | MTH)) != 0)是不等于0的
			//因为(VAR & VAL)!=0;
			//DEBUG.P("(VAR & VAL)="+(VAR & VAL));
            if ((pkind & (VAL | MTH)) != 0) skind = skind | VAL | TYP;
        }

        // Attribute the qualifier expression, and determine its symbol (if any).
        Type site = attribTree(tree.selected, env, skind, Infer.anyPoly);//Infer.anyPoly是一个Type(NONE, null)与JCNoType(NONE)类拟
        
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

				//我加上的，见if (tree.selected.type.tag == FORALL)的注释
				tree.type = syms.errType;
                return;
            }
        }
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">

        // If qualifier symbol is a type or `super', assert `selectSuper'
        // for the selection. This is relevant for determining whether
        // protected symbols are accessible.
		DEBUG.P("tree.selected="+tree.selected);
        Symbol sitesym = TreeInfo.symbol(tree.selected);
        boolean selectSuperPrev = env.info.selectSuper;
        
        DEBUG.P("sitesym="+sitesym);
		if(sitesym==site.tsym)
			DEBUG.P("sitesym==site.tsym");
		else
			DEBUG.P("sitesym!=site.tsym");
        DEBUG.P("selectSuperPrev="+selectSuperPrev);
        
        env.info.selectSuper =
            sitesym != null &&
            sitesym.name == names._super;

        // If selected expression is polymorphic, strip
        // type parameters and remember in env.info.tvars, so that
        // they can be added later (in Attr.checkId and Infer.instantiateMethod).

		DEBUG.P("env.info.selectSuper="+env.info.selectSuper);
		try {
		DEBUG.P("tree="+tree);
		DEBUG.P("tree.selected="+tree.selected);
		DEBUG.P("tree.selected.type="+tree.selected.type);
		DEBUG.P("tree.selected.type.tag="+TypeTags.toString(tree.selected.type.tag));

		/*
		这里有NullPointerException
		当编译T t=T.super.toString();时，
		上面的skind = TYP，报告错误"无法从类型变量中进行选择"后返回，
		但是没有对(T.super)JCFieldAccess tree.type赋值，
		导致tree.selected.type = null;
		*/
        if (tree.selected.type.tag == FORALL) {
            ForAll pstype = (ForAll)tree.selected.type;
            env.info.tvars = pstype.tvars;
            site = tree.selected.type = pstype.qtype;
        }

		} catch (RuntimeException e) {
			System.err.println("出错了:"+e);
			e.printStackTrace();
			throw e;
		}
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">
        // Determine the symbol represented by the selection.
        env.info.varArgs = false;
        Symbol sym = selectSym(tree, site, env, pt, pkind);
        
		DEBUG.P("tree="+tree);
		DEBUG.P("sym="+sym);
		DEBUG.P("sym.type="+sym.type);
		DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
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
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">
        
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
				/*
					test\attr\VisitSelectTest.java:15: 无法从类型变量中进行选择
					public class VisitSelectTest<T extends B> extends A<T.b> {
																		 ^
					test\attr\VisitSelectTest.java:15: 无法从类型变量中进行选择
					public class VisitSelectTest<T extends B> extends A<T.b> {
																		 ^
					test\attr\VisitSelectTest.java:19: 无法从静态上下文中引用非静态 变量 b
							B b=T.b;
								 ^
					test\attr\VisitSelectTest.java:20: 无法从静态上下文中引用非静态 方法 b()
							B b2=T.b();
								  ^
					4 错误
					class A<T>{}
					class B {
						//int i;
						B b;
						B b(){ return new B(); }
						class b{}
					}
					public class VisitSelectTest<T extends B> extends A<T.b> {
						//A<T.i> al;

						//A<T.b> al;
						B b=T.b;
						B b2=T.b();
					}
				*/
                if ((sym.flags() & STATIC) == 0 &&
                    sym.name != names._super &&
                    (sym.kind == VAR || sym.kind == MTH)) {
                    rs.access(rs.new StaticError(sym),
                              tree.pos(), site, sym.name, true);
                }
            }
        }
        
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed">
        
        DEBUG.P("env.info.selectSuper="+env.info.selectSuper);
        DEBUG.P("sym.flags_field="+Flags.toString(sym.flags_field));
        // If we are selecting an instance member via a `super', ...
        if (env.info.selectSuper && (sym.flags() & STATIC) == 0) {
			/*
				class ClassA<T>{
					void m(){};
				}
				public class VisitSelectTest extends ClassA {
					void m() {super.m();} //site.isRaw()=true
				}


				abstract class ClassA{
					abstract void m();
				}
				public class VisitSelectTest extends ClassA {
					void m() {super.m();} //无法直接访问 test.attr.ClassA 中的抽象 方法
				}
			*/
            // Check that super-qualified symbols are not abstract (JLS)
            rs.checkNonAbstract(tree.pos(), sym);

			DEBUG.P("site="+site);
			DEBUG.P("site.isRaw()="+site.isRaw());
            if (site.isRaw()) {
                // Determine argument types for site.
                Type site1 = types.asSuper(env.enclClass.sym.type, site.tsym);
                
				DEBUG.P("(site1 == site)="+(site1 == site));
				if (site1 != null) site = site1;
            }
        }

        env.info.selectSuper = selectSuperPrev;
        result = checkId(tree, site, sym, env, pkind, pt, varArgs);
        env.info.tvars = List.nil();
        
        
		}finally{//我加上的
        DEBUG.P(0,this,"visitSelect(1)");
        }
        // </editor-fold>
    }