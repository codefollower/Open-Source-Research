    /**
     *  Expression = Expression1 [ExpressionRest]
     *  ExpressionRest = [AssignmentOperator Expression1]
     *  AssignmentOperator = "=" | "+=" | "-=" | "*=" | "/=" |
     *                       "&=" | "|=" | "^=" |
     *                       "%=" | "<<=" | ">>=" | ">>>="
     *  Type = Type1
     *  TypeNoParams = TypeNoParams1
     *  StatementExpression = Expression
     *  ConstantExpression = Expression
     */
    JCExpression term() {
    	try {//我加上的
		DEBUG.P(this,"term()");
		
        JCExpression t = term1();   
        /*
        除了"="之外的所有赋值运算符在Token.java中的定义顺序如下:
        PLUSEQ("+="),
	    SUBEQ("-="),
	    STAREQ("*="),
	    SLASHEQ("/="),
	    AMPEQ("&="),
	    BAREQ("|="),
	    CARETEQ("^="),
	    PERCENTEQ("%="),
	    LTLTEQ("<<="),
	    GTGTEQ(">>="),
	    GTGTGTEQ(">>>="),
	    
	    语句PLUSEQ.compareTo(S.token()) <= 0 && S.token().compareTo(GTGTGTEQ) <= 0
	    表示S.token()是上面所列Token之一。
        
        PLUSEQ.compareTo(S.token()) <= 0表示PLUSEQ.ordinal<=S.token().ordinal
        compareTo()方法在java.lang.Enum<E>定义,形如:
        public final int compareTo(E o) {
		Enum other = (Enum)o;
		Enum self = this;
		............
		return self.ordinal - other.ordinal;
	    }
        */
        DEBUG.P("mode="+myMode(mode));
		DEBUG.P("S.token()="+S.token());
        //如果if条件为true说明是一个赋值表达式语句
        if ((mode & EXPR) != 0 &&
            S.token() == EQ || PLUSEQ.compareTo(S.token()) <= 0 && S.token().compareTo(GTGTGTEQ) <= 0)
            return termRest(t);
        else
            return t;
            
        }finally{//我加上的
		DEBUG.P(0,this,"term()");
		}    
    }

    JCExpression termRest(JCExpression t) {
    	try {//我加上的
		DEBUG.P(this,"termRest(JCExpression t)");
		DEBUG.P("t="+t);
		DEBUG.P("S.token()="+S.token());
		
        switch (S.token()) {
        case EQ: {
            int pos = S.pos();
            S.nextToken();
            mode = EXPR;
            /*注意这里是term()，而不是term1()，初看语法:
            Expression = Expression1 [ExpressionRest]
			ExpressionRest = [AssignmentOperator Expression1]
			感觉应是term1()才对，因为java语言允许像a=b=c=d这样的语法,
			所以把ExpressionRest = [AssignmentOperator Expression1]
			看成  ExpressionRest = [AssignmentOperator Expression]
			或者直接用下面一条语法:
			Expression = Expression1 {AssignmentOperator Expression1}
			替换
			Expression = Expression1 [ExpressionRest]
			ExpressionRest = [AssignmentOperator Expression1]
			这两种方式都比原来的好理解
			
			另外在
			Java Language Specification, Third Edition
			18.1. The Grammar of the Java Programming Language
			中的定义如下:
			   Expression:
      		   Expression1 [AssignmentOperator Expression1]]
      		   
      		“]]”有点莫明其妙，不知道是不是多加了个“]”
			*/
            JCExpression t1 = term();
            return toP(F.at(pos).Assign(t, t1));
        }
        case PLUSEQ:
        case SUBEQ:
        case STAREQ:
        case SLASHEQ:
        case PERCENTEQ:
        case AMPEQ:
        case BAREQ:
        case CARETEQ:
        case LTLTEQ:
        case GTGTEQ:
        case GTGTGTEQ:
            int pos = S.pos();
            Token token = S.token();
            S.nextToken();
            mode = EXPR;
            JCExpression t1 = term(); //同上
            return F.at(pos).Assignop(optag(token), t, t1);
        default:
            return t;
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"termRest(JCExpression t)");
		}  
    }