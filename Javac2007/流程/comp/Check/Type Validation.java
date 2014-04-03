/* *************************************************************************
 * Type Validation
 **************************************************************************/

    /** Validate a type expression. That is,
     *  check that all type arguments of a parametric type are within
     *  their bounds. This must be done in a second phase after type attributon
     *  since a class might have a subclass as type parameter bound. E.g:
     *
     *  class B<A extends C> { ... }
     *  class C extends B<C> { ... }
     *
     *  and we can't make sure that the bound is already attributed because
     *  of possible cycles.
     */
    private Validator validator = new Validator();

    /** Visitor method: Validate a type expression, if it is not null, catching
     *  and reporting any completion failures.
     */
    void validate(JCTree tree) {
		DEBUG.P(this,"validate(JCTree tree)");
		if (tree != null) {
			//DEBUG.P("tree.type="+tree.type);
			DEBUG.P("tree.tag="+tree.myTreeTag());
		}else DEBUG.P("tree=null");
		
		try {
			if (tree != null) tree.accept(validator);
		} catch (CompletionFailure ex) {
			completionError(tree.pos(), ex);
		}
		DEBUG.P(1,this,"validate(JCTree tree)");
    }

    /** Visitor method: Validate a list of type expressions.
     */
    void validate(List<? extends JCTree> trees) {
		DEBUG.P(this,"validate(List<? extends JCTree> trees)");
		DEBUG.P("trees.size="+trees.size());
		DEBUG.P("trees="+trees);
		for (List<? extends JCTree> l = trees; l.nonEmpty(); l = l.tail)
			validate(l.head);
		DEBUG.P(1,this,"validate(List<? extends JCTree> trees)");
    }

    /** Visitor method: Validate a list of type parameters.
     */
    void validateTypeParams(List<JCTypeParameter> trees) {
		DEBUG.P(this,"validateTypeParams(1)");
		DEBUG.P("trees="+trees);
		
		for (List<JCTypeParameter> l = trees; l.nonEmpty(); l = l.tail)
			validate(l.head);
		DEBUG.P(1,this,"validateTypeParams(1)");
    }

    /** A visitor class for type validation.
     */
    class Validator extends JCTree.Visitor {

        public void visitTypeArray(JCArrayTypeTree tree) {
			DEBUG.P(this,"visitTypeArray(1)");
			DEBUG.P("tree="+tree);
			validate(tree.elemtype);
			DEBUG.P(0,this,"visitTypeArray(1)");
		}

        public void visitTypeApply(JCTypeApply tree) {
			DEBUG.P(this,"visitTypeApply(1)");
			DEBUG.P("tree="+tree);
			DEBUG.P("tree.type="+tree.type);
			DEBUG.P("tree.type.tag="+TypeTags.toString(tree.type.tag));
				
			if (tree.type.tag == CLASS) {
				List<Type> formals = tree.type.tsym.type.getTypeArguments();
				List<Type> actuals = tree.type.getTypeArguments();
				List<JCExpression> args = tree.arguments;
				List<Type> forms = formals;
				ListBuffer<TypeVar> tvars_buf = new ListBuffer<TypeVar>();
				
				DEBUG.P("formals="+formals);
				DEBUG.P("actuals="+actuals);
				DEBUG.P("args="+args);
				
				// For matching pairs of actual argument types `a' and
				// formal type parameters with declared bound `b' ...
				while (args.nonEmpty() && forms.nonEmpty()) {
					validate(args.head);

					// exact type arguments needs to know their
					// bounds (for upper and lower bound
					// calculations).  So we create new TypeVars with
					// bounds substed with actuals.
					tvars_buf.append(types.substBound(((TypeVar)forms.head),
									  formals,
									  Type.removeBounds(actuals)));

					args = args.tail;
					forms = forms.tail;
				}

				args = tree.arguments;
				List<TypeVar> tvars = tvars_buf.toList();

				DEBUG.P("");
				DEBUG.P("tvars="+tvars);
				DEBUG.P("args ="+args);
				while (args.nonEmpty() && tvars.nonEmpty()) {
					DEBUG.P("");
					DEBUG.P("args.head.type="+args.head.type);
					DEBUG.P("args.head.type.tag="+TypeTags.toString(args.head.type.tag));
					// Let the actual arguments know their bound
					args.head.type.withTypeVar(tvars.head);
					args = args.tail;
					tvars = tvars.tail;
				}

				args = tree.arguments;
				tvars = tvars_buf.toList();
				while (args.nonEmpty() && tvars.nonEmpty()) {
					checkExtends(args.head.pos(),
						 args.head.type,
						 tvars.head);
					args = args.tail;
					tvars = tvars.tail;
				}
				
				DEBUG.P("");
				DEBUG.P("tree.type="+tree.type);
				DEBUG.P("tree.type.getEnclosingType()="+tree.type.getEnclosingType());
				DEBUG.P("tree.type.getEnclosingType().isRaw()="+tree.type.getEnclosingType().isRaw());
				DEBUG.P("tree.clazz="+tree.clazz);
				DEBUG.P("tree.clazz.tag="+tree.clazz.myTreeTag());

                // Check that this type is either fully parameterized, or
                // not parameterized at all.
                /*错误例子:
				bin\mysrc\my\test\Test.java:47: 类型的格式不正确，给出了普通类型的类型参数
                Test.MyTestInnerClass<?> myTestInnerClass =
                                     ^
				bin\mysrc\my\test\Test.java:47: improperly formed type, type parameters given on a raw type
                Test.MyTestInnerClass<?> myTestInnerClass =
                                     ^
				*/
                if (tree.type.getEnclosingType().isRaw())
                    log.error(tree.pos(), "improperly.formed.type.inner.raw.param");
                if (tree.clazz.tag == JCTree.SELECT)
                    visitSelectInternal((JCFieldAccess)tree.clazz);
			}

			DEBUG.P(0,this,"visitTypeApply(1)");
		}

		public void visitTypeParameter(JCTypeParameter tree) {
			DEBUG.P(this,"visitTypeParameter(1)");
			DEBUG.P("tree="+tree);
			validate(tree.bounds);
			checkClassBounds(tree.pos(), tree.type);
			DEBUG.P(0,this,"visitTypeParameter(1)");
		}

		@Override
        public void visitWildcard(JCWildcard tree) {
			DEBUG.P(this,"visitWildcard(1)");
			DEBUG.P("tree="+tree);
			if (tree.inner != null)
			validate(tree.inner);
			DEBUG.P(0,this,"visitWildcard(1)");
		}

        public void visitSelect(JCFieldAccess tree) {
			DEBUG.P(this,"visitSelect(1)");
			DEBUG.P("tree="+tree);
			DEBUG.P("tree.type.tag="+TypeTags.toString(tree.type.tag));
			
			if (tree.type.tag == CLASS) {
					visitSelectInternal(tree);

					// Check that this type is either fully parameterized, or
					// not parameterized at all.
					DEBUG.P("tree.selected.type.isParameterized()="+tree.selected.type.isParameterized());
					DEBUG.P("tree.type.tsym.type.getTypeArguments().nonEmpty()="+tree.type.tsym.type.getTypeArguments().nonEmpty());
					if (tree.selected.type.isParameterized() && tree.type.tsym.type.getTypeArguments().nonEmpty())
						log.error(tree.pos(), "improperly.formed.type.param.missing");
			}
			
			DEBUG.P(0,this,"visitSelect(1)");
		}
        public void visitSelectInternal(JCFieldAccess tree) {
        	DEBUG.P(this,"visitSelectInternal(1)");
        	DEBUG.P("tree.type.getEnclosingType().tag="+TypeTags.toString(tree.type.getEnclosingType().tag));
        	DEBUG.P("tree.selected.type.isParameterized()="+tree.selected.type.isParameterized());
            DEBUG.P("tree.selected.type.allparams()="+tree.selected.type.allparams());
            if (tree.type.getEnclosingType().tag != CLASS &&
                tree.selected.type.isParameterized()) {
                /*错误例子:
                bin\mysrc\my\test\Test.java:7: 无法从参数化的类型中选择静态类
				public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
				extends my.ExtendsTest<String>.MyInnerClassStatic {
				
				                              ^
				1 错误
				
				打印结果:
				com.sun.tools.javac.comp.Check$Validator===>visitSelect(1)
				-------------------------------------------------------------------------
				tree=my.ExtendsTest<String>.MyInnerClassStatic
				tree.type.tag=CLASS
				com.sun.tools.javac.comp.Check$Validator===>visitSelectInternal(1)
				-------------------------------------------------------------------------
				tree.type.getEnclosingType().tag=NONE
				tree.selected.type.isParameterized()=true
				tree.selected.type.allparams()=java.lang.String
				com.sun.tools.javac.comp.Check$Validator===>visitSelectInternal(1)  END
				-------------------------------------------------------------------------
				com.sun.tools.javac.comp.Check$Validator===>visitSelect(1)  END
				-------------------------------------------------------------------------
                */
                
                // The enclosing type is not a class, so we are
                // looking at a static member type.  However, the
                // qualifying expression is parameterized.
                log.error(tree.pos(), "cant.select.static.class.from.param.type");
            } else {
                // otherwise validate the rest of the expression
                validate(tree.selected);
            }
            
            DEBUG.P(0,this,"visitSelectInternal(1)");
        }

		/** Default visitor method: do nothing.
		 */
		public void visitTree(JCTree tree) {
			DEBUG.P(this,"visitTree(1)");
			DEBUG.P("tree="+tree);
			DEBUG.P("do nothing");
			DEBUG.P(0,this,"visitTree(1)");
		}
    }