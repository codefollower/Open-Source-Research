    /**
     * Attribute type variables (of generic classes or methods).
     * Compound types are attributed later in attribBounds.
     * @param typarams the type variables to enter
     * @param env      the current environment
     */
    //b10新增
    void attribTypeVariables(List<JCTypeParameter> typarams, Env<AttrContext> env) {
    	DEBUG.P(this,"attribTypeVariables(2)");
    	DEBUG.P("typarams="+typarams);
    	DEBUG.P("env="+env);
    	
    	/*注意:
		像class Test<S,P extends V, V extends InterfaceTest,T extends ExtendsTest,E extends ExtendsTest&InterfaceTest>
		这样的定义是合法的，
		虽然V在P之后，但P 先extends V也不会报错，
		因为所有的类型变量(这里是S, P, V, T, E)，在
		com.sun.tools.javac.comp.Enter===>visitTypeParameter(JCTypeParameter tree)
		方法中事先已加入与Test对应的Env里，如下所示为上面两个DEBUG.P()的结果:
		typarams=S,P extends V,V extends InterfaceTest,T extends ExtendsTest,E extends ExtendsTest & InterfaceTest
		env=Env(TK=CLASS EC=)[AttrContext[Scope[(nelems=5 owner=Test)E, T, V, P, S]],outer=Env(TK=COMPILATION_UNIT EC=)[AttrContext[Scope[(nelems=3 owner=test)Test, ExtendsTest, InterfaceTest]]]]
		
		当要生成类型变量P的bound时，因为JCTypeParameter.bounds=V，然后
		在env中查找，发现V在env的Scope存在，所以是可以超前引用V的，
		这主要是因为类型变量的解析和类型变量的bound的解析是分先后两个
		阶段进行的，但是把“P extends V”改成“P extends V2”，就会
		报“找不到符号”这个错误，因为V2不在env中，其他地方也找不到。
		*/
    	
        for (JCTypeParameter tvar : typarams) {
            TypeVar a = (TypeVar)tvar.type;
            DEBUG.P("a.tsym.name="+a.tsym.name);
            DEBUG.P("a.bound="+a.bound);
            DEBUG.P("tvar="+tvar);
    		DEBUG.P("tvar.bounds="+tvar.bounds);
            if (!tvar.bounds.isEmpty()) {
                List<Type> bounds = List.of(attribType(tvar.bounds.head, env));
                for (JCExpression bound : tvar.bounds.tail)
                    bounds = bounds.prepend(attribType(bound, env));
                DEBUG.P("bounds="+bounds);
                DEBUG.P("bounds.reverse()="+bounds.reverse());
                types.setBounds(a, bounds.reverse());
            } else {
                // if no bounds are given, assume a single bound of
                // java.lang.Object.
                types.setBounds(a, List.of(syms.objectType));
            }
            DEBUG.P("a.bound="+a.bound);DEBUG.P("");
        }
        for (JCTypeParameter tvar : typarams)
            chk.checkNonCyclic(tvar.pos(), (TypeVar)tvar.type);
        attribStats(typarams, env);
        
        DEBUG.P(0,this,"attribTypeVariables(2)");
    }