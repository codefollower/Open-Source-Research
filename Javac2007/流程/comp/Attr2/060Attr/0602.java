        /** The implementation of this (abstract) symbol in class origin;
         *  null if none exists. Synthetic methods are not considered
         *  as possible implementations.
         */
        public MethodSymbol implementation(TypeSymbol origin, Types types, boolean checkResult) {
        	//当前的MethodSymbol代表一个抽象方法，检查origin类及超类中是否实现了该方法
        	try {//我加上的
			DEBUG.P(this,"implementation(3)");
			DEBUG.P("TypeSymbol origin="+origin);
			DEBUG.P("boolean checkResult="+checkResult);
			
            for (Type t = origin.type; t.tag == CLASS; t = types.supertype(t)) {
                TypeSymbol c = t.tsym;
                DEBUG.P("第一层for:");
                DEBUG.P("TypeSymbol c="+c);
                DEBUG.P("c.members()="+c.members());
                DEBUG.P("lookup(name)="+name);
                DEBUG.P("t.tag="+TypeTags.toString(t.tag));
                for (Scope.Entry e = c.members().lookup(name);
                     e.scope != null;
                     e = e.next()) {
                    DEBUG.P("第二层for:");
                    DEBUG.P("e.sym="+e.sym);
                    DEBUG.P("e.scope="+e.scope);
                    DEBUG.P("e.sym.kind="+Kinds.toString(e.sym.kind));
                    if (e.sym.kind == MTH) {
                        MethodSymbol m = (MethodSymbol) e.sym;
                        
						//m有可能是原始实现类(origin)或者超类中的方法，this是被实现的抽象方法
                        boolean overrides=m.overrides(this, origin, types, checkResult);
						//如果非abstract类中含有abstract方法，m和this是指向这个非abstract类中
						//的同一个方法，在调用overrides方法时，
						//有一条“if (this == _other) return true;”的语句，
						//也就是说，直接就认为他们相互覆盖。
                        DEBUG.P("overrides="+overrides);
                        if(overrides) {
                        	if((m.flags() & SYNTHETIC) == 0) {
                        		DEBUG.P(m+".flags() 没有SYNTHETIC");
                        		return m;
                        	}
                        }
                        /*		
                        if (m.overrides(this, origin, types, checkResult) &&
                            (m.flags() & SYNTHETIC) == 0)
                            return m;
                            */
                    }
                }
            }
            DEBUG.P("结束第一层for");
            DEBUG.P("origin.type="+origin.type);
            // if origin is derived from a raw type, we might have missed
            // an implementation because we do not know enough about instantiations.
            // in this case continue with the supertype as origin.
            if (types.isDerivedRaw(origin.type))
                return implementation(types.supertype(origin.type).tsym, types, checkResult);
            else
                return null;
                
            }finally{//我加上的
			DEBUG.P(0,this,"implementation(3)");
			}
        }