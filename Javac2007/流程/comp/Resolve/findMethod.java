    /** Find best qualified method matching given name, type and value
     *  arguments.
     *  @param env       The current environment.
     *  @param site      The original type from where the selection
     *                   takes place.
     *  @param name      The method's name.
     *  @param argtypes  The method's value arguments.
     *  @param typeargtypes The method's type arguments
     *  @param allowBoxing Allow boxing conversions of arguments.
     *  @param useVarargs Box trailing arguments into an array for varargs.
     */
    Symbol findMethod(Env<AttrContext> env,
                      Type site,
                      Name name,
                      List<Type> argtypes,
                      List<Type> typeargtypes,
                      boolean allowBoxing,
                      boolean useVarargs,
                      boolean operator) {
        return findMethod(env,
                          site,
                          name,
                          argtypes,
                          typeargtypes,
                          site.tsym.type,
                          true,
                          methodNotFound,
                          allowBoxing,
                          useVarargs,
                          operator);
    }
    // where
    private Symbol findMethod(Env<AttrContext> env,
                              Type site,
                              Name name,
                              List<Type> argtypes,
                              List<Type> typeargtypes,
                              Type intype,
                              boolean abstractok,
                              Symbol bestSoFar,
                              boolean allowBoxing,
                              boolean useVarargs,
                              boolean operator) {
		DEBUG.P(this,"findMethod(11)");
    	DEBUG.P("env="+env);
		DEBUG.P("site="+site); //site是不含类型变量(typeAppyy)的类型
		DEBUG.P("name="+name);
		DEBUG.P("argtypes="+argtypes);
		DEBUG.P("typeargtypes="+typeargtypes);
		DEBUG.P("intype="+intype);             //intype是含原始类型变量的类型
		DEBUG.P("abstractok="+abstractok);
		DEBUG.P("bestSoFar="+bestSoFar);
		DEBUG.P("allowBoxing="+allowBoxing);
		DEBUG.P("useVarargs="+useVarargs);
		DEBUG.P("operator="+operator);
		DEBUG.P(2);

        for (Type ct = intype; ct.tag == CLASS; ct = types.supertype(ct)) {
            ClassSymbol c = (ClassSymbol)ct.tsym;
			DEBUG.P("c="+c);
			DEBUG.P("c.flags()="+Flags.toString(c.flags()));
			DEBUG.P("c.members()="+c.members());
            if ((c.flags() & (ABSTRACT | INTERFACE)) == 0)
                abstractok = false;
            for (Scope.Entry e = c.members().lookup(name);
                 e.scope != null;
                 e = e.next()) {
                //- System.out.println(" e " + e.sym);
                if (e.sym.kind == MTH &&
                    (e.sym.flags_field & SYNTHETIC) == 0) {
                    bestSoFar = selectBest(env, site, argtypes, typeargtypes,
                                           e.sym, bestSoFar,
                                           allowBoxing,
                                           useVarargs,
                                           operator);
                }
            }
			//如果在一个非抽象类中找不到指定的方法，
			//那么在这个类所有实现的接口同样找不到，
			//所以上面的if ((c.flags() & (ABSTRACT | INTERFACE)) == 0)
			//把abstractok设成了false之后，下面的部分就不再执行了
			DEBUG.P("abstractok="+abstractok);
            //- System.out.println(" - " + bestSoFar);
            if (abstractok) {
                Symbol concrete = methodNotFound;
                if ((bestSoFar.flags() & ABSTRACT) == 0)
                    concrete = bestSoFar;
                for (List<Type> l = types.interfaces(c.type);
                     l.nonEmpty();
                     l = l.tail) {
                    bestSoFar = findMethod(env, site, name, argtypes,
                                           typeargtypes,
                                           l.head, abstractok, bestSoFar,
                                           allowBoxing, useVarargs, operator);
                }
                if (concrete != bestSoFar &&
                    concrete.kind < ERR  && bestSoFar.kind < ERR &&
                    types.isSubSignature(concrete.type, bestSoFar.type))
                    bestSoFar = concrete;
            }
        }

		DEBUG.P("bestSoFar="+bestSoFar);
		DEBUG.P(0,this,"findMethod(11)");
        return bestSoFar;
    }