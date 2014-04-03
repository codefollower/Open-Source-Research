    /**
     * Qualident = Ident { DOT Ident }
     */
    public JCExpression qualident() {
    	DEBUG.P(this,"qualident()");
    	//注意下面是先F.at(S.pos())，然后再调用ident()
        JCExpression t = toP(F.at(S.pos()).Ident(ident()));
		DEBUGPos(t);
        while (S.token() == DOT) {
            int pos = S.pos();
            S.nextToken();
            
            /*
            //用当前pos覆盖TreeMaker里的pos,然后生成一棵JCFieldAccess树
            //所生成的JCFieldAccess实例将TreeMaker里的pos当成自己的pos
            //JCFieldAccess按Ident的逆序层层嵌套
            
            //如当Qualident =java.lang.Byte时表示为:
            JCFieldAccess {
            	Name name = "Byte";
            	JCExpression selected = {
            		JCFieldAccess {
            			Name name="lang";
            			JCExpression selected = {
				            JCIdent {
				            	Name name = "java";
				            }
				        }
				    }
				}
			}
			*/
            //DEBUG.P("pos="+pos);//这里的pos是"."号的开始位置
            t = toP(F.at(pos).Select(t, ident()));
			//DEBUGPos(t);//但是这里输出的开始位置总是第一个ident的开始位置
        }
        
        DEBUG.P("qualident="+t);
		DEBUGPos(t);
        DEBUG.P(0,this,"qualident()");
        return t;
    }