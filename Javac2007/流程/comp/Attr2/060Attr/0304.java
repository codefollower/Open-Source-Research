    /** Fetch a particular annotation from a symbol. */
    public Attribute.Compound attribute(Symbol anno) {
    	try {//我加上的
		DEBUG.P(this,"attribute(Symbol anno)");
		DEBUG.P("this="+toString());
		DEBUG.P("anno="+anno);
		
        for (Attribute.Compound a : getAnnotationMirrors())
            if (a.type.tsym == anno) return a;
        return null;
        
        }finally{//我加上的
		DEBUG.P(0,this,"attribute(Symbol anno)");
		}
    }

		public List<Attribute.Compound> getAnnotationMirrors() {
        	try {//我加上的
			DEBUG.P(this,"getAnnotationMirrors()");
		
            if (completer != null) complete();
            assert attributes_field != null;
            return attributes_field;
            
            }finally{//我加上的
	        DEBUG.P("attributes_field="+attributes_field);
			DEBUG.P(0,this,"getAnnotationMirrors()");
			}
        }
	
	/** The attributes of this symbol.
     */
    public List<Attribute.Compound> attributes_field;

    /** An accessor method for the attributes of this symbol.
     *  Attributes of class symbols should be accessed through the accessor
     *  method to make sure that the class symbol is loaded.
     */
    public List<Attribute.Compound> getAnnotationMirrors() {
    	try {//我加上的
		DEBUG.P(this,"getAnnotationMirrors()");
		
        assert attributes_field != null;
        return attributes_field;
        
        }finally{//我加上的
        DEBUG.P("attributes_field="+attributes_field);
		DEBUG.P(0,this,"getAnnotationMirrors()");
		}
    }

