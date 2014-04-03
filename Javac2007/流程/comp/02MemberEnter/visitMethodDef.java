    public void visitMethodDef(JCMethodDecl tree) {
    	DEBUG.P(this,"visitMethodDef(1)");
    	DEBUG.P("tree.name="+tree.name); 
        Scope enclScope = enter.enterScope(env);
        DEBUG.P("enclScope前="+enclScope); 
        MethodSymbol m = new MethodSymbol(0, tree.name, null, enclScope.owner);
        DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));
		DEBUG.P("m.flags_field前="+Flags.toString(m.flags_field));
        m.flags_field = chk.checkFlags(tree.pos(), tree.mods.flags, m, tree);
        tree.sym = m;
        DEBUG.P("m.flags_field后="+Flags.toString(m.flags_field));
        Env<AttrContext> localEnv = methodEnv(tree, env);
        
        //DEBUG.P("localEnv="+localEnv); 
        
        // Compute the method type
        m.type = signature(tree.typarams, tree.params,
                           tree.restype, tree.thrown,
                           localEnv);
        
        DEBUG.P("m.type.tag="+TypeTags.toString(m.type.tag));
                          
        // Set m.params
        ListBuffer<VarSymbol> params = new ListBuffer<VarSymbol>();
        JCVariableDecl lastParam = null;
        for (List<JCVariableDecl> l = tree.params; l.nonEmpty(); l = l.tail) {
            JCVariableDecl param = lastParam = l.head;
            assert param.sym != null;
            params.append(param.sym);
        }
        m.params = params.toList();

        // mark the method varargs, if necessary
        if (lastParam != null && (lastParam.mods.flags & Flags.VARARGS) != 0)
            m.flags_field |= Flags.VARARGS;

        localEnv.info.scope.leave();
        DEBUG.P("localEnv="+localEnv); 
        if (chk.checkUnique(tree.pos(), m, enclScope)) {
            enclScope.enter(m);
        }
        annotateLater(tree.mods.annotations, localEnv, m);

		DEBUG.P("tree.defaultValue="+tree.defaultValue); 
        if (tree.defaultValue != null)
            annotateDefaultValueLater(tree.defaultValue, localEnv, m);
        
        DEBUG.P("enclScope后="+enclScope); 
       	DEBUG.P(0,this,"visitMethodDef(1)");     
    }