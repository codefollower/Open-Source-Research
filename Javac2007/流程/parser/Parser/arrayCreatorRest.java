    /** ArrayCreatorRest = "[" ( "]" BracketsOpt ArrayInitializer
     *                         | Expression "]" {"[" Expression "]"} BracketsOpt )
     */
    JCExpression arrayCreatorRest(int newpos, JCExpression elemtype) {
    	try {//我加上的
        DEBUG.P(this,"arrayCreatorRest(2)");
        DEBUG.P("newpos="+newpos);
        DEBUG.P("elemtype="+elemtype);
        
        accept(LBRACKET);
        if (S.token() == RBRACKET) {
            accept(RBRACKET);
            elemtype = bracketsOpt(elemtype);
            if (S.token() == LBRACE) {
                return arrayInitializer(newpos, elemtype);
            } else {
                //例:int a[]=new int[];
                //src/my/test/ParserTest.java:6: 缺少数组维数
                //int a[]=new int[];
                //                 ^

                return syntaxError(S.pos(), "array.dimension.missing");
            }
        } else {
            //当指定了数组维数后就不能用大括号'{}'对数组进行初始化了
            //以下两例都不符合语法:
            //int a[]=new int[2]{1,2};
            //int b[][]=new int[2][3]{{1,2,3},{4,5,6}};
            
            ListBuffer<JCExpression> dims = new ListBuffer<JCExpression>();
            //例:int a[]=new int[8][4];
            dims.append(expression());
            accept(RBRACKET);
            while (S.token() == LBRACKET) {
                int pos = S.pos();
                S.nextToken();
				//int b[][]=new int[2][];      //无错
				//int c[][][]=new int[2][][3]; //有错
				//第一维数组的大小必须指定，二、三......维之后的可以是[][][]
                if (S.token() == RBRACKET) {
                    elemtype = bracketsOptCont(elemtype, pos);
                } else {
                    dims.append(expression());
                    accept(RBRACKET);
                }
            }
            DEBUG.P("dims.toList()="+dims.toList());
            DEBUG.P("elemtype="+elemtype);
            return toP(F.at(newpos).NewArray(elemtype, dims.toList(), null));
        }
        
        }finally{//我加上的
        DEBUG.P(0,this,"arrayCreatorRest(2)");
        }
    }