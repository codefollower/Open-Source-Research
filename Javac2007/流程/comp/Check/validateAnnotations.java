    /** Check the annotations of a symbol.
     */
    public void validateAnnotations(List<JCAnnotation> annotations, Symbol s) {
		try {//我加上的
		DEBUG.P(this,"validateAnnotations(2)");
		//DEBUG.P("暂时跳过注释，不检测");
		
		DEBUG.P("annotations="+annotations);
		DEBUG.P("s="+s);
		DEBUG.P("skipAnnotations="+skipAnnotations);
		
		
		if (skipAnnotations) return;
		for (JCAnnotation a : annotations)
			validateAnnotation(a, s);
		   
		}finally{//我加上的
		DEBUG.P(2,this,"validateAnnotations(2)");
		}
    }

    /** Check an annotation of a symbol.
     */
    public void validateAnnotation(JCAnnotation a, Symbol s) {
		DEBUG.P(this,"validateAnnotation(2)");
		DEBUG.P("a="+a);
		DEBUG.P("s="+s);
		
		validateAnnotation(a);
		/*
		if (!annotationApplicable(a, s))
			log.error(a.pos(), "annotation.type.not.applicable");
		if (a.annotationType.type.tsym == syms.overrideType.tsym) {
			if (!isOverrider(s))
			log.error(a.pos(), "method.does.not.override.superclass");
		}
		*/
		
		//下面两个log.error()的位置都是a.pos()，所以当两个同时出现时，只报告一个错误
		boolean annotationApplicableFlag=annotationApplicable(a, s);
		DEBUG.P("annotationApplicableFlag="+annotationApplicableFlag);
		if (!annotationApplicableFlag)
			log.error(a.pos(), "annotation.type.not.applicable");

		DEBUG.P("a.annotationType.type.tsym="+a.annotationType.type.tsym);
		DEBUG.P("syms.overrideType.tsym="+syms.overrideType.tsym);
		if (a.annotationType.type.tsym == syms.overrideType.tsym) {
			boolean isOverriderFlag=isOverrider(s);
			DEBUG.P("isOverriderFlag="+isOverriderFlag);
			if (!isOverriderFlag)
				log.error(a.pos(), "method.does.not.override.superclass");
		}
		
		DEBUG.P(1,this,"validateAnnotation(2)");
    }
    /** Check an annotation value.
     */
    public void validateAnnotation(JCAnnotation a) {
		try {//我加上的
		DEBUG.P(this,"validateAnnotation(1)");
		DEBUG.P("a="+a);
		DEBUG.P("a.type="+a.type);
		DEBUG.P("a.type.isErroneous()="+a.type.isErroneous());

        if (a.type.isErroneous()) return;

		DEBUG.P("");
		DEBUG.P("a.annotationType.type.tsym="+a.annotationType.type.tsym);
		DEBUG.P("a.annotationType.type.tsym.members()="+a.annotationType.type.tsym.members());
		// collect an inventory of the members
		Set<MethodSymbol> members = new HashSet<MethodSymbol>();
		for (Scope.Entry e = a.annotationType.type.tsym.members().elems;
			 e != null;
			 e = e.sibling)
			if (e.sym.kind == MTH)
					members.add((MethodSymbol) e.sym);
		DEBUG.P("members="+members);

		DEBUG.P("");
		DEBUG.P("a.args="+a.args);
		DEBUG.P("for...............开始");
		// count them off as they're annotated
		for (JCTree arg : a.args) {
			DEBUG.P("arg.tag="+arg.myTreeTag());

			if (arg.tag != JCTree.ASSIGN) continue; // recovery
			JCAssign assign = (JCAssign) arg;
			Symbol m = TreeInfo.symbol(assign.lhs);

			DEBUG.P("m="+m);

			if (m == null || m.type.isErroneous()) continue;
			/*
			检查注释成员值是否有重复，有重复，
			则编译器会报一个关键字为“duplicate.annotation.member.value”的错误。
			
			如下源代码:
			--------------------------------------------------------------------
			package my.error;
			@interface MyAnnotation {
				String value();
			}
			@MyAnnotation(value="testA",value="testB")
			public class duplicate_annotation_member_value  {}
			--------------------------------------------------------------------
			
			编译错误提示信息如下:
			--------------------------------------------------------------------
			bin\mysrc\my\error\duplicate_annotation_member_value.java:5: my.error.MyAnnotation 中的注释成员值 value 重复
			@MyAnnotation(value="testA",value="testB")
											  ^
			1 错误
			--------------------------------------------------------------------
			
			因为members=[value()]，a.args却有两个value，
			所以第二次members.remove(m)时将返回false
			(也就是value()在第一次for循环时已删除，在第二次for循环时已不存在)
			*/
			if (!members.remove(m))
			log.error(arg.pos(), "duplicate.annotation.member.value",
				  m.name, a.type);

			DEBUG.P("assign.rhs.tag="+assign.rhs.myTreeTag());

			if (assign.rhs.tag == ANNOTATION)
			validateAnnotation((JCAnnotation)assign.rhs);
		}
		DEBUG.P("for...............结束");

		DEBUG.P("");
		DEBUG.P("members="+members);

		// all the remaining ones better have default values
		for (MethodSymbol m : members)
			if (m.defaultValue == null && !m.type.isErroneous())
			log.error(a.pos(), "annotation.missing.default.value", 
							  a.type, m.name);

		DEBUG.P("a.annotationType.type.tsym="+a.annotationType.type.tsym);
		DEBUG.P("syms.annotationTargetType.tsym="+syms.annotationTargetType.tsym);
		// special case: java.lang.annotation.Target must not have
		// repeated values in its value member
		if (a.annotationType.type.tsym != syms.annotationTargetType.tsym ||
			a.args.tail == null)
			return;
			
		DEBUG.P("a.args.head.tag="+a.args.head.myTreeTag());
		
			if (a.args.head.tag != JCTree.ASSIGN) return; // error recovery
		JCAssign assign = (JCAssign) a.args.head;
		Symbol m = TreeInfo.symbol(assign.lhs);
		
		DEBUG.P("m.name="+m.name);
		
		if (m.name != names.value) return;
		JCTree rhs = assign.rhs;
		
		DEBUG.P("rhs.tag="+rhs.myTreeTag());
		
		if (rhs.tag != JCTree.NEWARRAY) return;
		JCNewArray na = (JCNewArray) rhs;
		Set<Symbol> targets = new HashSet<Symbol>();
		for (JCTree elem : na.elems) {
			if (!targets.add(TreeInfo.symbol(elem))) {
			log.error(elem.pos(), "repeated.annotation.target");
			}
		}
		
		}finally{//我加上的
		DEBUG.P(1,this,"validateAnnotation(1)");
		}
    }