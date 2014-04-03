    /**
     * Returns the result of combining the values in this object with 
     * the given annotations and flags.
     */
    public Lint augment(List<Attribute.Compound> attrs, long flags) {
    DEBUG.P(this,"augment(2)");
    DEBUG.P("attrs="+attrs);
    DEBUG.P("flags="+Flags.toString(flags));
    
	Lint l = augmentor.augment(this, attrs);
	
	//如果当前对象(如方法或类等)已加了“@Deprecated”这个注释标记，
	//那么在往下的程序中如果使用到了其他加了“@Deprecated”的对象，
	//这时不再警告，因为当前对象本身已不赞成使用。
	if ((flags & DEPRECATED) != 0) {//flags是DEPRECATED的情况
	    if (l == this)
		l = new Lint(this);
	    l.values.remove(LintCategory.DEPRECATION);
	    l.suppressedValues.add(LintCategory.DEPRECATION);
	}
	
	DEBUG.P("return lint="+l);
	DEBUG.P(0,this,"augment(2)");
	return l;
    }

	Lint augment(Lint parent, List<Attribute.Compound> attrs) {
		try {//我加上的
		DEBUG.P(this,"augment(2)");
		DEBUG.P("attrs="+attrs);
		DEBUG.P("lint  ="+lint);
		DEBUG.P("parent="+parent);

	    initSyms();
	    this.parent = parent;
	    lint = null;
	    for (Attribute.Compound a: attrs) {
		a.accept(this);
	    }
	    return (lint == null ? parent : lint);
	    
	    }finally{//我加上的
	    DEBUG.P("");
		DEBUG.P("lint  ="+lint);
		DEBUG.P("parent="+parent);
		DEBUG.P(0,this,"augment(2)");
		}
	}

	// If we find a @SuppressWarnings annotation, then we continue
	// walking the tree, in order to suppress the individual warnings
	// specified in the @SuppressWarnings annotation.
	public void visitCompound(Attribute.Compound compound) {
		DEBUG.P(this,"visitCompound(1)");
		DEBUG.P("compound="+compound);
		DEBUG.P("compound.type.tsym="+compound.type.tsym);
		DEBUG.P("syms.suppressWarningsType.tsym="+syms.suppressWarningsType.tsym);
		
	    if (compound.type.tsym == syms.suppressWarningsType.tsym) {
		for (List<Pair<MethodSymbol,Attribute>> v = compound.values;
		     v.nonEmpty(); v = v.tail) {
		    Pair<MethodSymbol,Attribute> value = v.head;
		    if (value.fst.name.toString().equals("value")) 
			value.snd.accept(this);
		}
		
	    }
	    
	    DEBUG.P(0,this,"visitCompound(1)");
	}

	public void visitArray(Attribute.Array array) {
		DEBUG.P(this,"visitArray(1)");
		
	    for (Attribute value : array.values) 
		value.accept(this);
		
		DEBUG.P(0,this,"visitArray(1)");
	}
	public void visitConstant(Attribute.Constant value) {
		DEBUG.P(this,"visitConstant(1)");
		DEBUG.P("value="+value);
	    if (value.type.tsym == syms.stringType.tsym) {
		LintCategory lc = LintCategory.get((String) (value.value));
		if (lc != null) 
		    suppress(lc);
	    }
	    DEBUG.P(0,this,"visitConstant(1)");
	}

	private void suppress(LintCategory lc) {
		DEBUG.P(this,"suppress(1)");
		DEBUG.P("lc="+lc);
		DEBUG.P("lint="+lint);
		
	    if (lint == null) 
		lint = new Lint(parent);
	    lint.suppressedValues.add(lc);
	    lint.values.remove(lc);
	    
	    DEBUG.P("");
	    DEBUG.P("lint="+lint);
	    DEBUG.P(0,this,"suppress(1)");
	}
