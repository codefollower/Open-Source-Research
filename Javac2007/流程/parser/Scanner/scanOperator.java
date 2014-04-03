    /** Read longest possible sequence of special characters and convert
     *  to token.
     */
    private void scanOperator() {
	while (true) {
	    putChar(ch);
	    Name newname = names.fromChars(sbuf, 0, sp);
	    
	    //DEBUG.P("newname="+newname);
	    //如果一个字符能作为一个完整的操作符的一部分，尽可能的把它加到操作符中，
	    //如果最近加入的字符使得原来的操作符变成了一个标识符了，那么往后退一格
        //如:假设先前读到的操作符为“!="，接着读进字符“*”变成了“!=*"，成了一
        //个标识符(IDENTIFIER)了，这时就得往后退一格，还原成“!="
        if (keywords.key(newname) == IDENTIFIER) {
			sp--;
			break;
	    }
	    
        name = newname;
        token = keywords.key(newname);
	    scanChar();
	    if (!isSpecial(ch)) break;
	}
    }