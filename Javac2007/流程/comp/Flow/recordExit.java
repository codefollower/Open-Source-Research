    /** Record an outward transfer of control. */
    void recordExit(JCTree tree) {
		DEBUG.P(this,"recordExit(1)");
		
		pendingExits.append(new PendingExit(tree, inits, uninits));
		markDead();
		
		DEBUG.P(0,this,"recordExit(1)");
    }