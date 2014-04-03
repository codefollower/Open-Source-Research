    /** Record that exception is potentially thrown and check that it
     *	is caught.
     */
    void markThrown(JCTree tree, Type exc) {
		DEBUG.P(this,"markThrown(2)");
		DEBUG.P("exc="+exc);
		DEBUG.P("exc.isUnchecked="+chk.isUnchecked(tree.pos(), exc));
		//DEBUG.P("exc.tag="+TypeTags.toString(exc.tag));
		DEBUG.P("caught="+caught);
		DEBUG.P("thrown="+thrown);
		
		//当调用的某一个方法抛出的异常不是
		//java.lang.RuntimeException、java.lang.Error及其子类时，
		//且调用者又没有捕获异常时，
		//将异常加入pendingExits(另请参见Check中的注释)
		if (!chk.isUnchecked(tree.pos(), exc)) {
			DEBUG.P("exc.isHandled="+chk.isHandled(exc, caught));
			if (!chk.isHandled(exc, caught))
				pendingExits.append(new PendingExit(tree, exc));
			thrown = chk.incl(exc, thrown);
		}
		DEBUG.P("thrown="+thrown);
		DEBUG.P(0,this,"markThrown(2)");
    }