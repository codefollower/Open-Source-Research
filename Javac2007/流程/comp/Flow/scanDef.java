/* ************************************************************************
 * Visitor methods for statements and definitions
 *************************************************************************/

    /** Analyze a definition.
     */
    void scanDef(JCTree tree) {
		DEBUG.P(this,"scanDef(1)");
		DEBUG.P("alive="+alive+" tree="+tree);
		scanStat(tree);
		if (tree != null && tree.tag == JCTree.BLOCK && !alive) {
			//初始化程序必须能够正常完成
			log.error(tree.pos(),
				  "initializer.must.be.able.to.complete.normally");
		}
		DEBUG.P(0,this,"scanDef(1)");
    }