package com.sun.tools.javac.processing;

/**
 * 
 * @author ZHH
 *
 */
public class PrettyPrinter {
	public int tab=0;
	public StringBuilder buf;
	private int oldTab = 0;


	public PrettyPrinter(StringBuilder buf) {
		this.buf = buf;
	}
	public PrettyPrinter(int tab) {
		this.buf = new StringBuilder();
		this.tab = tab;

		this.oldTab = tab;
	}
	public PrettyPrinter() {
		this.buf = new StringBuilder();
	}

	public void reset() {
		buf = new StringBuilder();
		tab = oldTab;
	}

	public StringBuilder print(Object s) {
		align();
		buf.append(s);
		return buf;
    }
	public StringBuilder printNoAlign(Object s) {
		buf.append(s);
		return buf;
    }

	public StringBuilder print(int t,Object s) {
		T(t);
		print(s);
		return buf;
    }

    public StringBuilder println() {
        buf.append(lineSep);
		return buf;
    }

    public StringBuilder println(Object s) {
        print(s);
        println();
		return buf;
    }

	public StringBuilder println(int t,Object s) {
        T(t);
        println(s);
		return buf;
    }


	//打印tab
	public StringBuilder T(int count) {
		for(int i=0;i<count;i++) buf.append('\t');
		return buf;
    }

	public String toString() {
		//String str = buf.toString();
		//buf = new StringBuilder();
		//return str;

		return buf.toString();
	}

	private void align() {
		for(int i=0; i<tab; i++) buf.append('\t');
	}

    private String lineSep = System.getProperty("line.separator");
}