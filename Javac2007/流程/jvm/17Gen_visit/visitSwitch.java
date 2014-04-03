    public void visitSwitch(JCSwitch tree) {
    DEBUG.P(this,"visitSwitch(1)");
	int limit = code.nextreg;
	assert tree.selector.type.tag != CLASS;
	int startpcCrt = genCrt ? code.curPc() : 0;
	Item sel = genExpr(tree.selector, syms.intType);
	List<JCCase> cases = tree.cases;
	if (cases.isEmpty()) {
	    // We are seeing:  switch <sel> {}
	    sel.load().drop();
	    if (genCrt)
		code.crt.put(TreeInfo.skipParens(tree.selector),
			     CRT_FLOW_CONTROLLER, startpcCrt, code.curPc());
	} else {
	    // We are seeing a nonempty switch.
	    sel.load();
	    if (genCrt)
		code.crt.put(TreeInfo.skipParens(tree.selector),
			     CRT_FLOW_CONTROLLER, startpcCrt, code.curPc());
	    Env<GenContext> switchEnv = env.dup(tree, new GenContext());
	    switchEnv.info.isSwitch = true;

	    // Compute number of labels and minimum and maximum label values.
	    // For each case, store its label in an array.
	    int lo = Integer.MAX_VALUE;  // minimum label.
	    int hi = Integer.MIN_VALUE;  // maximum label.
	    int nlabels = 0;               // number of labels.

	    int[] labels = new int[cases.length()];  // the label array.
	    int defaultIndex = -1;     // the index of the default clause.

	    List<JCCase> l = cases;
	    for (int i = 0; i < labels.length; i++) {
		if (l.head.pat != null) {
		    int val = ((Number)l.head.pat.type.constValue()).intValue();
		    labels[i] = val;
		    if (val < lo) lo = val;
		    if (hi < val) hi = val;
		    nlabels++;
		} else {
		    assert defaultIndex == -1;
		    defaultIndex = i;
		}
		l = l.tail;
	    }

	    // Determine whether to issue a tableswitch or a lookupswitch
	    // instruction.
	    long table_space_cost = 4 + ((long) hi - lo + 1); // words
	    long table_time_cost = 3; // comparisons
	    long lookup_space_cost = 3 + 2 * (long) nlabels;
	    long lookup_time_cost = nlabels;
	    int opcode =
		nlabels > 0 &&
		table_space_cost + 3 * table_time_cost <=
		lookup_space_cost + 3 * lookup_time_cost
		?
		tableswitch : lookupswitch;

	    int startpc = code.curPc();    // the position of the selector operation
	    code.emitop0(opcode);
	    code.align(4);
	    int tableBase = code.curPc();  // the start of the jump table
	    int[] offsets = null;          // a table of offsets for a lookupswitch
	    code.emit4(-1);                // leave space for default offset
	    if (opcode == tableswitch) {
		code.emit4(lo);            // minimum label
		code.emit4(hi);            // maximum label
		for (long i = lo; i <= hi; i++) {  // leave space for jump table
		    code.emit4(-1);
		}
	    } else {
		code.emit4(nlabels);    // number of labels
		for (int i = 0; i < nlabels; i++) {
		    code.emit4(-1); code.emit4(-1); // leave space for lookup table
		}
		offsets = new int[labels.length];
	    }
	    Code.State stateSwitch = code.state.dup();
	    code.markDead();

	    // For each case do:
	    l = cases;
	    for (int i = 0; i < labels.length; i++) {
		JCCase c = l.head;
		l = l.tail;

		int pc = code.entryPoint(stateSwitch);
		// Insert offset directly into code or else into the
		// offsets table.
		if (i != defaultIndex) {
		    if (opcode == tableswitch) {
			code.put4(
			    tableBase + 4 * (labels[i] - lo + 3),
			    pc - startpc);
		    } else {
			offsets[i] = pc - startpc;
		    }
		} else {
		    code.put4(tableBase, pc - startpc);
		}

		// Generate code for the statements in this case.
		genStats(c.stats, switchEnv, CRT_FLOW_TARGET);
	    }

	    // Resolve all breaks.
	    code.resolve(switchEnv.info.exit);

	    // If we have not set the default offset, we do so now.
	    if (code.get4(tableBase) == -1) {
		code.put4(tableBase, code.entryPoint(stateSwitch) - startpc);
	    }

	    if (opcode == tableswitch) {
		// Let any unfilled slots point to the default case.
		int defaultOffset = code.get4(tableBase);
		for (long i = lo; i <= hi; i++) {
		    int t = (int)(tableBase + 4 * (i - lo + 3));
		    if (code.get4(t) == -1)
			code.put4(t, defaultOffset);
		}
	    } else {
		// Sort non-default offsets and copy into lookup table.
		if (defaultIndex >= 0)
		    for (int i = defaultIndex; i < labels.length - 1; i++) {
			labels[i] = labels[i+1];
			offsets[i] = offsets[i+1];
		    }
		if (nlabels > 0)
		    qsort2(labels, offsets, 0, nlabels - 1);
		for (int i = 0; i < nlabels; i++) {
		    int caseidx = tableBase + 8 * (i + 1);
		    code.put4(caseidx, labels[i]);
		    code.put4(caseidx + 4, offsets[i]);
		}
	    }
	}
	code.endScopes(limit);
	DEBUG.P(0,this,"visitSwitch(1)");
    }