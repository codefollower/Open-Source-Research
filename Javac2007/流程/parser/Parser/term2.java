    /** Expression2   = Expression3 [Expression2Rest]
     *  Type2         = Type3
     *  TypeNoParams2 = TypeNoParams3
     */
    JCExpression term2() {
    	try {//我加上的
		DEBUG.P(this,"term2()");
        JCExpression t = term3();
        
        DEBUG.P("mode="+myMode(mode));
		DEBUG.P("S.token()="+S.token());
		
		//当前运算符的优先级>=“||”运算符的优先级时，才调用term2Rest
        if ((mode & EXPR) != 0 && prec(S.token()) >= TreeInfo.orPrec) {
            mode = EXPR;
            return term2Rest(t, TreeInfo.orPrec);
        } else {
            return t;
        }
        
        
        }finally{//我加上的
		DEBUG.P(0,this,"term2()");
		}        
    }
    
    //instanceof运算符和比较运算符("<" | ">" | "<=" | ">=")的优先级一样
    
    /*  Expression2Rest = {infixop Expression3}
     *                  | Expression3 instanceof Type
     *  infixop         = "||"
     *                  | "&&"
     *                  | "|"
     *                  | "^"
     *                  | "&"
     *                  | "==" | "!="
     *                  | "<" | ">" | "<=" | ">="
     *                  | "<<" | ">>" | ">>>"
     *                  | "+" | "-"
     *                  | "*" | "/" | "%"
     */
    JCExpression term2Rest(JCExpression t, int minprec) {
    	try {//我加上的
		DEBUG.P(this,"term2Rest(JCExpression t, int minprec)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		//DEBUG.P("odStackSupply.size="+odStackSupply.size());
		//DEBUG.P("opStackSupply.size="+opStackSupply.size());
		
		//odStack指向odStackSupply.elems.head
        //odStackSupply.elems往下移
        List<JCExpression[]> savedOd = odStackSupply.elems;
		//DEBUG.P("odStackSupply.elems="+odStackSupply.elems);
		//DEBUG.P("savedOd.size="+savedOd.size());
		//DEBUG.P("savedOd="+savedOd);
        JCExpression[] odStack = newOdStack();


        List<Token[]> savedOp = opStackSupply.elems;
		//DEBUG.P("opStackSupply.elems="+opStackSupply.elems);
		//DEBUG.P("savedOp.size="+savedOp.size());
		//DEBUG.P("savedOp="+savedOp);
        Token[] opStack = newOpStack();

		/*
		DEBUG.P(1);
		DEBUG.P("odStackSupply.elems="+odStackSupply.elems);
		DEBUG.P("savedOd.size="+savedOd.size());
		DEBUG.P("savedOd="+savedOd);
		DEBUG.P("opStackSupply.elems="+opStackSupply.elems);
		DEBUG.P("savedOp.size="+savedOp.size());
		DEBUG.P("savedOp="+savedOp);
		*/

        // optimization, was odStack = new Tree[...]; opStack = new Tree[...];
        int top = 0;
        odStack[0] = t;
        int startPos = S.pos();
        Token topOp = ERROR;
        while (prec(S.token()) >= minprec) {
        	DEBUG.P("topOp="+topOp+" S.token()="+S.token());
            opStack[top] = topOp;
            top++;
            topOp = S.token();
            int pos = S.pos();
            S.nextToken();
            odStack[top] = topOp == INSTANCEOF ? type() : term3();
            //for(int i=0;i<odStack.length;i++) {
			for(int i=0;i<=top;i++) {
            	if(odStack[i]!=null) DEBUG.P("odStack["+i+"]="+odStack[i]);
            }
            for(int i=0;i<=top;i++) {
            	if(opStack[i]!=null) DEBUG.P("opStack["+i+"]="+opStack[i]);
            }
            //只要前一个运算符的优先级>=紧接的运算符的优先级
            //就马上归并。如:1+2+4*5,会先归并1+2，接着是4*5
            //最后是(1+2)+(4*5)
            while (top > 0 && prec(topOp) >= prec(S.token())) {
            	DEBUG.P("pos="+pos);//这里的pos是topOp的pos
            	DEBUG.P("topOp="+topOp+" S.token()="+S.token());
            	//DEBUG.P("odStack[top-1]="+odStack[top-1]);
            	//DEBUG.P("odStack[top]="+odStack[top]);
                odStack[top-1] = makeOp(pos, topOp, odStack[top-1],
                                        odStack[top]);
                top--;
                topOp = opStack[top];
                DEBUG.P("topOp="+topOp+" S.token()="+S.token());
                
                for(int i=0;i<=top;i++) {
	            	if(odStack[i]!=null) DEBUG.P("odStack["+i+"]="+odStack[i]);
	            }
	            for(int i=0;i<=top;i++) {
	            	if(opStack[i]!=null) DEBUG.P("opStack["+i+"]="+opStack[i]);
	            }
            }
        }
        assert top == 0;
        /*
        odStack[0]所代表的Binary表达式的运算符(opcode)的优先级
        总是在所有字面表达式中最小最右边的那个
        
        如a || 1<=2 && 3<=4，则odStack[0].opcode=||
        以下是输出结果:
        ----------------------------
        t=a || 1 <= 2 && 3 <= 4
		t.tag=CONDITIONAL_OR
		t.lhs=a
		t.rhs=1 <= 2 && 3 <= 4
        
        
        再如1+2>0 || a || 1<=2 && 3<=4,则odStack[0].opcode还是等于||
        以下是输出结果:
        ----------------------------
        t=1 + 2 > 0 || a || 1 <= 2 && 3 <= 4
		t.tag=CONDITIONAL_OR
		t.lhs=1 + 2 > 0 || a
		t.rhs=1 <= 2 && 3 <= 4
		*/
        t = odStack[0];
		DEBUG.P(1);
        DEBUG.P("t="+t);
        DEBUG.P("t.tag="+t.getKind());
        if(t instanceof JCBinary) {
			DEBUG.P("t.lhs="+((JCBinary)t).lhs);
			DEBUG.P("t.rhs="+((JCBinary)t).rhs);
        }
        
        if (t.tag == JCTree.PLUS) {
            StringBuffer buf = foldStrings(t);
            DEBUG.P("buf="+buf);
            if (buf != null) {
                t = toP(F.at(startPos).Literal(TypeTags.CLASS, buf.toString()));
            }
        }
        
        //不用再次分配堆栈空间
        odStackSupply.elems = savedOd; // optimization
        opStackSupply.elems = savedOp; // optimization
        return t;
        
        }finally{//我加上的
		DEBUG.P(0,this,"term2Rest(JCExpression t, int minprec)");
		} 
    }
//where
        /** Construct a binary or type test node.
         */
        private JCExpression makeOp(int pos,
                                    Token topOp,
                                    JCExpression od1,
                                    JCExpression od2)
        {
            if (topOp == INSTANCEOF) {
                return F.at(pos).TypeTest(od1, od2);
            } else {
                return F.at(pos).Binary(optag(topOp), od1, od2);
            }
        }
        /** If tree is a concatenation of string literals, replace it
         *  by a single literal representing the concatenated string.
         */
        protected StringBuffer foldStrings(JCTree tree) {
        	try {//我加上的
        	DEBUG.P(this,"foldStrings(JCTree tree");
        	DEBUG.P("tree="+tree);
       		DEBUG.P("tree.tag="+tree.getKind());
       		
            List<String> buf = List.nil();
            /*
            只有表达中的运算符全是加号(+)，而且用加号连接起来
            的每个字面值全都是字符串时，才把每个字面值字符串合并起来。
            例如 "ab"+"cd"+"ef"+"gh":
            List<String> buf的内部结构按如下过程变化:
            1. buf.prepend("gh") = "gh"
            2. buf.prepend("ef") = "ef"==>"gh"
            3. buf.prepend("cd") = "cd"==>"ef"==>"gh"
            
            然后StringBuffer sbuf = new StringBuffer("ab");
            sbuf.append("cd") = "abcd"
            sbuf.append("ef") = "abcdef"
            sbuf.append("gh") = "abcdefgh"
            
            最后返回:"abcdefgh"。
            
            对于"ab"+"cd"+"ef"+1 或 1+"cd"+"ef"+"gh"
                                 或 "ab"+1*2+"cd"+"ef"+"gh"
            都将返回null

			注意:String str="A"+"B"+'c';也返回null，因为'c'是字符，不是字符串
			而str="A"+"B"+"c";就返回ABc
            */
            
            while (true) {
                if (tree.tag == JCTree.LITERAL) { //最左边的字符串
                    JCLiteral lit = (JCLiteral) tree;
                    if (lit.typetag == TypeTags.CLASS) {
                        StringBuffer sbuf =
                            new StringBuffer((String)lit.value);
                        while (buf.nonEmpty()) {
                            sbuf.append(buf.head);
                            buf = buf.tail;
                        }
                        return sbuf;
                    }
                } else if (tree.tag == JCTree.PLUS) {
                    JCBinary op = (JCBinary)tree;
                    DEBUG.P("op.rhs.tag="+op.rhs.getKind());
                    if (op.rhs.tag == JCTree.LITERAL) {
                        JCLiteral lit = (JCLiteral) op.rhs;
                        if (lit.typetag == TypeTags.CLASS) {
                            buf = buf.prepend((String) lit.value);
                            tree = op.lhs;
                            continue;
                        }
                    }
                }
                return null;
            }
	        
	        }finally{//我加上的
			DEBUG.P(0,this,"foldStrings(JCTree tree");
			}
        }

        /** optimization: To save allocating a new operand/operator stack
         *  for every binary operation, we use supplys.
         */
		//odStackSupply.size()与opStackSupply.size() = 表达式中的括号对数+1
		//如表达式:a=a*(b+a)，那么odStackSupply.size() = opStackSupply.size() = 2
        ListBuffer<JCExpression[]> odStackSupply = new ListBuffer<JCExpression[]>();
        ListBuffer<Token[]> opStackSupply = new ListBuffer<Token[]>();

        private JCExpression[] newOdStack() {
			//DEBUG.P(this,"newOdStack()");
			//DEBUG.P("odStackSupply.elems="+odStackSupply.elems);
			//DEBUG.P("odStackSupply.last="+odStackSupply.last);
			//DEBUG.P("if (odStackSupply.elems == odStackSupply.last)="+(odStackSupply.elems == odStackSupply.last));

            if (odStackSupply.elems == odStackSupply.last)
                odStackSupply.append(new JCExpression[infixPrecedenceLevels + 1]);
            JCExpression[] odStack = odStackSupply.elems.head;
            odStackSupply.elems = odStackSupply.elems.tail;
            return odStack;
        }

        private Token[] newOpStack() {
            if (opStackSupply.elems == opStackSupply.last)
                opStackSupply.append(new Token[infixPrecedenceLevels + 1]);
            Token[] opStack = opStackSupply.elems.head;
            opStackSupply.elems = opStackSupply.elems.tail;
            return opStack;
        }