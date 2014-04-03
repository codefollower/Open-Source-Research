    public List<Type> substBounds(List<Type> tvars,
                                  List<Type> from,
                                  List<Type> to) {
        if (tvars.isEmpty())
            return tvars;
        if (tvars.tail.isEmpty())
            // fast common case
            return List.<Type>of(substBound((TypeVar)tvars.head, from, to));
        ListBuffer<Type> newBoundsBuf = lb();
        boolean changed = false;
        // calculate new bounds
        for (Type t : tvars) {
            TypeVar tv = (TypeVar) t;
            Type bound = subst(tv.bound, from, to);
            if (bound != tv.bound)
                changed = true;
            newBoundsBuf.append(bound);
        }
        if (!changed)
            return tvars;
        ListBuffer<Type> newTvars = lb();
        // create new type variables without bounds
        for (Type t : tvars) {
            newTvars.append(new TypeVar(t.tsym, null));
        }
        // the new bounds should use the new type variables in place
        // of the old
        List<Type> newBounds = newBoundsBuf.toList();
        from = tvars;
        to = newTvars.toList();
        for (; !newBounds.isEmpty(); newBounds = newBounds.tail) {
            newBounds.head = subst(newBounds.head, from, to);
        }
        newBounds = newBoundsBuf.toList();
        // set the bounds of new type variables to the new bounds
        for (Type t : newTvars.toList()) {
            TypeVar tv = (TypeVar) t;
            tv.bound = newBounds.head;
            newBounds = newBounds.tail;
        }
        return newTvars.toList();
    }

    public TypeVar substBound(TypeVar t, List<Type> from, List<Type> to) {
    	try {//我加上的
		DEBUG.P(this,"substBound(3)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("from="+from);
		DEBUG.P("to="+to);
		
        Type bound1 = subst(t.bound, from, to);
        DEBUG.P("(bound1 == t.bound)="+(bound1 == t.bound));
        if (bound1 == t.bound)
            return t;
        else
            return new TypeVar(t.tsym, bound1);
            
        }finally{//我加上的
		DEBUG.P(1,this,"substBound(3)");
		}
    }
    // </editor-fold>