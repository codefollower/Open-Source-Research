    public JCClassDecl AnonymousClassDef(JCModifiers mods,
					 List<JCTree> defs)
    {
	return ClassDef(mods,
			names.empty,
			List.<JCTypeParameter>nil(),
			null,
			List.<JCExpression>nil(),
			defs);
    }