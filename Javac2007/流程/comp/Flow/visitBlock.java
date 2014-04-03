	public void visitBlock(JCBlock tree) {
		DEBUG.P(this,"visitBlock(JCBlock tree)");

		//在scan完JCBlock后,nextadr还是还原为原来的nextadr
		//这一点值得注意，因为JCBlock可以看成是一个整体，
		//如果JCBlock中涉及的变量都是正常的，对JCBlock的scan只是过渡性质
		int nextadrPrev = nextadr;
		scanStats(tree.stats);
		
		DEBUG.P("nextadr当前="+nextadr+" nextadr还原后="+nextadrPrev);
		nextadr = nextadrPrev;
		
		DEBUG.P(0,this,"visitBlock(JCBlock tree)");
    }