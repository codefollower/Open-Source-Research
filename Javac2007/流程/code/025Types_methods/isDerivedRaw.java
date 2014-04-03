//isDerivedRaw
    // <editor-fold defaultstate="collapsed" desc="isDerivedRaw">
    Map<Type,Boolean> isDerivedRawCache = new HashMap<Type,Boolean>();

    public boolean isDerivedRaw(Type t) {
    	DEBUG.P(this,"isDerivedRaw(Type t)");
		DEBUG.P("t="+t+" t.tag="+TypeTags.toString(t.tag));
		
        Boolean result = isDerivedRawCache.get(t);
        if (result == null) {
            result = isDerivedRawInternal(t);
            isDerivedRawCache.put(t, result);
        }
        
        DEBUG.P("result="+result);
        DEBUG.P(0,this,"isDerivedRaw(Type t)");
        return result;
    }

    public boolean isDerivedRawInternal(Type t) {
    	try {//我加上的
		DEBUG.P(this,"isDerivedRawInternal(Type t)");
		DEBUG.P("t.isErroneous()="+t.isErroneous());
		DEBUG.P("t.isRaw()="+t.isRaw());

        if (t.isErroneous())
            return false;
        return
            t.isRaw() ||
            supertype(t) != null && isDerivedRaw(supertype(t)) ||
            isDerivedRaw(interfaces(t));
            
        }finally{//我加上的
		DEBUG.P(0,this,"isDerivedRawInternal(Type t)");
		}    
    }

    public boolean isDerivedRaw(List<Type> ts) {
    	try {//我加上的
		DEBUG.P(this,"isDerivedRaw(List<Type> ts)");
		DEBUG.P("ts="+ts);
		
        List<Type> l = ts;
        while (l.nonEmpty() && !isDerivedRaw(l.head)) l = l.tail;
        return l.nonEmpty();
        
        }finally{//我加上的
		DEBUG.P(0,this,"isDerivedRaw(List<Type> ts)");
		} 
    }
    // </editor-fold>
//