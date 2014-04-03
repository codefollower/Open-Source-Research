//where
        /** Return first abstract member of class `c' that is not defined
	 *  in `impl', null if there is none.
	 */
	//impl是一个实现类，c是一个假定被impl实现的类(抽象或非抽象类、接口)
	//只要在c中找到第一个没有被impl实现的抽象方法，就马上返回它，否则返回null
	private MethodSymbol firstUndef(ClassSymbol impl, ClassSymbol c) {
		DEBUG.P(this,"firstUndef(2)");	
	    MethodSymbol undef = null;
	    DEBUG.P("ClassSymbol impl="+impl);
	    DEBUG.P("ClassSymbol c   ="+c);
	    DEBUG.P("c.flags()="+Flags.toString(c.flags_field));
	    // Do not bother to search in classes that are not abstract,
	    // since they cannot have abstract members.

		//c == impl这个条件用于检查非abstract类中, 含有abstract方法的情况
	    if (c == impl || (c.flags() & (ABSTRACT | INTERFACE)) != 0) {
			Scope s = c.members();
			DEBUG.P("Scope s="+s);
			DEBUG.P("");
			DEBUG.P("for........................开始");
			for (Scope.Entry e = s.elems;
			     undef == null && e != null;
			     e = e.sibling) {
			    DEBUG.P("");
				DEBUG.P("e.sym.name="+e.sym.name);
			    DEBUG.P("e.sym.kind="+Kinds.toString(e.sym.kind));
			    DEBUG.P("e.sym.flags()="+Flags.toString(e.sym.flags_field));
	
			    if (e.sym.kind == MTH &&
				(e.sym.flags() & (ABSTRACT|IPROXY)) == ABSTRACT) {
				MethodSymbol absmeth = (MethodSymbol)e.sym;
				DEBUG.P("absmeth="+absmeth);
				
				MethodSymbol implmeth = absmeth.implementation(impl, types, true);
				
				DEBUG.P("implmeth="+implmeth);
				/*
				implmeth == absmeth这个条件用于检查非abstract类中,
				含有abstract方法的情况
				例子:
				------------------------------------------------------
				public class Test {
					public abstract void abstractMethod();
				}
				错误提示:
				bin\mysrc\my\test\Test.java:1: Test 不是抽象的，并且未覆盖 Test 中的抽象方法 abstractMethod()
				public class Test {
				       ^
				1 错误
				------------------------------------------------------

				或者是在实现类impl中没有定义absmeth这个方法，
				absmeth只在超类中定义，如:
				------------------------------------------------------
				abstract class ExtendsTestt{
					public abstract void extendsTestAbstractMethod();
				}
				public class Test extends ExtendsTest {}
				------------------------------------------------------
				这时:implmeth = absmeth = extendsTestAbstractMethod()
				*/
				if (implmeth == null || implmeth == absmeth)
				    undef = absmeth;
			    }
			}
			DEBUG.P("for........................结束");
			DEBUG.P("");

			DEBUG.P("undef="+undef);
			DEBUG.P("搜索超类........................开始");
			if (undef == null) {
			    Type st = types.supertype(c.type);
			    
			    DEBUG.P("st.tag="+TypeTags.toString(st.tag));
			    
			    if (st.tag == CLASS)
				undef = firstUndef(impl, (ClassSymbol)st.tsym);
			}
			DEBUG.P("搜索超类........................结束");

			DEBUG.P("");
			DEBUG.P("undef="+undef);
			DEBUG.P("搜索接口........................开始");
			//在for之前可以多加个if (undef == null)，这样当
			//undef!=null时就不用找c.type的interfaces。
			//源代码在这没加，可能是作者考虑到用户写程序时
			//抽象方法没有实现的情况比已实现的情况多，也就是undef!=null
			//这种情况很少出现，大多数情况下还是undef == null
			for (List<Type> l = types.interfaces(c.type);
			     undef == null && l.nonEmpty();
			     l = l.tail) {
			    undef = firstUndef(impl, (ClassSymbol)l.head.tsym);
			}
			DEBUG.P("搜索接口........................结束");
	    }
	    
		DEBUG.P("");
	    DEBUG.P("undef="+undef);
	    DEBUG.P(0,this,"firstUndef(2)");	
	    return undef;
	}