    public void visitConditional(JCConditional tree) {
		DEBUG.P(this,"visitConditional(1)");
		scanCond(tree.cond);
		Bits initsBeforeElse = initsWhenFalse;
		Bits uninitsBeforeElse = uninitsWhenFalse;
		inits = initsWhenTrue;
		uninits = uninitsWhenTrue;
		if (tree.truepart.type.tag == BOOLEAN &&
			tree.falsepart.type.tag == BOOLEAN) {
			// if b and c are boolean valued, then
			// v is (un)assigned after a?b:c when true iff
			//    v is (un)assigned after b when true and
			//    v is (un)assigned after c when true
			scanCond(tree.truepart);
			Bits initsAfterThenWhenTrue = initsWhenTrue.dup();
			Bits initsAfterThenWhenFalse = initsWhenFalse.dup();
			Bits uninitsAfterThenWhenTrue = uninitsWhenTrue.dup();
			Bits uninitsAfterThenWhenFalse = uninitsWhenFalse.dup();
			inits = initsBeforeElse;
			uninits = uninitsBeforeElse;
			scanCond(tree.falsepart);
			initsWhenTrue.andSet(initsAfterThenWhenTrue);
			initsWhenFalse.andSet(initsAfterThenWhenFalse);
			uninitsWhenTrue.andSet(uninitsAfterThenWhenTrue);
			uninitsWhenFalse.andSet(uninitsAfterThenWhenFalse);
		} else {
			scanExpr(tree.truepart);
			Bits initsAfterThen = inits.dup();
			Bits uninitsAfterThen = uninits.dup();
			inits = initsBeforeElse;
			uninits = uninitsBeforeElse;
			scanExpr(tree.falsepart);
			inits.andSet(initsAfterThen);
			uninits.andSet(uninitsAfterThen);
		}
		myUninitVars(inits,uninits);
		DEBUG.P(0,this,"visitConditional(1)");
    }