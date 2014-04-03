    //参考jdk1.6.0docs/technotes/guides/language/enums.html
    /** EnumeratorDeclaration = AnnotationsOpt [TypeArguments] IDENTIFIER [ Arguments ] [ "{" ClassBody "}" ]
     */
    JCTree enumeratorDeclaration(Name enumName) {
    	DEBUG.P(this,"enumeratorDeclaration(Name enumName)");
        String dc = S.docComment();
        int flags = Flags.PUBLIC|Flags.STATIC|Flags.FINAL|Flags.ENUM;
        if (S.deprecatedFlag()) {
            flags |= Flags.DEPRECATED;
            S.resetDeprecatedFlag();
        }
        int pos = S.pos();
        List<JCAnnotation> annotations = annotationsOpt();
        JCModifiers mods = F.at(annotations.isEmpty() ? Position.NOPOS : pos).Modifiers(flags, annotations);
        
        /*在Java Language Specification, Third Edition
		 18.1. The Grammar of the Java Programming Language
		 中有如下定义:
		 EnumConstant:
      	 Annotations Identifier [Arguments] [ClassBody]
      	 所以上面的语法AnnotationsOpt [TypeArguments] IDENTIFIER是错误的
      	 
      	 类似“<?>SUPER("? super ")”这样的枚举常量是错语的(非法的表达式开始)
      	 */
        List<JCExpression> typeArgs = typeArgumentsOpt();//总是返回null
        int identPos = S.pos();
        Name name = ident();
        int createPos = S.pos();
        List<JCExpression> args = (S.token() == LPAREN)
            ? arguments() : List.<JCExpression>nil();
        JCClassDecl body = null;
        if (S.token() == LBRACE) {
        	/*如下代码片断:
        		public static enum MyBoundKind {
			    @Deprecated EXTENDS("? extends ") {
			    	 String toString() {
			    	 	return "extends"; 
			    	 }
			    },
			*/
            JCModifiers mods1 = F.at(Position.NOPOS).Modifiers(Flags.ENUM | Flags.STATIC);
            List<JCTree> defs = classOrInterfaceBody(names.empty, false);
            body = toP(F.at(identPos).AnonymousClassDef(mods1, defs));
        }
        if (args.isEmpty() && body == null)
            createPos = Position.NOPOS;
        JCIdent ident = F.at(Position.NOPOS).Ident(enumName);
        //每个枚举常量就相当于是此枚举类型的一个实例
        JCNewClass create = F.at(createPos).NewClass(null, typeArgs, ident, args, body);
        if (createPos != Position.NOPOS)
            storeEnd(create, S.prevEndPos());
        ident = F.at(Position.NOPOS).Ident(enumName);//注意这里与上面不是同一个JCIdent的实例
        JCTree result = toP(F.at(pos).VarDef(mods, name, ident, create));
        attach(result, dc);
        
        DEBUG.P(0,this,"enumeratorDeclaration(Name enumName)");
        return result;
    }