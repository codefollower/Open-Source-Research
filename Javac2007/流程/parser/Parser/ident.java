    /**
     * Ident = IDENTIFIER
     */
    Name ident() {
    	try {//我加上的
		DEBUG.P(this,"ident()");
		
        if (S.token() == IDENTIFIER) {
            Name name = S.name();
            DEBUG.P("ident.name="+name);
            S.nextToken();
            return name;
        } else if (S.token() == ASSERT) {
            if (allowAsserts) {
            	/*
            	例:
                F:\Javac\bin\other>javac Test5.java
                Test5.java:4: 从版本 1.4 开始，'assert' 是一个关键字，但不能用作标识符
                （请使用 -source 1.3 或更低版本以便将 'assert' 用作标识符）
                        int assert=0;
                            ^
                1 错误
                */
                log.error(S.pos(), "assert.as.identifier");
                S.nextToken();
                return names.error;//error在com.sun.tools.javac.util.Name.Table中定义
            } else {
            	/*
            	例:
            	F:\Javac\bin\other>javac -source 1.3 Test5.java
                Test5.java:4: 警告：从版本 1.4 开始，'assert' 是一个关键字，但不能用作标识符
                （请使用 -source 1.4 或更高版本以便将 'assert' 用作关键字）
                                int assert=0;
                                    ^
                1 警告
                */
                log.warning(S.pos(), "assert.as.identifier");
                Name name = S.name();
                S.nextToken();
                return name;
            }
        } else if (S.token() == ENUM) {
        	//与ASSERT类似
            if (allowEnums) {
                log.error(S.pos(), "enum.as.identifier");
                S.nextToken();
                return names.error;
            } else {
                log.warning(S.pos(), "enum.as.identifier");
                Name name = S.name();
                S.nextToken();
                return name;
            }
        } else {
            accept(IDENTIFIER);
            return names.error;
        }

		}finally{//我加上的
		DEBUG.P(0,this,"ident()");
		}        
	}