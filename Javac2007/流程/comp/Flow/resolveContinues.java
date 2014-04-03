    /** Resolve all continues of this statement. */
    boolean resolveContinues(JCTree tree) {
		DEBUG.P(this,"resolveContinues(1)");
		
		boolean result = false;
		List<PendingExit> exits = pendingExits.toList();
		pendingExits = new ListBuffer<PendingExit>();
		DEBUG.P("exits.size="+exits.size());
		for (; exits.nonEmpty(); exits = exits.tail) {
			PendingExit exit = exits.head;
			DEBUG.P("exit.tree.tag="+exit.tree.myTreeTag());
			if (exit.tree.tag == JCTree.CONTINUE &&
			((JCContinue) exit.tree).target == tree) {

				DEBUG.P("exit.inits  ="+exit.inits);
				DEBUG.P("exit.uninits="+exit.uninits);

				DEBUG.P("inits  前   ="+inits);
				DEBUG.P("uninits前   ="+uninits);
				
				//在continue语句之前所有变量的赋值情况与continue语句之后
				//所有变量的赋值情况进行位与运算(and)
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
		DEBUG.P(0,this,"resolveContinues(1)");
		return result;
    }