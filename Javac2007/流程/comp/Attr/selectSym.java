        /** Determine symbol referenced by a Select expression,
         *
         *  @param tree   The select tree.
         *  @param site   The type of the selected expression,
         *  @param env    The current environment.
         *  @param pt     The current prototype.
         *  @param pkind  The expected kind(s) of the Select expression.
         */
        private Symbol selectSym(JCFieldAccess tree,
                                 Type site,
                                 Env<AttrContext> env,
                                 Type pt,
                                 int pkind) {
            try {//我加上的
            DEBUG.P(this,"selectSym(5)");
            DEBUG.P("tree="+tree);
            DEBUG.P("site="+site); 
            DEBUG.P("site.tag="+TypeTags.toString(site.tag));   
            DEBUG.P("env="+env);
            DEBUG.P("pt="+pt); 
            DEBUG.P("pt.tag="+TypeTags.toString(pt.tag));
            DEBUG.P("pkind="+Kinds.toString(pkind));
			
            DiagnosticPosition pos = tree.pos();
            Name name = tree.name;

			DEBUG.P("name="+name);

            switch (site.tag) {
            case PACKAGE:
                return rs.access(
                    rs.findIdentInPackage(env, site.tsym, name, pkind),
                    pos, site, name, true);
            case ARRAY:
            case CLASS:
                if (pt.tag == METHOD || pt.tag == FORALL) {
                    return rs.resolveQualifiedMethod(
                        pos, env, site, name, pt.getParameterTypes(), pt.getTypeArguments());
				//此处不处理像c.super()或c.this()(语法错误)这样的情形
				//而是在visitApply(1)中处理
                } else if (name == names._this || name == names._super) {
                    return rs.resolveSelf(pos, env, site.tsym, name);
                } else if (name == names._class) {
                    // In this case, we have already made sure in
                    // visitSelect that qualifier expression is a type.
                    Type t = syms.classType;
                    List<Type> typeargs = allowGenerics
                        ? List.of(types.erasure(site))
                        : List.<Type>nil();
                    t = new ClassType(t.getEnclosingType(), typeargs, t.tsym);
                    return new VarSymbol(
                        STATIC | PUBLIC | FINAL, names._class, t, site.tsym);
                } else {
                    // We are seeing a plain identifier as selector.
                    Symbol sym = rs.findIdentInType(env, site, name, pkind);
                    if ((pkind & ERRONEOUS) == 0)
                        sym = rs.access(sym, pos, site, name, true);
                    return sym;
                }
            case WILDCARD:
                throw new AssertionError(tree);
            case TYPEVAR:
                // Normally, site.getUpperBound() shouldn't be null.
                // It should only happen during memberEnter/attribBase
                // when determining the super type which *must* be
                // done before attributing the type variables.  In
                // other words, we are seeing this illegal program:
                // class B<T> extends A<T.foo> {}
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
				DEBUG.P("site.getUpperBound()="+site.getUpperBound()); 
                Symbol sym = (site.getUpperBound() != null)
                    ? selectSym(tree, capture(site.getUpperBound()), env, pt, pkind)
                    : null;
				DEBUG.P("sym="+sym); 
				DEBUG.P("selectSym(5) isType(sym)="+isType(sym)); 
				if(sym!=null)DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
				/*
				class A{
					C c;
					static class C{
						static int i;
					}
				}
				class D<T extends A> {
					Class<?> c = T.class;
					int i = T.C.i; //isType(sym)=true
					A.C c = T.c; //无法从静态上下文中引用非静态 变量 c
				}
				*/
                if (sym == null || isType(sym)) {
                    log.error(pos, "type.var.cant.be.deref");
                    return syms.errSymbol;
                } else {
                    return sym;
                }
            case ERROR:
                // preserve identifier names through errors
                return new ErrorType(name, site.tsym).tsym;
            default:
                // The qualifier expression is of a primitive type -- only
                // .class is allowed for these.
                if (name == names._class) {
                    // In this case, we have already made sure in Select that
                    // qualifier expression is a type.
                    Type t = syms.classType;
                    Type arg = types.boxedClass(site).type;
                    t = new ClassType(t.getEnclosingType(), List.of(arg), t.tsym);
                    return new VarSymbol(
                        STATIC | PUBLIC | FINAL, names._class, t, site.tsym);
                } else {
					/*
						test\attr\VisitSelectTest.java:8: 无法取消引用 int
										int c = t.t;
												 ^
						1 错误
						void m(int t){
							int c = t.t;
						}
					*/
                    log.error(pos, "cant.deref", site);
                    return syms.errSymbol;
                }
            }
            
            }finally{//我加上的
            DEBUG.P(0,this,"selectSym(5)");
            }
        }