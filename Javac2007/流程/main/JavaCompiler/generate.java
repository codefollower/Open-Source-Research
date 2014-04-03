    /** Generates the source or class file for a list of classes.
     * The decision to generate a source file or a class file is
     * based upon the compiler's options.
     * Generation stops if an error occurs while writing files.
     */
    public void generate(List<Pair<Env<AttrContext>, JCClassDecl>> list) {
        DEBUG.P(this,"generate(1)");
		
        generate(list, null);
        
        DEBUG.P(1,this,"generate(1)");
    }
    
    public void generate(List<Pair<Env<AttrContext>, JCClassDecl>> list, ListBuffer<JavaFileObject> results) {
        try {//我加上的
        DEBUG.P(this,"generate(2)");
        
        boolean usePrintSource = (stubOutput || sourceOutput || printFlat);
        
        DEBUG.P("usePrintSource="+usePrintSource);
        DEBUG.P("list.size()="+list.size());

        for (List<Pair<Env<AttrContext>, JCClassDecl>> l = list; l.nonEmpty(); l = l.tail) {
            Pair<Env<AttrContext>, JCClassDecl> x = l.head;
            Env<AttrContext> env = x.fst;
            JCClassDecl cdef = x.snd;
            
            DEBUG.P("env="+env);
            DEBUG.P("cdef.sym="+cdef.sym);

            if (verboseCompilePolicy) {
                log.printLines(log.noticeWriter, "[generate "
                               + (usePrintSource ? " source" : "code")
                               + " " + env.enclClass.sym + "]");
            }

            if (taskListener != null) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.GENERATE, env.toplevel, cdef.sym);
                taskListener.started(e);
            }

            JavaFileObject prev = log.useSource(env.enclClass.sym.sourcefile != null ?
                                      env.enclClass.sym.sourcefile :
                                      env.toplevel.sourcefile);
            try {
                JavaFileObject file;
                if (usePrintSource)
                    file = printSource(env, cdef);
                else
                    file = genCode(env, cdef);
                if (results != null && file != null)
                    results.append(file);
            } catch (IOException ex) {
                log.error(cdef.pos(), "class.cant.write",
                          cdef.sym, ex.getMessage());
                return;
            } finally {
                log.useSource(prev);
            }

            if (taskListener != null) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.GENERATE, env.toplevel, cdef.sym);
                taskListener.finished(e);
            }
        }
        
        }finally{//我加上的
        DEBUG.P(1,this,"generate(2)");
    	}
    }