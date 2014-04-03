package test.attr.error;

public enum enum_no_finalize {
	;
	protected final void finalize(){}
	public final void finalize(){}
	public final void finalize(int i){}
	public final int finalize(){return 0;}
}
