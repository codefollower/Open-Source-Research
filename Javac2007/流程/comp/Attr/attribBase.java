    /** Attribute type reference in an `extends' or `implements' clause.
     *
     *  @param tree              The tree making up the type reference.
     *  @param env               The environment current at the reference.
     *  @param classExpected     true if only a class is expected here.
     *  @param interfaceExpected true if only an interface is expected here.
     */
    //b10
    Type attribBase(JCTree tree,
                    Env<AttrContext> env,
                    boolean classExpected,
                    boolean interfaceExpected,
                    boolean checkExtensible) {
        try {//我加上的
        DEBUG.P(this,"attribBase(5)");
        DEBUG.P("tree="+tree);
        DEBUG.P("tree.tag="+tree.myTreeTag());
        DEBUG.P("env="+env);
        DEBUG.P("classExpected="+classExpected);
        DEBUG.P("interfaceExpected="+interfaceExpected);
        DEBUG.P("checkExtensible="+checkExtensible);  
		
        Type t = attribType(tree, env);
        
        DEBUG.P("t.tag="+TypeTags.toString(t.tag));
        
        return checkBase(t, tree, env, classExpected, interfaceExpected, checkExtensible);
        
        }finally{//我加上的
        DEBUG.P(0,this,"attribBase(5)");
        }
    }
    //b10
    Type checkBase(Type t,
                   JCTree tree,
                   Env<AttrContext> env,
                   boolean classExpected,
                   boolean interfaceExpected,
                   boolean checkExtensible) {
        try {//我加上的
        DEBUG.P(this,"checkBase(6)");
        DEBUG.P("t.tag="+TypeTags.toString(t.tag));
        DEBUG.P("tree="+tree);
        DEBUG.P("env="+env);
        DEBUG.P("classExpected="+classExpected);
        DEBUG.P("interfaceExpected="+interfaceExpected);
        DEBUG.P("checkExtensible="+checkExtensible);  
                 
        if (t.tag == TYPEVAR && !classExpected && !interfaceExpected) {
            DEBUG.P("t.getUpperBound()="+t.getUpperBound());
            // check that type variable is already visible
            if (t.getUpperBound() == null) {
                log.error(tree.pos(), "illegal.forward.ref");
                return syms.errType;
            }
        } else {
            t = chk.checkClassType(tree.pos(), t, checkExtensible|!allowGenerics);
        }
        if (interfaceExpected && (t.tsym.flags() & INTERFACE) == 0) {
            /*错误例子:
            bin\mysrc\my\test\Test.java:8: 此处需要接口
                    public class Test<S,T extends ExtendsTest,E extends ExtendsTest & MyInterfaceA>
                    extends my.ExtendsTest.MyInnerClassStatic implements ExtendsTest {
                                                                         ^
            */
            log.error(tree.pos(), "intf.expected.here");
            // return errType is necessary since otherwise there might
            // be undetected cycles which cause attribution to loop
            return syms.errType;
        } else if (checkExtensible &&
                   classExpected &&
                   (t.tsym.flags() & INTERFACE) != 0) {
            /*src/my/test/EnterTest.java:24: 此处不需要接口
            public class EnterTest<T,S> extends EnterTestInterfaceA implements EnterTestInterfaceA,EnterTestInterfaceB {
                                                ^
            */
            log.error(tree.pos(), "no.intf.expected.here");
            return syms.errType;
        }
        if (checkExtensible &&
            ((t.tsym.flags() & FINAL) != 0)) {
            /*
            src/my/test/EnterTest.java:27: 无法从最终 my.test.EnterTestFinalSupertype 进行继承
            public class EnterTest<T,S> extends EnterTestFinalSupertype {    
                                                ^
            */
            log.error(tree.pos(),
                      "cant.inherit.from.final", t.tsym);
        }
        chk.checkNonCyclic(tree.pos(), t);
        return t;
        
        
        }finally{//我加上的
        DEBUG.P(0,this,"checkBase(6)");
        }
    }