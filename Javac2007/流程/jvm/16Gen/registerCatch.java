    /** Register a catch clause in the "Exceptions" code-attribute.
	 */
	void registerCatch(DiagnosticPosition pos,
			   int startpc, int endpc,
			   int handler_pc, int catch_type) {
		//handler_pc是catch子句中第一条指令的偏移量，
		//catch_type是捕获的异常类在常量池中的索引
		DEBUG.P(this,"registerCatch(5)"); 
		DEBUG.P("startpc="+startpc);
		DEBUG.P("endpc="+endpc);

	    if (startpc != endpc) {
			char startpc1 = (char)startpc;
			char endpc1 = (char)endpc;
			char handler_pc1 = (char)handler_pc;
			if (startpc1 == startpc &&
				endpc1 == endpc &&
				handler_pc1 == handler_pc) {
				code.addCatch(startpc1, endpc1, handler_pc1,
					  (char)catch_type);
			} else {
				if (!useJsrLocally && !target.generateStackMapTable()) {
							useJsrLocally = true;
							throw new CodeSizeOverflow();
				} else {
					log.error(pos, "limit.code.too.large.for.try.stmt");
					nerrs++;
				}
			}
	    }
	    DEBUG.P(0,this,"registerCatch(5)");
	}