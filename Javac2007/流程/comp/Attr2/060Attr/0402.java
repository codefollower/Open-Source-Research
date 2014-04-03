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
		while (args.nonEmpty() && tvars.nonEmpty()) {
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
		
		DEBUG.P("tree.type.getEnclosingType()="+tree.type.getEnclosingType());
		DEBUG.P("tree.type.getEnclosingType().isRaw()="+tree.type.getEnclosingType().isRaw());
		DEBUG.P("tree.clazz.tag="+tree.clazz.myTreeTag());
		
                // Check that this type is either fully parameterized, or
                // not parameterized at all.
                if (tree.type.getEnclosingType().isRaw())
                    log.error(tree.pos(), "improperly.formed.type.inner.raw.param");
                if (tree.clazz.tag == JCTree.SELECT)
                    visitSelectInternal((JCFieldAccess)tree.clazz);
	    }
	    
	    DEBUG.P(0,this,"visitTypeApply(1)");
	}


	    /** Check that a type is within some bounds.
     *
     *  Used in TypeApply to verify that, e.g., X in V<X> is a valid
     *  type argument.
     *  @param pos           Position to be used for error reporting.
     *  @param a             The type that should be bounded by bs.
     *  @param bs            The bound.
     */
    private void checkExtends(DiagnosticPosition pos, Type a, TypeVar bs) {
    try {//我加上的
	DEBUG.P(this,"checkExtends(3)");
	DEBUG.P("a="+a);
	DEBUG.P("a.tag="+TypeTags.toString(a.tag));
	DEBUG.P("a.isUnbound()="+a.isUnbound());
	DEBUG.P("a.isExtendsBound()="+a.isExtendsBound());
	DEBUG.P("a.isSuperBound()="+a.isSuperBound());
	DEBUG.P("bs="+bs);

	if (a.isUnbound()) {
	    return;
	} else if (a.tag != WILDCARD) {
	    a = types.upperBound(a);
	    for (List<Type> l = types.getBounds(bs); l.nonEmpty(); l = l.tail) {
		if (!types.isSubtype(a, l.head)) {
		    log.error(pos, "not.within.bounds", a);
		    return;
		}
	    }
	} else if (a.isExtendsBound()) {
	    if (!types.isCastable(bs.getUpperBound(), types.upperBound(a), Warner.noWarnings))
		log.error(pos, "not.within.bounds", a);
	} else if (a.isSuperBound()) {
	    if (types.notSoftSubtype(types.lowerBound(a), bs.getUpperBound()))
		log.error(pos, "not.within.bounds", a);
	}
	
	}finally{//我加上的
	DEBUG.P(0,this,"checkExtends(3)");
	}
    }
