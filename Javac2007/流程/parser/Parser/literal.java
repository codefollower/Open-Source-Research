    /**
     * Literal =
     *     INTLITERAL
     *   | LONGLITERAL
     *   | FLOATLITERAL
     *   | DOUBLELITERAL
     *   | CHARLITERAL
     *   | STRINGLITERAL
     *   | TRUE
     *   | FALSE
     *   | NULL
     */

     //为什么没有byte,short呢？因为在Scanner中分析数字或字符不按类型声明来分析的，
     //只是单单从字面值分析，所以没有byte,short这样的字面值(LITERAL)
    JCExpression literal(Name prefix) {
    	DEBUG.P(this,"literal(Name prefix)");
    	DEBUG.P("prefix="+prefix);
    	//prefix是指字面文字(Literal)的前缀,如是否带负号(-)
    	
        int pos = S.pos();
        JCExpression t = errorTree;

        switch (S.token()) {
        case INTLITERAL:
            try {
            	//类全限定名称:com.sun.tools.javac.code.TypeTags
            	//类全限定名称:com.sun.tools.javac.util.Convert
                t = F.at(pos).Literal(
                    TypeTags.INT,
                    Convert.string2int(strval(prefix), S.radix()));
            } catch (NumberFormatException ex) {
            	/*错误例子:
            	bin\mysrc\my\test\Test3.java:29: 过大的整数： 099
		        public final int c=099;
		                           ^
		        */                   
                log.error(S.pos(), "int.number.too.large", strval(prefix));
            }
            break;
        case LONGLITERAL:
            try {
                t = F.at(pos).Literal(
                    TypeTags.LONG,
                    new Long(Convert.string2long(strval(prefix), S.radix())));
            } catch (NumberFormatException ex) {
                log.error(S.pos(), "int.number.too.large", strval(prefix));
            }
            break;
        case FLOATLITERAL: {
            String proper = (S.radix() == 16 ? ("0x"+ S.stringVal()) : S.stringVal());
            Float n;
            try {
				//经过词法分析后proper代表的浮点数格式肯定是正确的，
				//但是词法分析时并不知道浮点字面值是否过小还是过大
				//如果过小，那么Float.valueOf(proper)总是返回0.0f，
				//这与正常的0.0f无法区分，所以在下面通过!isZero(proper)来判断，
				//如果proper("0x"除外)中的每个字符只要有一个不是0或'.'号，
				//则一定是过小的浮点数
				//另外，对于过大的浮点数，Float.valueOf(proper)总是返回Float.POSITIVE_INFINITY
                n = Float.valueOf(proper);
            } catch (NumberFormatException ex) {
                // error already repoted in scanner
                n = Float.NaN;
            }
            if (n.floatValue() == 0.0f && !isZero(proper)) //例:float f1=1.1E-33333f;
                log.error(S.pos(), "fp.number.too.small");
            else if (n.floatValue() == Float.POSITIVE_INFINITY) //例:float f2=1.1E+33333f;
                log.error(S.pos(), "fp.number.too.large");
            else
                t = F.at(pos).Literal(TypeTags.FLOAT, n);
            break;
        }
        case DOUBLELITERAL: {
            String proper = (S.radix() == 16 ? ("0x"+ S.stringVal()) : S.stringVal());
            Double n;
            try {
                n = Double.valueOf(proper); //同上
            } catch (NumberFormatException ex) {
                // error already reported in scanner
                n = Double.NaN;
            }
            if (n.doubleValue() == 0.0d && !isZero(proper))
                log.error(S.pos(), "fp.number.too.small");
            else if (n.doubleValue() == Double.POSITIVE_INFINITY)
                log.error(S.pos(), "fp.number.too.large");
            else
                t = F.at(pos).Literal(TypeTags.DOUBLE, n);
            break;
        }
        case CHARLITERAL:
            t = F.at(pos).Literal(
                TypeTags.CHAR,
                S.stringVal().charAt(0) + 0); //注意这里：字符转成了整数,Literal方法接收的是Integer对象
            break;
        case STRINGLITERAL:
            t = F.at(pos).Literal(
                TypeTags.CLASS,
                S.stringVal());
            break;
        case TRUE: case FALSE:
            t = F.at(pos).Literal(
                TypeTags.BOOLEAN,
                (S.token() == TRUE ? 1 : 0));
            break;
        case NULL:
            t = F.at(pos).Literal(
                TypeTags.BOT,
                null);
            break;
        default:
            assert false;
        }
        if (t == errorTree)
            t = F.at(pos).Erroneous();
        storeEnd(t, S.endPos());
        S.nextToken();
        
        DEBUG.P("return t="+t);
        DEBUG.P(0,this,"literal(Name prefix)");
        return t;
    }
//where
        boolean isZero(String s) {
            char[] cs = s.toCharArray();
            int base = ((Character.toLowerCase(s.charAt(1)) == 'x') ? 16 : 10);
            int i = ((base==16) ? 2 : 0);
            while (i < cs.length && (cs[i] == '0' || cs[i] == '.')) i++;
            return !(i < cs.length && (Character.digit(cs[i], base) > 0));
        }

        String strval(Name prefix) {
        	//字面文字(Literal)在Scanner中被当成
        	//字符串存放在临时缓存字符数组中
            String s = S.stringVal();
            return (prefix.len == 0) ? s : prefix + s;
        }