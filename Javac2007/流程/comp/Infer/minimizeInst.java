    /** Instaniate undetermined type variable to the lub of all its lower bounds.
     *  Throw a NoInstanceException if this not possible.
     */
    void minimizeInst(UndetVar that, Warner warn) throws NoInstanceException {
		try {//我加上的
		DEBUG.P(this,"minimizeInst(2)");
		DEBUG.P("that="+that);
		DEBUG.P("that.inst="+that.inst);

		if (that.inst == null) {
			DEBUG.P("that.lobounds="+that.lobounds);
			if (that.lobounds.isEmpty())
				that.inst = syms.botType;
			else if (that.lobounds.tail.isEmpty())
				that.inst = that.lobounds.head;
			else {
				that.inst = types.lub(that.lobounds);
				if (that.inst == null)
					throw ambiguousNoInstanceException
					.setMessage("no.unique.minimal.instance.exists",
							that.qtype, that.lobounds);
			}
			DEBUG.P("that.inst="+that.inst);
			DEBUG.P("that.hibounds="+that.hibounds);

			// VGJ: sort of inlined maximizeInst() below.  Adding
			// bounds can cause lobounds that are above hibounds.
			if (that.hibounds.isEmpty())
				return;
			Type hb = null;
			if (that.hibounds.tail.isEmpty())
				hb = that.hibounds.head;
			else for (List<Type> bs = that.hibounds;
						bs.nonEmpty() && hb == null;
						bs = bs.tail) {
				if (isSubClass(bs.head, that.hibounds))
					hb = types.fromUnknownFun.apply(bs.head);
			}
			DEBUG.P("hb="+hb);
			if (hb == null ||
					!types.isSubtypeUnchecked(hb, that.hibounds, warn) ||
					!types.isSubtypeUnchecked(that.inst, hb, warn))
			throw ambiguousNoInstanceException;
		}

		}finally{//我加上的
		DEBUG.P(0,this,"minimizeInst(2)");
		}
    }