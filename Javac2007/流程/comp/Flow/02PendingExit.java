    /*-------------------- Environments ----------------------*/

    /** A pending exit.	 These are the statements return, break, and
     *	continue.  In addition, exception-throwing expressions or
     *	statements are put here when not known to be caught.  This
     *	will typically result in an error unless it is within a
     *	try-finally whose finally block cannot complete normally.
     */
    static class PendingExit {
		JCTree tree;
		Bits inits;
		Bits uninits;
		Type thrown;
		PendingExit(JCTree tree, Bits inits, Bits uninits) {
			this.tree = tree;
			this.inits = inits.dup();
			this.uninits = uninits.dup();
		}
		PendingExit(JCTree tree, Type thrown) {
			this.tree = tree;
			this.thrown = thrown;
		}
    }

    /** The currently pending exits that go from current inner blocks
     *	to an enclosing block, in source order.
     */
    ListBuffer<PendingExit> pendingExits;