    /** Is code generation currently enabled?
     */
    public boolean isAlive() {
	return alive || pendingJumps != null;
    }