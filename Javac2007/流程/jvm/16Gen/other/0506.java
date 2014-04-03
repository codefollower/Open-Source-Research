        public Type withTypeVar(Type t) {
        	try {//我加上的
			DEBUG.P(this,"withTypeVar(Type t)");
			DEBUG.P("bound="+bound);
			DEBUG.P("t    ="+t);
			
            //-System.err.println(this+".withTypeVar("+t+");");//DEBUG
            if (bound == t)
                return this;
            bound = (TypeVar)t;
            return this;
            
            }finally{//我加上的
            DEBUG.P("");
            DEBUG.P("泛型类形参："+bound);
            DEBUG.P("泛型类实参："+this);
			DEBUG.P(1,this,"withTypeVar(Type t)");
			}
        }