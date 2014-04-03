package com.sun.tools;

enum CompilerBootstrapEnumTest implements java.io.Serializable,Comparable<String>,IA<?>{
	A,
	B,
	C;
}

interface IA<T>{}