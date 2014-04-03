    /**
     * The phases following annotation processing: attribution,
     * desugar, and finally code generation.
     */
    private void compile2() {
    	DEBUG.P(this,"compile2() (字节码从这开始生成)");
    	DEBUG.P("compilePolicy="+compilePolicy);
    	if(todo.nonEmpty()) {
    		DEBUG.P("todo env lists:");
    		DEBUG.P("---------------------------------------------------");
    		for(Env<AttrContext> e:todo) DEBUG.P(""+e);
    	}
    	else DEBUG.P("todo=null");
    	DEBUG.P("");
    	
    	
        try {
            switch (compilePolicy) {
            case ATTR_ONLY:
                attribute(todo);
                break;

            case CHECK_ONLY:
                flow(attribute(todo));
                break;

            case SIMPLE:
                generate(desugar(flow(attribute(todo))));
                break;

            case BY_FILE:
                for (List<Env<AttrContext>> list : groupByFile(flow(attribute(todo))).values())
                    generate(desugar(list));
                break;

            case BY_TODO:
                while (todo.nonEmpty())
                    generate(desugar(flow(attribute(todo.next()))));
                break;

            default:
                assert false: "unknown compile policy";
            }
        } catch (Abort ex) {
            if (devVerbose)
                ex.printStackTrace();
        }

        if (verbose) {
	    elapsed_msec = elapsed(start_msec);;
            printVerbose("total", Long.toString(elapsed_msec));
		}

        reportDeferredDiagnostics();

        if (!log.hasDiagnosticListener()) {
            printCount("error", errorCount());
            printCount("warn", warningCount());
        }
        
        DEBUG.P(0,this,"compile2()");
    }
