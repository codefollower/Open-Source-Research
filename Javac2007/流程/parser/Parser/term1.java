    /** Expression1   = Expression2 [Expression1Rest]
     *  Type1         = Type2
     *  TypeNoParams1 = TypeNoParams2
     */
    JCExpression term1() {
    	try {//我加上的
		DEBUG.P(this,"term1()");
        JCExpression t = term2();
        DEBUG.P("mode="+myMode(mode));
		DEBUG.P("S.token()="+S.token());
        if ((mode & EXPR) != 0 && S.token() == QUES) {
            mode = EXPR;
            return term1Rest(t);
        } else {
            return t;
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"term1()");
		}
    }

    /** Expression1Rest = ["?" Expression ":" Expression1]
     */
    JCExpression term1Rest(JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"term1Rest(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		
        if (S.token() == QUES) {
            int pos = S.pos();
            DEBUG.P("pos="+pos);
            S.nextToken();
            JCExpression t1 = term();
            accept(COLON);
            
            //对于condition ? trueExpression : falseExpression语句
            //从这里可以看出falseExpression不能含有赋值运算符AssignmentOperator
            //但是trueExpression可以
            JCExpression t2 = term1();
            
            //JCConditional的pos是QUES的pos,而不是t的pos
            return F.at(pos).Conditional(t, t1, t2);
        } else {
            return t;
        }
             
        }finally{//我加上的
		DEBUG.P(0,this,"term1Rest(JCExpression t)");
		}
    }