    public JCClassDecl ClassDef(JCModifiers mods,
				Name name,
				List<JCTypeParameter> typarams,
				JCTree extending,
				List<JCExpression> implementing,
				List<JCTree> defs)
    {
        JCClassDecl tree = new JCClassDecl(mods,
				     name,
				     typarams,
				     extending,
				     implementing,
				     defs,
				     null);
        tree.pos = pos;
        return tree;
    }