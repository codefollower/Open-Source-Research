    /** Read token.
     */
    public void nextToken() {

	try {
	    prevEndPos = endPos;
	    sp = 0;
	
	    while (true) {
	    //处理完processWhiteSpace()与processLineTerminator()两个
	    //方法后，继续往下扫描字符
		pos = bp;
		switch (ch) {
		case ' ': // (Spec 3.6)
		case '\t': // (Spec 3.6)
		case FF: // (Spec 3.6)   //form feed是指换页
		    do {
			scanChar();
		    } while (ch == ' ' || ch == '\t' || ch == FF);
		    endPos = bp;
		    processWhiteSpace();
		    break;
		case LF: // (Spec 3.4)   //换行,有的系统生成的文件可能没有回车符
		    scanChar();
		    endPos = bp;
		    processLineTerminator();
		    break;
		case CR: // (Spec 3.4)   //回车,回车符后面跟换行符
		    scanChar();
		    if (ch == LF) {
			scanChar();
		    }
		    endPos = bp;
		    processLineTerminator();
		    break;
		//符合java标识符(或保留字)的首字母的情况之一
		case 'A': case 'B': case 'C': case 'D': case 'E':
		case 'F': case 'G': case 'H': case 'I': case 'J':
		case 'K': case 'L': case 'M': case 'N': case 'O':
		case 'P': case 'Q': case 'R': case 'S': case 'T':
		case 'U': case 'V': case 'W': case 'X': case 'Y':
		case 'Z':
		case 'a': case 'b': case 'c': case 'd': case 'e':
		case 'f': case 'g': case 'h': case 'i': case 'j':
		case 'k': case 'l': case 'm': case 'n': case 'o':
		case 'p': case 'q': case 'r': case 's': case 't':
		case 'u': case 'v': case 'w': case 'x': case 'y':
		case 'z':
		case '$': case '_':
		    scanIdent();
		    return;
		case '0': //16或8进制数的情况
		    scanChar();
		    if (ch == 'x' || ch == 'X') {
			scanChar();
			if (ch == '.') {
				//参数为false表示在小数点之前没有数字
			    scanHexFractionAndSuffix(false);
			} else if (digit(16) < 0) {
				//如: 0x、0xw 报错:十六进制数字必须包含至少一位十六进制数
			    lexError("invalid.hex.number");
			} else {
			    scanNumber(16);
			}
		    } else {
			putChar('0');
			scanNumber(8);
		    }
		    return;
		case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
		    scanNumber(10);
		    return;
		case '.':
		    scanChar();
		    if ('0' <= ch && ch <= '9') {
			putChar('.');
			scanFractionAndSuffix();
		    } else if (ch == '.') {  //检测是否是省略符号(...)
			putChar('.'); putChar('.');
			scanChar();
			if (ch == '.') {
			    scanChar();
			    putChar('.');
			    token = ELLIPSIS;
			} else {  //否则认为是浮点错误
			    lexError("malformed.fp.lit");
			}
		    } else {
			token = DOT;
		    }
		    return;
		case ',':
		    scanChar(); token = COMMA; return;
		case ';':
		    scanChar(); token = SEMI; return;
		case '(':
		    scanChar(); token = LPAREN; return;
		case ')':
		    scanChar(); token = RPAREN; return;
		case '[':
		    scanChar(); token = LBRACKET; return;
		case ']':
		    scanChar(); token = RBRACKET; return;
		case '{':
		    scanChar(); token = LBRACE; return;
		case '}':
		    scanChar(); token = RBRACE; return;
		case '/':
		    scanChar();
		    if (ch == '/') {
				do {
					scanCommentChar();
				} while (ch != CR && ch != LF && bp < buflen);
				if (bp < buflen) {
					endPos = bp;
					processComment(CommentStyle.LINE);
				}
				break;
		    } else if (ch == '*') {
				scanChar();
                CommentStyle style;

				if (ch == '*') {
					style = CommentStyle.JAVADOC;
					scanDocComment();
				} else {
					style = CommentStyle.BLOCK;

					while (bp < buflen) {
						if (ch == '*') {
							scanChar();
							if (ch == '/') break;
						} else {
							scanCommentChar();
						}
					}
				}

				if (ch == '/') {
					scanChar();
					endPos = bp;
					processComment(style);
					break;
				} else {
					//未结束的注释
					lexError("unclosed.comment");
					return;
				}
		    } else if (ch == '=') {
			name = names.slashequals;
			token = SLASHEQ;
			scanChar();
		    } else {
			name = names.slash;
			token = SLASH;
		    }
		    return;
		case '\'':  //字符与字符串都不能跨行
		    scanChar();
		    if (ch == '\'') {
			lexError("empty.char.lit");  //空字符字面值
		    } else {
			if (ch == CR || ch == LF)
			    lexError(pos, "illegal.line.end.in.char.lit");//字符字面值的行结尾不合法
			scanLitChar();
			if (ch == '\'') {
			    scanChar();
			    token = CHARLITERAL;
			} else {
			    lexError(pos, "unclosed.char.lit");
			}
		    }
		    return;
		case '\"':
		    scanChar();
		    while (ch != '\"' && ch != CR && ch != LF && bp < buflen)
			scanLitChar();
		    if (ch == '\"') {
			token = STRINGLITERAL;
			scanChar();
		    } else {
			lexError(pos, "unclosed.str.lit");
		    }
		    return;
		default:
		    if (isSpecial(ch)) { //可以作为操作符的某一部分的字符
			scanOperator();
		    } else {
		    	//这里处理其它字符,如中文变量之类的
		    	//与scanIdent()有相同的部分
		    	//注意这里是Start，而scanIdent()是Part
                boolean isJavaIdentifierStart;
                if (ch < '\u0080') {
					// all ASCII range chars already handled, above
					isJavaIdentifierStart = false;
                } else {
					char high = scanSurrogates();
					if (high != 0) {
						if (sp == sbuf.length) {
							putChar(high);
                        } else {
							sbuf[sp++] = high;
						}

						isJavaIdentifierStart = Character.isJavaIdentifierStart(
                                    Character.toCodePoint(high, ch));
					} else {
						isJavaIdentifierStart = Character.isJavaIdentifierStart(ch);
                    }
				}

                if (isJavaIdentifierStart) {
					scanIdent();
		        } else if (bp == buflen || ch == EOI && bp+1 == buflen) { // JLS 3.5
					token = EOF;
					pos = bp = eofPos;
		        } else {
					//如: public char \u007fmyField12
					//报错:非法字符： \127
					lexError("illegal.char", String.valueOf((int)ch));
					scanChar();
		        }
		    }
		    return;
		}//switch
	    }//while
	} finally {
	    endPos = bp;
	    /*
	    if (scannerDebug)
		System.out.println("nextToken(" + pos
				   + "," + endPos + ")=|" +
				   new String(getRawCharacters(pos, endPos))
				   + "|");
		*/
		
		//我多加了tokenName=...(方便查看调试结果)
		if (scannerDebug)
		System.out.println("nextToken(" + pos
				   + "," + endPos + ")=|" +
				   new String(getRawCharacters(pos, endPos))
				   + "|  tokenName=|"+token+ "|  prevEndPos="+prevEndPos);
	}
    }