    /** Enter an inner class into the `innerClasses' set/queue.
     */
    void enterInner(ClassSymbol c) {
		try {//我加上的
		DEBUG.P(this,"enterInner(1)");
		DEBUG.P("c="+c);
		DEBUG.P("innerClasses前="+innerClasses);
		DEBUG.P("innerClassesQueue前="+innerClassesQueue);

        assert !c.type.isCompound();
        try {
            c.complete();
        } catch (CompletionFailure ex) {
            System.err.println("error: " + c + ": " + ex.getMessage());
            throw ex;
        }
		DEBUG.P("");
		DEBUG.P("c.type="+c.type+"  c.type.tag="+TypeTags.toString(c.type.tag));
		DEBUG.P("pool="+pool);
		if(pool != null) DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
        if (c.type.tag != CLASS) return; // arrays
        if (pool != null && // pool might be null if called from xClassName
            c.owner.kind != PCK &&
            (innerClasses == null || !innerClasses.contains(c))) {
				DEBUG.P("新增内部类");
//          log.errWriter.println("enter inner " + c);//DEBUG
            if (c.owner.kind == TYP) enterInner((ClassSymbol)c.owner);
            pool.put(c);
            pool.put(c.name);
            if (innerClasses == null) {
                innerClasses = new HashSet<ClassSymbol>();
                innerClassesQueue = new ListBuffer<ClassSymbol>();
                pool.put(names.InnerClasses);
            }
            innerClasses.add(c);
            innerClassesQueue.append(c);
        }

		}finally{//我加上的
		DEBUG.P("innerClasses后="+innerClasses);
		DEBUG.P("innerClassesQueue后="+innerClassesQueue);
		DEBUG.P(0,this,"enterInner(1)");
		}
    }