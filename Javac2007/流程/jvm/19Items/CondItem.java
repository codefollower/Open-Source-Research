    /** An item representing a conditional or unconditional jump.
     */
    class CondItem extends Item {

	/** A chain encomassing all jumps that can be taken
	 *  if the condition evaluates to true.
	 */
	Chain trueJumps;

	/** A chain encomassing all jumps that can be taken
	 *  if the condition evaluates to false.
	 */
	Chain falseJumps;

	/** The jump's opcode.
	 */
	int opcode;

	/*
	 *  An abstract syntax tree of this item. It is needed
	 *  for branch entries in 'CharacterRangeTable' attribute.
	 */
	JCTree tree;

	CondItem(int opcode, Chain truejumps, Chain falsejumps) {
	    super(BYTEcode);

		DEBUG.P(this,"CondItem(3)");
		DEBUG.P("opcode="+Code.mnem(opcode));
		DEBUG.P("truejumps ="+truejumps);
		DEBUG.P("falsejumps="+falsejumps);

	    this.opcode = opcode;
	    this.trueJumps = truejumps;
	    this.falseJumps = falsejumps;

		DEBUG.P(0,this,"CondItem(3)");
	}

	Item load() {
		try {//我加上的
		DEBUG.P(this,"load()");

	    Chain trueChain = null;
	    Chain falseChain = jumpFalse();

		DEBUG.P("isFalse()="+isFalse());
	    if (!isFalse()) {
		code.resolve(trueJumps);
		code.emitop0(iconst_1);
		trueChain = code.branch(goto_);
	    }

		DEBUG.P("falseChain="+falseChain);
	    if (falseChain != null) {
		code.resolve(falseChain);
		code.emitop0(iconst_0);
	    }
	    code.resolve(trueChain);
	    return stackItem[typecode];

		}finally{//我加上的
		DEBUG.P(0,this,"load()");
		}
	}

	void duplicate() {
	    load().duplicate();
	}

	void drop() {
	    load().drop();
	}

	void stash(int toscode) {
	    assert false;
	}

	CondItem mkCond() {
	    return this;
	}

	Chain jumpTrue() {
		try {//我加上的
		DEBUG.P(this,"jumpTrue()");
		DEBUG.P("tree="+tree);

	    if (tree == null) return code.mergeChains(trueJumps, code.branch(opcode));
	    // we should proceed further in -Xjcov mode only
	    int startpc = code.curPc();
	    Chain c = code.mergeChains(trueJumps, code.branch(opcode));
	    code.crt.put(tree, CRTable.CRT_BRANCH_TRUE, startpc, code.curPc());
	    return c;

		}finally{//我加上的
		DEBUG.P(0,this,"jumpTrue()");
		}
	}

	Chain jumpFalse() {
		try {//我加上的
		DEBUG.P(this,"jumpFalse()");
		DEBUG.P("tree="+tree);

	    if (tree == null) return code.mergeChains(falseJumps, code.branch(code.negate(opcode)));
	    // we should proceed further in -Xjcov mode only
	    int startpc = code.curPc();
	    Chain c = code.mergeChains(falseJumps, code.branch(code.negate(opcode)));
	    code.crt.put(tree, CRTable.CRT_BRANCH_FALSE, startpc, code.curPc());
	    return c;

		}finally{//我加上的
		DEBUG.P(0,this,"jumpFalse()");
		}
	}

	CondItem negate() {
		try {//我加上的
		DEBUG.P(this,"negate()");

	    CondItem c = new CondItem(code.negate(opcode), falseJumps, trueJumps);
	    c.tree = tree;
	    return c;

		}finally{//我加上的
		DEBUG.P(0,this,"negate()");
		}
	}

	int width() {
	    // a CondItem doesn't have a size on the stack per se.
	    throw new AssertionError();
	}

	boolean isTrue() {
	    return falseJumps == null && opcode == goto_;
	}

	boolean isFalse() {
	    return trueJumps == null && opcode == dontgoto;
	}

	public String toString() {
	    //return "cond(" + Code.mnem(opcode) + ")";

		//我加上的
		return "CondItem(" + Code.mnem(opcode) + "[trueJumps="+trueJumps+", falseJumps="+falseJumps+", tree="+tree+"])";
	}
    }