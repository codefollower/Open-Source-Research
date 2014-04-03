
    /** An abstract class for finalizer generation.
     */
    abstract class GenFinalizer {
		/** Generate code to clean up when unwinding. */
		abstract void gen();

		/** Generate code to clean up at last. */
		abstract void genLast();

		/** Does this finalizer have some nontrivial cleanup to perform? */
		boolean hasFinalizer() { return true; }
    }