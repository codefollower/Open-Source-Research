    /** Find an unqualified type symbol.
     *  @param env       The current environment.
     *  @param name      The type's name.
     */
    Symbol findType(Env<AttrContext> env, Name name) {
    	Symbol bestSoFar = typeNotFound;
        Symbol sym;
        
    	try{
    	//DEBUG.ON();
    	DEBUG.P(this,"findType(2)");
    	DEBUG.P("name="+name);
    	DEBUG.P("env="+env);
    	DEBUG.P("env.outer="+env.outer);
    	
    	/*
    	先从当前env.info.scope中查找name，
    	没找到时再根据env.enclClass.sym查找，
    	因为env.enclClass.sym是ClassSymbol类的实例引用,
    	所以实际是在ClassSymbol的members_field中查找name,
    	如还没找到再找ClassSymbol.type的超类以及所有实现的接口,
    	如还没找到再在env.outer中按上面的方式查找.
    	
    	经过上面后(直到env.outer==null，即topLevelEnv),如还没找到
    	再找env.toplevel.namedImportScope,
    	再找env.toplevel.packge.members(),
    	再找env.toplevel.starImportScope
    	*/
        
        boolean staticOnly = false;
		//为什么结束条件是env1.outer != null呢?
		//因为当env1.outer == null时，表示env1是最顶层了，
		//最顶层的env.enclClass.sym.members_field是Symtab中预定义
		//的符号是没有TYP类型的基本符号，所以没必要查找了，
		//被遗漏的namedImportScope在退出for时再查找
        for (Env<AttrContext> env1 = env; env1.outer != null; env1 = env1.outer) {
            // <editor-fold defaultstate="collapsed">
            DEBUG.P("env1.info.staticLevel="+env1.info.staticLevel);
            DEBUG.P("env1.outer.info.staticLevel="+env1.outer.info.staticLevel);
            if (isStatic(env1)) staticOnly = true;
            DEBUG.P("staticOnly="+staticOnly);
            DEBUG.P("env1.info.scope="+env1.info.scope);
            for (Scope.Entry e = env1.info.scope.lookup(name);
                 e.scope != null;
                 e = e.next()) {
				DEBUG.P("e.sym="+e.sym);
                DEBUG.P("e.sym.kind="+Kinds.toString(e.sym.kind));
				if (e.sym.kind == TYP) {
					DEBUG.P("e.sym.type.tag="+TypeTags.toString(e.sym.type.tag));
					DEBUG.P("e.sym.owner="+e.sym.owner);
					DEBUG.P("e.sym.owner.kind="+Kinds.toString(e.sym.owner.kind));
						
					/*
						错误例子:
                        bin\mysrc\my\test\Test.java:28: 无法从静态上下文中引用非静态 类型变量的限制范围T
                                        public static <M extends T,S> int[] myMethod(final M m,S[] s[],int i,Str
                        ing s2,int... ii)[] throws Exception,Error{
                                                                                         ^
                        这里的错误提示位置有点怪，虽然错误是在static方法myMethod中引用非静态 类型变量T，
                        但错误提示位置是在Exception，而不是在<M extends T,S>下
                        
						class VisitSelectTest<T> {
							static T a2;
						}
						test\attr\VisitSelectTest.java:3: 无法从静态上下文中引用非静态 类型变量的限制范 围 T
								static T a2;
									   ^
						1 错误						
					*/
					if (staticOnly &&
                        e.sym.type.tag == TYPEVAR &&
                        e.sym.owner.kind == TYP) return new StaticError(e.sym);

                    DEBUG.P("已找到 "+e.sym+" 在env="+env1.info.scope);
                    return e.sym;
				}
            }

            sym = findMemberType(env1, env1.enclClass.sym.type, name,
                                 env1.enclClass.sym);
            if (staticOnly && sym.kind == TYP &&
                sym.type.tag == CLASS &&
                sym.type.getEnclosingType().tag == CLASS &&
                env1.enclClass.sym.type.isParameterized() &&
                sym.type.getEnclosingType().isParameterized())
                return new StaticError(sym);
            else if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;

            JCClassDecl encl = env1.baseClause ? (JCClassDecl)env1.tree : env1.enclClass;
            if ((encl.sym.flags() & STATIC) != 0)
                staticOnly = true;
            
            // </editor-fold>
        }
        
        DEBUG.P("env.tree.tag="+env.tree.myTreeTag());
        if (env.tree.tag != JCTree.IMPORT) {
            sym = findGlobalType(env, env.toplevel.namedImportScope, name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;

            sym = findGlobalType(env, env.toplevel.packge.members(), name);
            DEBUG.P("sym="+sym);
            DEBUG.P("sym.exists()="+sym.exists());
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;

            sym = findGlobalType(env, env.toplevel.starImportScope, name);
            if (sym.exists()) return sym;
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        
        return bestSoFar;
        
        
    	}finally{
    	//DEBUG.P("env.toplevel.namedImportScope="+env.toplevel.namedImportScope);
    	//DEBUG.P("env.toplevel.packge.members()="+env.toplevel.packge.members());
    	//DEBUG.P("env.toplevel.starImportScope="+env.toplevel.starImportScope);
    	//DEBUG.P("Symbol bestSoFar="+bestSoFar);
    	DEBUG.P("bestSoFar.kind="+Kinds.toString(bestSoFar.kind));
    	DEBUG.P(0,this,"findType(2)");
    	//DEBUG.OFF();
    	}
    }