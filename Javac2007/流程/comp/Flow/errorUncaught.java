    /*-------------------- Exceptions ----------------------*/

    /** Complain that pending exceptions are not caught.
     */
    void errorUncaught() {
		DEBUG.P(this,"errorUncaught()");
		DEBUG.P("pendingExits.size()="+pendingExits.size());
		for (PendingExit exit = pendingExits.next();
			 exit != null;
			 exit = pendingExits.next()) {
			boolean synthetic = classDef != null &&
			classDef.pos == exit.tree.pos;

			DEBUG.P("synthetic="+synthetic);

			log.error(exit.tree.pos(),
				  synthetic
				  ? "unreported.exception.default.constructor"
				  : "unreported.exception.need.to.catch.or.throw",
				  exit.thrown);
		}
		
		//因为在for中调用了pendingExits.next()，
		//所以pendingExits.size()最后总是为0
		DEBUG.P("pendingExits.size()="+pendingExits.size());
		DEBUG.P(0,this,"errorUncaught()");
    }