        public void visitTypeApply(JCTypeApply tree) {
        DEBUG.P(this,"visitTypeApply(1)");
        DEBUG.P("tree="+tree);
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