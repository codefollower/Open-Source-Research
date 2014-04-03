//isAssignable
    // <editor-fold defaultstate="collapsed" desc="isAssignable">
    public boolean isAssignable(Type t, Type s) {
		try {//我加上的
		DEBUG.P(this,"isAssignable(2)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

        return isAssignable(t, s, Warner.noWarnings);

		}finally{//我加上的
		DEBUG.P(1,this,"isAssignable(2)");
		}
    }

    /**
     * Is t assignable to s?<br>
     * Equivalent to subtype except for constant values and raw
     * types.<br>
     * (not defined for Method and ForAll types)
     */
	//注意赋值(isAssignable)不同于强制转换(isCastable)
	//赋值只能是子类赋给超类，而不能是超类赋给子类
	//如:
	/*
		Integer aInteger = 10;
		Number aNumber=10;
		aNumber=aInteger;//正确
		aInteger=aNumber;//错误

		//下面两个强制转换都合法
		aNumber=(Number)aInteger;
		aInteger=(Integer)aNumber;
	*/
    public boolean isAssignable(Type t, Type s, Warner warn) {
		try {//我加上的
		DEBUG.P(this,"isAssignable(3)");
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));

		boolean returnResult= myIsAssignable(t, s, warn);
            
		
		DEBUG.P("t="+t+"  t.tag="+TypeTags.toString(t.tag));
		DEBUG.P("s="+s+"  s.tag="+TypeTags.toString(s.tag));
		DEBUG.P("returnResult="+returnResult);
		return returnResult;

	  /*
        if (t.tag == ERROR)
            return true;
        if (t.tag <= INT && t.constValue() != null) {
            int value = ((Number)t.constValue()).intValue();
            switch (s.tag) {
            case BYTE:
                if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE)
                    return true;
                break;
            case CHAR:
                if (Character.MIN_VALUE <= value && value <= Character.MAX_VALUE)
                    return true;
                break;
            case SHORT:
                if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE)
                    return true;
                break;
            case INT:
                return true;
            case CLASS:
                switch (unboxedType(s).tag) {
                case BYTE:
                case CHAR:
                case SHORT://当是Integer aInteger = 10;时为INT，
						   //但是这里省略了，INT的情况转到return isConvertible(t, s, warn);
                    return isAssignable(t, unboxedType(s), warn);
                }
                break;
            }
        }
        return isConvertible(t, s, warn);
	  */
		}finally{//我加上的
		DEBUG.P(1,this,"isAssignable(3)");
		}
    }

	private boolean myIsAssignable(Type t, Type s, Warner warn) {
        if (t.tag == ERROR)
            return true;
        if (t.tag <= INT && t.constValue() != null) {
            int value = ((Number)t.constValue()).intValue();
            switch (s.tag) {
            case BYTE:
                if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE)
                    return true;
                break;
            case CHAR:
                if (Character.MIN_VALUE <= value && value <= Character.MAX_VALUE)
                    return true;
                break;
            case SHORT:
                if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE)
                    return true;
                break;
            case INT:
                return true;
            case CLASS:
                switch (unboxedType(s).tag) {
                case BYTE:
                case CHAR:
                case SHORT:
                    return isAssignable(t, unboxedType(s), warn);
                }
                break;
            }
        }
        return isConvertible(t, s, warn);
    }
    // </editor-fold>
//