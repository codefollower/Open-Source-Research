    /** Visitor method for parameterized types.
     *  Bound checking is left until later, since types are attributed
     *  before supertype structure is completely known
     */
    public void visitTypeApply(JCTypeApply tree) {
		DEBUG.P(this,"visitTypeApply(1)");
		DEBUG.P("tree="+tree);
		
        Type owntype = syms.errType;

        // Attribute functor part of application and make sure it's a class.
        Type clazztype = chk.checkClassType(tree.clazz.pos(), attribType(tree.clazz, env));
        
        

        // Attribute type parameters
        List<Type> actuals = attribTypes(tree.arguments, env);
        
        DEBUG.P("");
        DEBUG.P("actuals="+actuals);
        DEBUG.P("clazztype="+clazztype);
        DEBUG.P("clazztype.tag="+TypeTags.toString(clazztype.tag));
        if (clazztype.tag == CLASS) {
            List<Type> formals = clazztype.tsym.type.getTypeArguments();
            DEBUG.P("formals="+formals);
            
            DEBUG.P("actuals.length()="+actuals.length());
            DEBUG.P("formals.length()="+formals.length());
            if (actuals.length() == formals.length()) {
                List<Type> a = actuals;
                List<Type> f = formals;
                while (a.nonEmpty()) {
                    a.head = a.head.withTypeVar(f.head);//只对WildcardType有用
                    a = a.tail;
                    f = f.tail;
                }
                // Compute the proper generic outer
                Type clazzOuter = clazztype.getEnclosingType();
                DEBUG.P("");
                DEBUG.P("clazzOuter="+clazzOuter);
        		DEBUG.P("clazzOuter.tag="+TypeTags.toString(clazzOuter.tag));
                if (clazzOuter.tag == CLASS) {
                	DEBUG.P("tree.clazz="+tree.clazz);
        			DEBUG.P("tree.clazz.tag="+tree.clazz.myTreeTag());
        			DEBUG.P("env="+env);
                    Type site;
                    if (tree.clazz.tag == JCTree.IDENT) {
                        site = env.enclClass.sym.type;
                    } else if (tree.clazz.tag == JCTree.SELECT) {
                        site = ((JCFieldAccess) tree.clazz).selected.type;
                    } else throw new AssertionError(""+tree);
                    
                    DEBUG.P("site="+site);
        			DEBUG.P("site.tag="+TypeTags.toString(site.tag));
        			DEBUG.P("(clazzOuter.tag == CLASS && site != clazzOuter)="+(clazzOuter.tag == CLASS && site != clazzOuter));
                    if (clazzOuter.tag == CLASS && site != clazzOuter) {
                        if (site.tag == CLASS)
                            site = types.asOuterSuper(site, clazzOuter.tsym);
                        if (site == null)
                            site = types.erasure(clazzOuter);
                        clazzOuter = site;
                    }
                }
                owntype = new ClassType(clazzOuter, actuals, clazztype.tsym);
            } else {
                if (formals.length() != 0) {
                	/*例子:
                	class ExtendsTest<T,S,B>  {}
                	public class MyTestInnerClass
					<Z extends ExtendsTest<?,? super ExtendsTest>> 
					
					错误提示(中文):
					bin\mysrc\my\test\Test.java:8: 类型变量数目错误；需要 3
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        错误提示(英文):
			        bin\mysrc\my\test\Test.java:8: wrong number of type arguments; required 3
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        注:中文错误提示翻译不准确,“type arguments”不能翻译成“类型变量”，
			        “类型变量”是特指泛型类定义中的“类型变量”，如Test<T>，“T”就是
			        一个“类型变量”，而“type arguments”是指参数化后的泛型类的参数，
			        如Test<String>，String就是一个“type argument”，所以准确一点的
			        翻译应该是“类型参数数目错误”。
			        */                                     
					
                    log.error(tree.pos(), "wrong.number.type.args",
                              Integer.toString(formals.length()));
                } else {
                	/*例子:
                	class ExtendsTest{}
                	public class MyTestInnerClass
					<Z extends ExtendsTest<?,? super ExtendsTest>> 
					
					错误提示(中文):
					bin\mysrc\my\test\Test.java:8: 类型 my.test.ExtendsTest 不带有参数
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        错误提示(英文):
			        bin\mysrc\my\test\Test.java:8: type my.test.ExtendsTest does not take parameters
			        MyTestInnerClass<Z extends ExtendsTest<?,? super ExtendsTest>>
			                                              ^
			        */                                  
                    log.error(tree.pos(), "type.doesnt.take.params", clazztype.tsym);
                }
                owntype = syms.errType;
            }
        }
        result = check(tree, owntype, TYP, pkind, pt);
        
        DEBUG.P("tree.type="+tree.type);
        DEBUG.P("tree.type.tsym.type="+tree.type.tsym.type);
		DEBUG.P(0,this,"visitTypeApply(JCTypeApply tree)");
    }