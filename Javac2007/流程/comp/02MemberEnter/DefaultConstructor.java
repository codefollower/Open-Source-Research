/* ***************************************************************************
 * tree building
 ****************************************************************************/

    /** Generate default constructor for given class. For classes different
     *  from java.lang.Object, this is:
     *
     *    c(argtype_0 x_0, ..., argtype_n x_n) throws thrown {
     *      super(x_0, ..., x_n)
     *    }
     *
     *  or, if based == true:
     *
     *    c(argtype_0 x_0, ..., argtype_n x_n) throws thrown {
     *      x_0.super(x_1, ..., x_n)
     *    }
     *
     *  @param make     The tree factory.
     *  @param c        The class owning the default constructor.
     *  @param argtypes The parameter types of the constructor.
     *  @param thrown   The thrown exceptions of the constructor.
     *  @param based    Is first parameter a this$n?
     */
    JCTree DefaultConstructor(TreeMaker make,
                            ClassSymbol c,
                            List<Type> typarams,
                            List<Type> argtypes,
                            List<Type> thrown,
                            long flags,
                            boolean based) {
        DEBUG.P(this,"DefaultConstructor(7)");                    	
        List<JCVariableDecl> params = make.Params(argtypes, syms.noSymbol);
        DEBUG.P("params="+params);
        DEBUG.P("flags="+Flags.toString(flags));
        List<JCStatement> stats = List.nil();
        if (c.type != syms.objectType)
            stats = stats.prepend(SuperCall(make, typarams, params, based));
        
        DEBUG.P("stats="+stats);
        
        if ((c.flags() & ENUM) != 0 &&
            (types.supertype(c.type).tsym == syms.enumSym ||
             target.compilerBootstrap(c))) {
            // constructors of true enums are private
            flags = (flags & ~AccessFlags) | PRIVATE | GENERATEDCONSTR;
        } else
            flags |= (c.flags() & AccessFlags) | GENERATEDCONSTR;
        if (c.name.len == 0) flags |= ANONCONSTR;
        JCTree result = make.MethodDef(
            make.Modifiers(flags),
            names.init,
            null,
            make.TypeParams(typarams),
            params,
            make.Types(thrown),
            make.Block(0, stats),
            null);
        
        DEBUG.P("flags="+Flags.toString(flags));
        DEBUG.P("result="+result);
        DEBUG.P(0,this,"DefaultConstructor(7)");
        return result;
    }
