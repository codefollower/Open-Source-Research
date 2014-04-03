package my.test;


public class List2<A> {
	private static List2 L=List2.<String>nil();
    /** The first element of the list, supposed to be immutable.
     */
    public A head;

    /** The remainder of the list except for its first element, supposed
     *  to be immutable.
     */
    //@Deprecated
    public List2<A> tail;

    /** Construct a list given its head and tail.
     */
    List2(A head, List2<A> tail) {
	this.tail = tail;
	this.head = head;
    }

    /** Construct an empty list.
     */
    @SuppressWarnings("unchecked")
    public static <A> List2<A> nil() {
	return EMPTY_LIST;
    }
    private static List2 EMPTY_LIST = new List2<Object>(null,null) {
	public List2<Object> setTail(List2<Object> tail) {
	    throw new UnsupportedOperationException();
	}
	public boolean isEmpty() {
	    return true;
	}
    };
}