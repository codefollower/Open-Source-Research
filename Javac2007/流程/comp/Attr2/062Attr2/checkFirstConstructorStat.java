    //where
        /** Check that given application node appears as first statement
         *  in a constructor call.
         *  @param tree   The application node
         *  @param env    The environment current at the application.
         */
        boolean checkFirstConstructorStat(JCMethodInvocation tree, Env<AttrContext> env) {
            try {//我加上的
			DEBUG.P(this,"checkFirstConstructorStat(2)");
			DEBUG.P("tree="+tree);
			DEBUG.P("env="+env);
			
            JCMethodDecl enclMethod = env.enclMethod;
            if (enclMethod != null && enclMethod.name == names.init) {
                JCBlock body = enclMethod.body;
                if (body.stats.head.tag == JCTree.EXEC &&
                    ((JCExpressionStatement) body.stats.head).expr == tree)
                    return true;
            }
            log.error(tree.pos(),"call.must.be.first.stmt.in.ctor",
                      TreeInfo.name(tree.meth));
            return false;
            
            }finally{//我加上的
			DEBUG.P(0,this,"checkFirstConstructorStat(2)");
			}
        }