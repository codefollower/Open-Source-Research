/* *************************************************************************
 * Check annotations
 **************************************************************************/

    /** Annotation types are restricted to primitives, String, an
     *  enum, an annotation, Class, Class<?>, Class<? extends
     *  Anything>, arrays of the preceding.
     */
    void validateAnnotationType(JCTree restype) {
        // restype may be null if an error occurred, so don't bother validating it
        if (restype != null) {
            validateAnnotationType(restype.pos(), restype.type);
        }
    }

    void validateAnnotationType(DiagnosticPosition pos, Type type) {
		if (type.isPrimitive()) return;
		if (types.isSameType(type, syms.stringType)) return;
		if ((type.tsym.flags() & Flags.ENUM) != 0) return;
		if ((type.tsym.flags() & Flags.ANNOTATION) != 0) return;
		if (types.lowerBound(type).tsym == syms.classType.tsym) return;
		if (types.isArray(type) && !types.isArray(types.elemtype(type))) {
			validateAnnotationType(pos, types.elemtype(type));
			return;
		}
		log.error(pos, "invalid.annotation.member.type");
    }

    /**
     * "It is also a compile-time error if any method declared in an
     * annotation type has a signature that is override-equivalent to
     * that of any public or protected method declared in class Object
     * or in the interface annotation.Annotation."
     *
     * @jls3 9.6 Annotation Types
     */
    void validateAnnotationMethod(DiagnosticPosition pos, MethodSymbol m) {
        for (Type sup = syms.annotationType; sup.tag == CLASS; sup = types.supertype(sup)) {
            Scope s = sup.tsym.members();
            for (Scope.Entry e = s.lookup(m.name); e.scope != null; e = e.next()) {
                if (e.sym.kind == MTH &&
                    (e.sym.flags() & (PUBLIC | PROTECTED)) != 0 &&
                    types.overrideEquivalent(m.type, e.sym.type))
                    log.error(pos, "intf.annotation.member.clash", e.sym, sup);
            }
        }
    }

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

    /** Is s a method symbol that overrides a method in a superclass? */
    boolean isOverrider(Symbol s) {
		try {//我加上的
		DEBUG.P(this,"isOverrider(Symbol s)");
		DEBUG.P("s="+s+"  s.kind="+Kinds.toString(s.kind)+" s.isStatic()="+s.isStatic());
		
        if (s.kind != MTH || s.isStatic()) //静态方法永远不会覆盖超类中的静态方法
            return false;
        MethodSymbol m = (MethodSymbol)s;
        TypeSymbol owner = (TypeSymbol)m.owner;
        
        DEBUG.P("m="+m);
        DEBUG.P("owner="+owner);
        
        for (Type sup : types.closure(owner.type)) {
            if (sup == owner.type)
                continue; // skip "this"
            Scope scope = sup.tsym.members();
            DEBUG.P("scope="+scope);
            for (Scope.Entry e = scope.lookup(m.name); e.scope != null; e = e.next()) {
                if (!e.sym.isStatic() && m.overrides(e.sym, owner, types, true))
                    return true;
            }
        }
        return false;
        
		}finally{//我加上的
		DEBUG.P(1,this,"isOverrider(Symbol s)");
		}  
    }

    /** Is the annotation applicable to the symbol? */
    boolean annotationApplicable(JCAnnotation a, Symbol s) {
		try {//我加上的
		DEBUG.P(this,"annotationApplicable(2)");
		DEBUG.P("a="+a);
		DEBUG.P("s="+s+"  s.kind="+Kinds.toString(s.kind)+" s.isStatic()="+s.isStatic());
		
		Attribute.Compound atTarget =
			a.annotationType.type.tsym.attribute(syms.annotationTargetType.tsym);
		
		DEBUG.P("atTarget="+atTarget);
		if (atTarget == null) return true;
		Attribute atValue = atTarget.member(names.value);
		DEBUG.P("atValue="+atValue);
		DEBUG.P("(!(atValue instanceof Attribute.Array))="+(!(atValue instanceof Attribute.Array)));
		if (!(atValue instanceof Attribute.Array)) return true; // error recovery
		Attribute.Array arr = (Attribute.Array) atValue;
		for (Attribute app : arr.values) {
			DEBUG.P("(!(app instanceof Attribute.Enum))="+(!(app instanceof Attribute.Enum)));
			if (!(app instanceof Attribute.Enum)) return true; // recovery
			Attribute.Enum e = (Attribute.Enum) app;
			
			DEBUG.P("s.kind="+Kinds.toString(s.kind));
			DEBUG.P("s.owner.kind="+Kinds.toString(s.owner.kind));
			DEBUG.P("s.flags()="+Flags.toString(s.flags()));
			DEBUG.P("e.value.name="+e.value.name);
			if (e.value.name == names.TYPE)
			{ if (s.kind == TYP) return true; }
			else if (e.value.name == names.FIELD)
			{ if (s.kind == VAR && s.owner.kind != MTH) return true; }
			else if (e.value.name == names.METHOD)
			{ if (s.kind == MTH && !s.isConstructor()) return true; }
			else if (e.value.name == names.PARAMETER)
			{	
				if (s.kind == VAR &&
				  s.owner.kind == MTH &&
				  (s.flags() & PARAMETER) != 0)
				return true;
			}
			else if (e.value.name == names.CONSTRUCTOR)
			{ if (s.kind == MTH && s.isConstructor()) return true; }
			else if (e.value.name == names.LOCAL_VARIABLE)
			{ if (s.kind == VAR && s.owner.kind == MTH &&
				  (s.flags() & PARAMETER) == 0)
				return true;
			}
			else if (e.value.name == names.ANNOTATION_TYPE)
			{ if (s.kind == TYP && (s.flags() & ANNOTATION) != 0)
				return true;
			}
			else if (e.value.name == names.PACKAGE)
			{ if (s.kind == PCK) return true; }
			else
			//在Annotate解析Target时发生了错误，导致e.value.name不是以上各项
			return true; // recovery
		}
		return false;
		
		}finally{//我加上的
		DEBUG.P(0,this,"annotationApplicable(2)");
		}
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
		DEBUG.P("a.args.tail="+a.args.tail);
		// special case: java.lang.annotation.Target must not have
		// repeated values in its value member
		if (a.annotationType.type.tsym != syms.annotationTargetType.tsym ||
			a.args.tail == null) //a.args.tail == null是@Target不加参数的情况
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

    void checkDeprecatedAnnotation(DiagnosticPosition pos, Symbol s) {
		/*
		当在javac命令行中启用“-Xlint:dep-ann”选项时，
		如果javadoc文档中有@deprecated，
		但是没有加“@Deprecated ”这个注释标记时，编译器就会发出警告
		*/
		DEBUG.P(this,"checkDeprecatedAnnotation(2)");
		if (allowAnnotations &&
			lint.isEnabled(Lint.LintCategory.DEP_ANN) &&
			(s.flags() & DEPRECATED) != 0 &&
			!syms.deprecatedType.isErroneous() &&
			s.attribute(syms.deprecatedType.tsym) == null) {
			log.warning(pos, "missing.deprecated.annotation");
		}
		DEBUG.P(0,this,"checkDeprecatedAnnotation(2)");
    }