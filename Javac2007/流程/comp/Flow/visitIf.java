    /*为什么像下面的语句只报一次错误?
			int iii;
			if(iii>5) iii++;
			else iii--;
			
			bin\mysrc\my\test\Test.java:91: 可能尚未初始化变量 iii
					if(iii>5) iii++;
					   ^
		因为在scanCond(tree.cond)中scan到iii>5时，会转到checkInit(2),
		此时发现inits中没有iii，就报错:可能尚未初始化变量 iii,报完错
		误提示信息后，再把iii加入inits中，
		接着再把inits赋给initsWhenTrue与initsWhenFalse
		
		而	int i=10;
		　　int iii;
			if(i>5) iii++;
			else iii--;
		报了两次错:	
		bin\mysrc\my\test\Test.java:91: 可能尚未初始化变量 iii
					if(i>5) iii++;
							^
		bin\mysrc\my\test\Test.java:92: 可能尚未初始化变量 iii
					else iii--;
						 ^
		是因为:if语句的两个部分(then与else)分别对
		应的是initsWhenTrue与initsWhenFalse，
		而上面的if(iii>5)只单独对应inits，当调用完checkInit(2)后，
		再用inits的当前值赋给initsWhenTrue与initsWhenFalse，而此时
		这两个值都已包含了变量iii。
		
		但是对于if(i>5)来说，在调用完scanCond(tree.cond)后，inits还
		没有包含变量iii，然后就直接赋给initsWhenTrue与initsWhenFalse，
		当调用scanStat(tree.thenpart)与scanStat(tree.elsepart)之前，
		又把initsWhenTrue与initsWhenFalse分别赋给inits，所以在执行
		到checkInit(2)时，inits都没有包含变量iii，从而报两次错误，
		所以这很合理。
	*/
    public void visitIf(JCIf tree) {
		DEBUG.P(this,"visitIf(1)");
		scanCond(tree.cond);
		Bits initsBeforeElse = initsWhenFalse;
		Bits uninitsBeforeElse = uninitsWhenFalse;
		inits = initsWhenTrue;
		uninits = uninitsWhenTrue;
		DEBUG.P("scanStat(tree.thenpart)开始");
		scanStat(tree.thenpart);
		DEBUG.P("scanStat(tree.thenpart)结束");
		if (tree.elsepart != null) {
			DEBUG.P(2);
			DEBUG.P("scanStat(tree.elsepart)开始");
			boolean aliveAfterThen = alive;
			alive = true;
			Bits initsAfterThen = inits.dup();
			Bits uninitsAfterThen = uninits.dup();
			inits = initsBeforeElse;
			uninits = uninitsBeforeElse;
			
			scanStat(tree.elsepart);
			inits.andSet(initsAfterThen);
			uninits.andSet(uninitsAfterThen);
			alive = alive | aliveAfterThen;
			DEBUG.P("scanStat(tree.elsepart)结束");
		} else {
			inits.andSet(initsBeforeElse);
			uninits.andSet(uninitsBeforeElse);
			alive = true;
		}
		DEBUG.P("alive="+alive);
		DEBUG.P("inits  ="+inits);
		DEBUG.P("uninits="+uninits);
		myUninitVars(inits,uninits);
		DEBUG.P(0,this,"visitIf(1)");
    }