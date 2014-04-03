//Greatest lower bound
    // <editor-fold defaultstate="collapsed" desc="Greatest lower bound">
    public Type glb(Type t, Type s) {
		try {//我加上的
		DEBUG.P(this,"glb(2)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

        if (s == null)
            return t;
        else if (isSubtypeNoCapture(t, s))
            return t;
        else if (isSubtypeNoCapture(s, t))
            return s;

        List<Type> closure = union(closure(t), closure(s));
        List<Type> bounds = closureMin(closure);

		DEBUG.P("closure="+closure);
		DEBUG.P("bounds="+bounds);

        if (bounds.isEmpty()) {             // length == 0
            return syms.objectType;
        } else if (bounds.tail.isEmpty()) { // length == 1
            return bounds.head;
        } else {                            // length > 1
            int classCount = 0;
            for (Type bound : bounds)
                if (!bound.isInterface())
                    classCount++;
			DEBUG.P("classCount="+classCount);
            if (classCount > 1)
                return syms.errType;
        }
        return makeCompoundType(bounds);

		}finally{//我加上的
		DEBUG.P(0,this,"glb(2)");
		}
    }
    // </editor-fold>
//