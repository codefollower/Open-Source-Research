    public void visitVarDef(JCVariableDecl tree) {
    	DEBUG.P(this,"visitVarDef(1)");

        DEBUG.P("tree="+tree); 
        DEBUG.P("tree.mods.flags="+Flags.toString(tree.mods.flags));
		DEBUG.P("env.info.scope.owner="+env.info.scope.owner);
		DEBUG.P("env.info.scope.owner.flags_field="+Flags.toString(env.info.scope.owner.flags_field));
        
        Env<AttrContext> localEnv = env;
        
        DEBUG.P("localEnv1="+localEnv); 
        
        if ((tree.mods.flags & STATIC) != 0 ||
            (env.info.scope.owner.flags() & INTERFACE) != 0) {
            localEnv = env.dup(tree, env.info.dup());
            localEnv.info.staticLevel++;
        }
        DEBUG.P("localEnv2="+localEnv); 
        
        attr.attribType(tree.vartype, localEnv);
        Scope enclScope = enter.enterScope(env);
        
        DEBUG.P("enclScope前="+enclScope); 
        
        VarSymbol v =
            new VarSymbol(0, tree.name, tree.vartype.type, enclScope.owner);
        v.flags_field = chk.checkFlags(tree.pos(), tree.mods.flags, v, tree);
        tree.sym = v;
        
        DEBUG.P("v.flags_field="+Flags.toString(v.flags_field));
        
        if (tree.init != null) {
            v.flags_field |= HASINIT;
            if ((v.flags_field & FINAL) != 0 && tree.init.tag != JCTree.NEWCLASS)
                v.setLazyConstValue(initEnv(tree, env), log, attr, tree.init);
        }
        if (chk.checkUnique(tree.pos(), v, enclScope)) {
            chk.checkTransparentVar(tree.pos(), v, enclScope);
            enclScope.enter(v);
        }
        annotateLater(tree.mods.annotations, localEnv, v);
        v.pos = tree.pos;
        
        DEBUG.P("enclScope后="+enclScope); 
        DEBUG.P(0,this,"visitVarDef(1)");
    }