//rank
    // <editor-fold defaultstate="collapsed" desc="rank">
    /**
     * The rank of a class is the length of the longest path between
     * the class and java.lang.Object in the class inheritance
     * graph. Undefined for all but reference types.
     */
    public int rank(Type t) { //只有ClassType、TypeVar有rank_field字段
		try {//我加上的
		DEBUG.P(this,"rank(Type t)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));

        switch(t.tag) {
        case CLASS: {
            ClassType cls = (ClassType)t;
			DEBUG.P("cls.rank_field="+cls.rank_field);
            if (cls.rank_field < 0) {
                Name fullname = cls.tsym.getQualifiedName();
				DEBUG.P("fullname="+fullname);
                if (fullname == fullname.table.java_lang_Object)
                    cls.rank_field = 0;
                else {
                    int r = rank(supertype(cls));
                    for (List<Type> l = interfaces(cls);
                         l.nonEmpty();
                         l = l.tail) {
                        if (rank(l.head) > r)
                            r = rank(l.head);
                    }
                    cls.rank_field = r + 1;
                }
            }
			DEBUG.P("cls.rank_field="+cls.rank_field);
            return cls.rank_field;
        }
        case TYPEVAR: {
            TypeVar tvar = (TypeVar)t;
			DEBUG.P("tvar.rank_field="+tvar.rank_field);
            if (tvar.rank_field < 0) {
                int r = rank(supertype(tvar));
                for (List<Type> l = interfaces(tvar);
                     l.nonEmpty();
                     l = l.tail) {
                    if (rank(l.head) > r) r = rank(l.head);
                }
                tvar.rank_field = r + 1;
            }
			DEBUG.P("tvar.rank_field="+tvar.rank_field);
            return tvar.rank_field;
        }
        case ERROR:
            return 0;
        default:
            throw new AssertionError();
        }

		}finally{//我加上的
		DEBUG.P(0,this,"rank(Type t)");
		}
    }
    // </editor-fold>
//