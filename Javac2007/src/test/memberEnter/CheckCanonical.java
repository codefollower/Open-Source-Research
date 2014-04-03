/*
 * @test
 * @bug 4462745
 * @summary compiler permits to import class given by its non-canonical name
 * @author gafter
 *
 * @compile/fail ImportCanonical1.java ImportCanonical2.java
 */


package test.memberEnter;


import test.memberEnter.A2.I;

public class CheckCanonical { I x; }
class A1 { static class I {} }
//class A2 extends A1 {}

class A2 extends A1 implements IA,IB {}
interface IA{
	class I {}
}

interface IB{
	class I {}
}