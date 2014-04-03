    /** Create a a list of value parameter trees x0, ..., xn from a list of
     *  their types and an their owner.
     */
    public List<JCVariableDecl> Params(List<Type> argtypes, Symbol owner) {
    	DEBUG.P(this,"Params(2)");  
    	DEBUG.P("argtypes="+argtypes);
        DEBUG.P("owner="+owner);
        DEBUG.P("owner.kind="+Kinds.toString(owner.kind));
        ListBuffer<JCVariableDecl> params = new ListBuffer<JCVariableDecl>();
        MethodSymbol mth = (owner.kind == MTH) ? ((MethodSymbol)owner) : null;
        if (mth != null && mth.params != null && argtypes.length() == mth.params.length()) {
            for (VarSymbol param : ((MethodSymbol)owner).params)
                params.append(VarDef(param, null));
        } else {
            int i = 0;
            for (List<Type> l = argtypes; l.nonEmpty(); l = l.tail)
                params.append(Param(paramName(i++), l.head, owner));
        }
        DEBUG.P(0,this,"Params(2)");  
        return params.toList();
    }

	/** The name of synthetic parameter number `i'.
     */
    public Name paramName(int i)   { return names.fromString("x" + i); }

    /** Create a value parameter tree from its name, type, and owner.
     */
    public JCVariableDecl Param(Name name, Type argtype, Symbol owner) {
        return VarDef(new VarSymbol(0, name, argtype, owner), null);
    }

    /** Create a variable definition from a variable symbol and an initializer
     *  expression.
     */
    public JCVariableDecl VarDef(VarSymbol v, JCExpression init) {
        return (JCVariableDecl)
            new JCVariableDecl(
                Modifiers(v.flags(), Annotations(v.getAnnotationMirrors())),
                v.name,
                Type(v.type),
                init,
                v).setPos(pos).setType(v.type);
    }

    /**
     * A variable definition.
     * @param modifiers variable modifiers
     * @param name variable name
     * @param vartype type of the variable
     * @param init variables initial value
     * @param sym symbol
     */
    public static class JCVariableDecl extends JCStatement implements VariableTree {
        public JCModifiers mods;
        public Name name;
        public JCExpression vartype;
        public JCExpression init;
        public VarSymbol sym;
        protected JCVariableDecl(JCModifiers mods,
			 Name name,
			 JCExpression vartype,
			 JCExpression init,
			 VarSymbol sym) {
            super(VARDEF);
            this.mods = mods;
            this.name = name;
            this.vartype = vartype;
            this.init = init;
            this.sym = sym;
        }
        @Override
        public void accept(Visitor v) { v.visitVarDef(this); }

        public Kind getKind() { return Kind.VARIABLE; }
        public JCModifiers getModifiers() { return mods; }
        public Name getName() { return name; }
        public JCTree getType() { return vartype; }
        public JCExpression getInitializer() {
            return init;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitVariable(this, d);
        }
    }