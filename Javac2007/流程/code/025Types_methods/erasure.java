//erasure
    // <editor-fold defaultstate="collapsed" desc="erasure">
    /**
     * The erasure of t {@code |t|} -- the type that results when all
     * type parameters in t are deleted.
     */
    /**
		所谓erasure，就是将type parameters去掉，例如Test<T>，erasure后就变为Test，
		完成erasure这个功能，实际上是由Type类及其子类相应的map(Mapping f)方法实现
		的(ClassType例外,ClassType用ClassSymbol.erasure(Types types)方法实现)，
		如果Type类及其子类带type parameters，将type parameters去掉后，重新
		用原来的Type类及其子类的实例各字段生成一个相应的实例，就得到erasure后的类型
		
		注:如果是ClassType的flags是COMPOUND，那么erasure在makeCompoundType方法
		中已经事先设置，当调用ClassSymbol.erasure(Types types)方法时就可直接返回
		erasure后的类型
		
		
		例:<E extends ExtendsTest&InterfaceTest>，返回类型变量E的erasure后的类型
		
		输出结果如下:
		
		com.sun.tools.javac.code.Types===>erasure(Type t)
		-------------------------------------------------------------------------
		t.tag=(TYPEVAR)14  lastBaseTag=8
		com.sun.tools.javac.code.Types===>erasure(Type t)
		-------------------------------------------------------------------------
		t.tag=(CLASS)10  lastBaseTag=8
		com.sun.tools.javac.code.Symbol$ClassSymbol===>erasure(Types types)
		-------------------------------------------------------------------------
		erasure_field=my.ExtendsTest  //erasure_field已经存在
		com.sun.tools.javac.code.Symbol$ClassSymbol===>erasure(Types types)  END
		-------------------------------------------------------------------------
		t=my.ExtendsTest,my.InterfaceTest  erasureType=my.ExtendsTest
		com.sun.tools.javac.code.Types===>erasure(Type t)  END
		-------------------------------------------------------------------------
		t=E23195919  erasureType=my.ExtendsTest
		com.sun.tools.javac.code.Types===>erasure(Type t)  END
		-------------------------------------------------------------------------
    */
    public Type erasure(Type t) {
    	//if (t.tag <= lastBaseTag)
        //    return t; /* fast special case */
        //else
        //    return erasure.visit(t);

		DEBUG.P(this,"erasure(Type t)");
		DEBUG.P("t="+t+"  t.tag=("+TypeTags.toString(t.tag)+")"+t.tag+"  lastBaseTag="+lastBaseTag);
		
		Type returnType;
		//lastBaseTag=BOOLEAN，也就是8个基本类型不用erasure
        if (t.tag <= lastBaseTag)
            returnType = t; 
        else
            returnType =  erasure.visit(t);
            
		DEBUG.P("t="+t+"  erasureType="+returnType);
		DEBUG.P(1,this,"erasure(Type t)");
		return returnType;
    }
    // where
        private UnaryVisitor<Type> erasure = new UnaryVisitor<Type>() {
            public Type visitType(Type t, Void ignored) {
                if (t.tag <= lastBaseTag)
                    return t; /*fast special case*/
                else
                    return t.map(erasureFun);
            }

            @Override
            public Type visitWildcardType(WildcardType t, Void ignored) {
                //return erasure(upperBound(t));
                
                try {//我加上的
				DEBUG.P(this,"erasure==>visitWildcardType(2)");
                
                return erasure(upperBound(t));
                
                }finally{//我加上的
				DEBUG.P(0,this,"erasure==>visitWildcardType(2)");
				}
            }

            @Override
            public Type visitClassType(ClassType t, Void ignored) {
                //return t.tsym.erasure(Types.this);
                try {//我加上的
				DEBUG.P(this,"erasure==>visitClassType(2)");
                
                return t.tsym.erasure(Types.this);
                
                }finally{//我加上的
				DEBUG.P(0,this,"erasure==>visitClassType(2)");
				}
            }
            /*
            测试源码:
            class ClassA {}
			public class Test<T extends ClassA,E extends T>{}
            
            输入结果:
            com.sun.tools.javac.code.Types===>erasure(Type t)
			-------------------------------------------------------------------------
			t=T{ bound=my.test.ClassA }  t.tag=(TYPEVAR)14  lastBaseTag=8
			com.sun.tools.javac.code.Types$16===>erasure==>visitTypeVar(2)
			-------------------------------------------------------------------------
			com.sun.tools.javac.code.Types===>erasure(Type t)
			-------------------------------------------------------------------------
			t=my.test.ClassA  t.tag=(CLASS)10  lastBaseTag=8
			com.sun.tools.javac.code.Types$16===>visitClassType(2)
			-------------------------------------------------------------------------
			com.sun.tools.javac.code.Types$16===>visitClassType(2)  END
			-------------------------------------------------------------------------
			
			t=my.test.ClassA  erasureType=my.test.ClassA
			com.sun.tools.javac.code.Types===>erasure(Type t)  END
			-------------------------------------------------------------------------
			
			com.sun.tools.javac.code.Types$16===>erasure==>visitTypeVar(2)  END
			-------------------------------------------------------------------------
			
			t=T{ bound=my.test.ClassA }  erasureType=my.test.ClassA
			com.sun.tools.javac.code.Types===>erasure(Type t)  END
			-------------------------------------------------------------------------
			*/
            @Override
            public Type visitTypeVar(TypeVar t, Void ignored) {
            	try {//我加上的
				DEBUG.P(this,"erasure==>visitTypeVar(2)");
                
                return erasure(t.bound);
                
                }finally{//我加上的
				DEBUG.P(0,this,"erasure==>visitTypeVar(2)");
				}
            }

            @Override
            public Type visitErrorType(ErrorType t, Void ignored) {
                return t;
            }
        };
    private Mapping erasureFun = new Mapping ("erasure") {
            public Type apply(Type t) { return erasure(t); }
        };

    public List<Type> erasure(List<Type> ts) {
        return Type.map(ts, erasureFun);
    }
    // </editor-fold>
//