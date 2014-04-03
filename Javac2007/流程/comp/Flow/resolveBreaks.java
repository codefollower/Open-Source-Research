    /** Resolve all breaks of this statement. */
    boolean resolveBreaks(JCTree tree,
			  ListBuffer<PendingExit> oldPendingExits) {
		DEBUG.P(this,"resolveBreaks(2)");
		
		boolean result = false;
		List<PendingExit> exits = pendingExits.toList();
		DEBUG.P("exits.size="+exits.size());
		pendingExits = oldPendingExits;
		for (; exits.nonEmpty(); exits = exits.tail) {
			PendingExit exit = exits.head;
			DEBUG.P("exit.tree.tag="+exit.tree.myTreeTag());
			if (exit.tree.tag == JCTree.BREAK &&
			((JCBreak) exit.tree).target == tree) {

				DEBUG.P("exit.inits  ="+exit.inits);
				DEBUG.P("exit.uninits="+exit.uninits);

				DEBUG.P("inits  前   ="+inits);
				DEBUG.P("uninits前   ="+uninits);

				inits.andSet(exit.inits);
				uninits.andSet(exit.uninits);

				DEBUG.P("inits  后   ="+inits);
				DEBUG.P("uninits后   ="+uninits);
				result = true;
			} else {
				pendingExits.append(exit);
			}
		}
		DEBUG.P("result="+result);
		DEBUG.P(0,this,"resolveBreaks(2)");
		return result;
    }