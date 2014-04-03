package test.attr;

public class SubstTest {
	SubstTest() {
		<ClassA,ClassB>this( new ClassA() { void methodA(){} } );
	}

	//<TA extends ClassA & InterfaceA, TB extends TA>SubstTest(ClassA ca) {}
	//<TA extends ClassB<?>, TB extends ClassB<? extends ClassA>>SubstTest(ClassA ca) {}
	//<TA extends ClassB<? extends ClassB<TA>>, TB extends ClassB<?> >SubstTest(ClassA ca) {}
	<TA extends ClassB<? extends ClassB<? extends ClassB<TA>>>, TB extends ClassB<?> >SubstTest(ClassA ca) {}

	class ClassA implements InterfaceA {}
	interface InterfaceA {}
	//class ClassB extends ClassA implements InterfaceA {}

	//这个也是正确的，虽然 TB extends TA，并且TA extends ClassA & InterfaceA，
	//但是不要求与TB对应的实参extends ClassA & InterfaceA，
	//只要TB对应的实参extends自TA对应的实参就可以了，
	//像上面的例子:TA=ClassA，因为ClassB extends ClassA，所以TB=ClassB是合法的
	//通过Types.subst([TA],[TA,TB],[ClassA,ClassB]).visitTypeVar(TA)得到[ClassA]
	//然后再Types.isSubtypeUnchecked(ClassB,ClassA)得到true
	class ClassB<T> extends ClassA{}
}
