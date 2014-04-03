    /** Read fractional part of floating point number.
     */
    private void scanFraction() {
        while (digit(10) >= 0) {
	    	putChar(ch);
            scanChar();
        }
        
		int sp1 = sp;
        if (ch == 'e' || ch == 'E') {
	    	putChar(ch);
            scanChar();
            if (ch == '+' || ch == '-') {
				putChar(ch);
                scanChar();
	   		}
	   		
		    if ('0' <= ch && ch <= '9') {
				do {
				    putChar(ch);
				    scanChar();
				} while ('0' <= ch && ch <= '9');
				return;
		    }
			//如:1.2E+w，字符w不是数字0-9，编译器报错:浮点字面值不规则
		    lexError("malformed.fp.lit");
		    sp = sp1;
		}
    }