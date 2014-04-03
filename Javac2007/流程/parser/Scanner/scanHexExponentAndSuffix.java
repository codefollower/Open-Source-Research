    /** Read fractional part of hexadecimal floating point number.
     */
    private void scanHexExponentAndSuffix() {
    	//16进制浮点指数部分(注:p(或P)后面是指数,不能省略,如果是float类型则f(或F)也是必须的)
        if (ch == 'p' || ch == 'P') {
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
				
				if (!allowHexFloats) {
					//例子:0x.1p-1f，当指定选项:-source 1.4时
					//报错:在 -source 5 之前，不支持十六进制浮点字面值
				    lexError("unsupported.fp.lit");
		            allowHexFloats = true;
		        }
		        else if (!hexFloatsWork)
					//该 VM 不支持十六进制浮点字面值
				    lexError("unsupported.cross.fp.lit");
		    } else
				//如:0x.1p-wf，字符w不是数字0-9，编译器报错:浮点字面值不规则
				//但如果是:0x.1p-2wf，虽然字符w不是数字0-9，但不在这里报错
				//这里只检查+-号后面的字符是否是数字
				lexError("malformed.fp.lit");
				
		} else {
			//如:0x.1-1f，少了字符p(或P)，编译器报错:浮点字面值不规则
		    lexError("malformed.fp.lit");
		}
		if (ch == 'f' || ch == 'F') {
		    putChar(ch);
		    scanChar();
	        token = FLOATLITERAL;
		} else {
			/*
			如果浮点数后没有指定后缀f(或F)，那么都把它当成是双精度的，
			这时如果把它赋值给一个float类型的字段，编译器在其他地方(Check类中)会检查，
			如：public float myFloat2=0x.1p-2;

			错误提示如下:

			bin\mysrc\my\test\ScannerTest.java:9: 可能损失精度
			找到： double
			需要： float
					public float myFloat2=0x.1p-2;
										  ^
			1 错误
			*/
		    if (ch == 'd' || ch == 'D') {
				putChar(ch);
				scanChar();
		    }
		    token = DOUBLELITERAL;
		}
    }