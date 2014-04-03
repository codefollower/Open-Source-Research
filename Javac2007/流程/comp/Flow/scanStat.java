    /** Analyze a statement. Check that statement is reachable.
     */
    void scanStat(JCTree tree) {
		DEBUG.P(this,"scanStat(1)");
		DEBUG.P("alive="+alive+"  (tree != null)="+(tree != null));

		if (!alive && tree != null) {
			/*如下语句:
				if (dd>0) {
					continue;
					;
					ddd++;
				}
			错误提示:
			bin\mysrc\my\test\Test.java:105: 无法访问的语句
									;
									^
			bin\mysrc\my\test\Test.java:106: 无法访问的语句
									ddd++;
									^
			因为编译器在运到“continue”语句时，调用visitContinue(1)-->
			recordExit(1)-->markDead()，在markDead()中把alive设为false
			*/
			log.error(tree.pos(), "unreachable.stmt");
			if (tree.tag != JCTree.SKIP) alive = true;
		}
		scan(tree);

		DEBUG.P(1,this,"scanStat(1)");
    }

    /** Analyze list of statements.
     */
    void scanStats(List<? extends JCStatement> trees) {
		DEBUG.P(this,"scanStats(1)");
		if (trees == null) DEBUG.P("trees is null");
		else DEBUG.P("trees.size="+trees.size());
		
		if (trees != null)
			for (List<? extends JCStatement> l = trees; l.nonEmpty(); l = l.tail)
				scanStat(l.head);
		DEBUG.P(0,this,"scanStats(1)");	
    }