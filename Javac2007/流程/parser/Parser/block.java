    /** Block = "{" BlockStatements "}"
     */
    JCBlock block(int pos, long flags) {
    	DEBUG.P(this,"block(int pos, long flags)");
		DEBUG.P("pos="+pos+" flags="+flags+" modifiers=("+Flags.toString(flags)+")");
		
        accept(LBRACE);
        List<JCStatement> stats = blockStatements();
        
        JCBlock t = F.at(pos).Block(flags, stats);
        while (S.token() == CASE || S.token() == DEFAULT) {
        	/*
        	如下代码:
        	{
				case;
			}
			错误提示:“单个 case”或“单个 default”
			*/
            syntaxError("orphaned", keywords.token2string(S.token()));
            switchBlockStatementGroups();
        }
        // the Block node has a field "endpos" for first char of last token, which is
        // usually but not necessarily the last char of the last token.
        t.endpos = S.pos();
        accept(RBRACE);
        
        DEBUG.P(1,this,"block(int pos, long flags)");
        return toP(t);
    }

    public JCBlock block() {
        return block(S.pos(), 0);
    }