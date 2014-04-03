    /** Enter interface into into set.
     *  If it existed already, issue a "repeated interface" error.
     */
    void checkNotRepeated(DiagnosticPosition pos, Type it, Set<Type> its) {
		DEBUG.P(this,"checkNotRepeated(3)");
		DEBUG.P("it="+it);
		DEBUG.P("its="+its);
		/*
		bin\mysrc\my\test\Test.java:8: 接口重复
		public class Test<S extends TestBound & MyInterfaceA, T> extends TestOhter<Integ
		er,String> implements MyInterfaceA,MyInterfaceA,MyInterfaceB {
		
										   ^
		1 错误
		*/
		if (its.contains(it))
			log.error(pos, "repeated.interface");
		else {
			its.add(it);
		}
		DEBUG.P(0,this,"checkNotRepeated(3)");
    }