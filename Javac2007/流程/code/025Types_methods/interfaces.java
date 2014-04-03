//interfaces
    // <editor-fold defaultstate="collapsed" desc="interfaces">
    /**
     * Return the interfaces implemented by this class.
     */
    public List<Type> interfaces(Type t) {
        //return interfaces.visit(t);

		DEBUG.P(this,"interfaces(Type t)");
		//DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		
		List<Type> returnTypes = interfaces.visit(t);
            
		//DEBUG.P("returnTypes="+returnTypes);

		DEBUG.P("t="+t);
		DEBUG.P("interfaces="+interfaces);
		DEBUG.P(1,this,"interfaces(Type t)");
		return returnTypes;
    }
    // where
        private UnaryVisitor<List<Type>> interfaces = new UnaryVisitor<List<Type>>() {

            public List<Type> visitType(Type t, Void ignored) {
                return List.nil();
            }

            @Override
            public List<Type> visitClassType(ClassType t, Void ignored) {
				//DEBUG.P(this,"visitClassType(2)");
            	//DEBUG.P("t.interfaces_field前="+t.interfaces_field);
                if (t.interfaces_field == null) {
					DEBUG.P("t.interfaces_field == null");
                    List<Type> interfaces = ((ClassSymbol)t.tsym).getInterfaces();
                    //为什么这里再一次判断t.interfaces_field是否为null呢?
                    //因为getInterfaces()内部会调用complete()，
                    //从而导致t.interfaces_field的值不确定
                    //参考supertype(Type t)
					DEBUG.P("t.interfaces_field中="+t.interfaces_field);
                    if (t.interfaces_field == null) {
                        // If t.interfaces_field is null, then t must
                        // be a parameterized type (not to be confused
                        // with a generic type declaration).
                        // Terminology:
                        //    Parameterized type: List<String>
                        //    Generic type declaration: class List<E> { ... }
                        // So t corresponds to List<String> and
                        // t.tsym.type corresponds to List<E>.
                        // The reason t must be parameterized type is
                        // that completion will happen as a side
                        // effect of calling
                        // ClassSymbol.getInterfaces.  Since
                        // t.interfaces_field is null after
                        // completion, we can assume that t is not the
                        // type of a class/interface declaration.
                        assert t != t.tsym.type : t.toString();
                        List<Type> actuals = t.allparams();
                        List<Type> formals = t.tsym.type.allparams();

						DEBUG.P("");
                    	DEBUG.P("t.interfaces_field == null   t="+t);
                    	DEBUG.P("interfaces="+interfaces);
                    	DEBUG.P("actuals   ="+actuals);
                    	DEBUG.P("formals   ="+formals);
                    	DEBUG.P("");

                        if (actuals.isEmpty()) {
                            if (formals.isEmpty()) {
                                // In this case t is not generic (nor raw).
                                // So this should not happen.
                                t.interfaces_field = interfaces;
                            } else {
                                t.interfaces_field = erasure(interfaces);
                            }
                        } else {
                            t.interfaces_field =
                                upperBounds(subst(interfaces, formals, actuals));
                        }
                    }
                }

				//DEBUG.P("t.interfaces_field后="+t.interfaces_field);
                //DEBUG.P(0,this,"visitClassType(2)");
                return t.interfaces_field;
            }

            @Override
            public List<Type> visitTypeVar(TypeVar t, Void ignored) {
            	try {//我加上的
				DEBUG.P(this,"visitTypeVar(2)");
				DEBUG.P("t.bound="+t.bound);
				DEBUG.P("t.bound.tag="+TypeTags.toString(t.bound.tag));
				DEBUG.P("t.bound.isCompound() ="+t.bound.isCompound());
				DEBUG.P("t.bound.isInterface()="+t.bound.isInterface());
				
                if (t.bound.isCompound())
                    return interfaces(t.bound);

                if (t.bound.isInterface())
                    return List.of(t.bound);

                return List.nil();
                
                }finally{//我加上的
				DEBUG.P(0,this,"visitTypeVar(2)");
				}
            }
        };
    // </editor-fold>
//