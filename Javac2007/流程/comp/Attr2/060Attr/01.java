    /** Main method: attribute class definition associated with given class symbol.
     *  reporting completion failures at the given position.
     *  @param pos The source position at which completion errors are to be
     *             reported.
     *  @param c   The class symbol whose definition will be attributed.
     */
    public void attribClass(DiagnosticPosition pos, ClassSymbol c) {
    	DEBUG.P(5);DEBUG.P(this,"attribClass(2)");
        try {
            annotate.flush();
            attribClass(c);
        } catch (CompletionFailure ex) {
            chk.completionError(pos, ex);
        }
        DEBUG.P(2,this,"attribClass(2)");
    }

    /** Attribute class definition associated with given class symbol.
     *  @param c   The class symbol whose definition will be attributed.
     */
    void attribClass(ClassSymbol c) throws CompletionFailure {
    	try {//我加上的
    	DEBUG.P(this,"attribClass(1)");
    	DEBUG.P("ClassSymbol c="+c);
    	DEBUG.P("c.type="+c.type);
    	DEBUG.P("c.type.tag="+TypeTags.toString(c.type.tag));
    	DEBUG.P("c.type.supertype="+((ClassType)c.type).supertype_field);
    	DEBUG.P("c.type.supertype.tag="+TypeTags.toString((((ClassType)c.type).supertype_field).tag));
    	
    	
        if (c.type.tag == ERROR) return;

        // Check for cycles in the inheritance graph, which can arise from
        // ill-formed class files.
        chk.checkNonCyclic(null, c.type);
        //检查类(或接口)是否自己继承(或实现)自己，是否彼此之间互相继承(或实现)
        //如Test4 extends Test4(自己继承自己)
        //如Test4 extends Test5且Test5 extends Test4(彼此之间互相继承)
        //如:public class Test4 extends Test4
        //报错:cyclic inheritance involving my.test.Test4


        Type st = types.supertype(c.type);
        DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
        DEBUG.P("c.supertype="+st);
        DEBUG.P("c.supertype.tag="+TypeTags.toString(st.tag));
        DEBUG.P("c.owner="+c.owner);
        DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
        DEBUG.P("c.owner.type.tag="+TypeTags.toString(c.owner.type.tag));
        
        
        //c.flags_field不包含Flags.COMPOUND时执行
        if ((c.flags_field & Flags.COMPOUND) == 0) {
        	DEBUG.P("c.flags_field不包含Flags.COMPOUND");
            // First, attribute superclass.
            if (st.tag == CLASS)
                attribClass((ClassSymbol)st.tsym);

            // Next attribute owner, if it is a class.
            if (c.owner.kind == TYP && c.owner.type.tag == CLASS)
                attribClass((ClassSymbol)c.owner);
        }
        
        DEBUG.P("完成对："+c+" 的superclass及owner的attribute");
        DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
        // The previous operations might have attributed the current class
        // if there was a cycle. So we test first whether the class is still
        // UNATTRIBUTED.
        if ((c.flags_field & UNATTRIBUTED) != 0) {
			//这条语句很有用，因为如果对c这个类进行attribClass后，
        	//在c.flags_field中就没有UNATTRIBUTED这个标志了，当其他
        	//类的超类是c时，在调用Check.checkNonCyclic方法检测循环时，
        	//就可以把ACYCLIC标志加进c.flags_field中。
            c.flags_field &= ~UNATTRIBUTED;

            // Get environment current at the point of class definition.
            Env<AttrContext> env = enter.typeEnvs.get(c);
            
            DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            DEBUG.P("env="+env);

            // The info.lint field in the envs stored in enter.typeEnvs is deliberately uninitialized,
            // because the annotations were not available at the time the env was created. Therefore,
            // we look up the environment chain for the first enclosing environment for which the
            // lint value is set. Typically, this is the parent env, but might be further if there
            // are any envs created as a result of TypeParameter nodes.
            Env<AttrContext> lintEnv = env;
            while (lintEnv.info.lint == null)
                lintEnv = lintEnv.next;
                
            DEBUG.P("lintEnv="+lintEnv);
            // Having found the enclosing lint value, we can initialize the lint value for this class
            env.info.lint = lintEnv.info.lint.augment(c.attributes_field, c.flags());
            
            DEBUG.P("env.info.lint="+env.info.lint);

            Lint prevLint = chk.setLint(env.info.lint);
            JavaFileObject prev = log.useSource(c.sourcefile);

            try {
            	
            	DEBUG.P("");
            	DEBUG.P("st.tsym="+st.tsym);
            	if (st.tsym != null) 
            		DEBUG.P("st.tsym.flags_field="+Flags.toString(st.tsym.flags_field));
            	DEBUG.P("c.flags_field="+Flags.toString(c.flags_field));
            
                // java.lang.Enum may not be subclassed by a non-enum
                if (st.tsym == syms.enumSym &&
                    ((c.flags_field & (Flags.ENUM|Flags.COMPOUND)) == 0))
                    /*例子:
                    F:\javac\bin\mysrc\my\test\TestOhter.java:2: 类无法直接继承 java.lang.Enum
					public class TestOhter<TestOhterS,TestOhterT> extends Enum {
					       ^
					1 错误
					*/
                    log.error(env.tree.pos(), "enum.no.subclassing");

                // Enums may not be extended by source-level classes
                if (st.tsym != null &&
                    ((st.tsym.flags_field & Flags.ENUM) != 0) &&
                    ((c.flags_field & Flags.ENUM) == 0) &&
                    !target.compilerBootstrap(c)) {
                    /*例子:
                    源代码:
                    package my.test.myenum;
					public class EnumTest extends MyEnum {}
					enum MyEnum {}
					
					错误提示:
					bin\mysrc\my\test\myenum\EnumTest.java:3: 无法从最终 my.test.myenum.MyEnum 进行继承
					public class EnumTest extends MyEnum {}
					                              ^
					bin\mysrc\my\test\myenum\EnumTest.java:3: 枚举类型不可继承
					public class EnumTest extends MyEnum {}
					       ^
					2 错误
					*/
                    log.error(env.tree.pos(), "enum.types.not.extensible");
                }

				DEBUG.P(2);
                attribClassBody(env, c);

                chk.checkDeprecatedAnnotation(env.tree.pos(), c);
            } finally {
                log.useSource(prev);
                chk.setLint(prevLint);
                
            }
        }
        
        
        }finally{//我加上的
        DEBUG.P("结束对此类的属性分性: "+c);
        DEBUG.P(1,this,"attribClass(1)");
    	}
    }