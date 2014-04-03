        private int initCode(JCMethodDecl tree, Env<GenContext> env, boolean fatcode) {
           try {//我加上的
            DEBUG.P(this,"initCode(3)");
            DEBUG.P("tree.sym="+tree.sym);
            DEBUG.P("env="+env);
            DEBUG.P("fatcode="+fatcode);
			DEBUG.P("lineDebugInfo="+lineDebugInfo);
			DEBUG.P("varDebugInfo="+varDebugInfo);
			DEBUG.P("stackMap="+stackMap);
			DEBUG.P("debugCode="+debugCode);
			DEBUG.P("genCrt="+genCrt);
            
            // Create a new code structure.
            meth.code = code = new Code(meth,
                                        fatcode, 
                                        lineDebugInfo ? toplevel.lineMap : null, 
                                        varDebugInfo,
                                        stackMap, 
                                        debugCode,
                                        genCrt ? new CRTable(tree, env.toplevel.endPositions) 
                                               : null,
                                        syms,
                                        types,
                                        pool);
            items = new Items(pool, code, syms, types);
            if (code.debugCode)
                System.err.println(meth + " for body " + tree);

            // If method is not static, create a new local variable address
            // for `this'.

			DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));

            if ((tree.mods.flags & STATIC) == 0) {
                Type selfType = meth.owner.type;

				DEBUG.P("selfType="+selfType);
				DEBUG.P("meth.isConstructor()="+meth.isConstructor());

                if (meth.isConstructor() && selfType != syms.objectType)
                    selfType = UninitializedType.uninitializedThis(selfType);

				DEBUG.P("selfType="+selfType);

				//this变量在局部变量数组的索引总是0
                code.setDefined(
                        code.newLocal(
                            new VarSymbol(FINAL, names._this, selfType, meth.owner)));
            }

            // Mark all parameters as defined from the beginning of
            // the method.

			DEBUG.P("tree.params="+tree.params);

            for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
                checkDimension(l.head.pos(), l.head.sym.type);
                code.setDefined(code.newLocal(l.head.sym));
            }

            // Get ready to generate code for method body.
            int startpcCrt = genCrt ? code.curPc() : 0;
            code.entryPoint();

            // Suppress initial stackmap
            code.pendingStackMap = false;
            
			DEBUG.P("startpcCrt="+startpcCrt);

            return startpcCrt;

			}finally{//我加上的
			DEBUG.P(1,this,"initCode(3)");
			}
        }