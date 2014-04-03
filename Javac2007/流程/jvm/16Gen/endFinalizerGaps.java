    /** Mark end of all gaps in catch-all ranges for finalizers of environments
     *  lying between, and including to two environments.
     *  @param from    the most deeply nested environment to mark
     *  @param to      the least deeply nested environment to mark
     */
    void endFinalizerGaps(Env<GenContext> from, Env<GenContext> to) {
    DEBUG.P(this,"endFinalizerGaps(2)");
    
	Env<GenContext> last = null;
	while (last != to) {
	    endFinalizerGap(from);
	    last = from;
	    from = from.next;
	}
	
	DEBUG.P(0,this,"endFinalizerGaps(2)");
    }