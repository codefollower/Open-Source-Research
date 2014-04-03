    // TODO: called by JavacTaskImpl
    public JavaCompiler processAnnotations(List<JCCompilationUnit> roots) throws IOException {
    	try {//我加上的
		DEBUG.P(this,"processAnnotations(1)");
		
        return processAnnotations(roots, List.<String>nil());
        
        }finally{//我加上的
		DEBUG.P(0,this,"processAnnotations(1)");
		}
    }

	/**
     * Process any anotations found in the specifed compilation units.
     * @param roots a list of compilation units
     * @return an instance of the compiler in which to complete the compilation
     */
    public JavaCompiler processAnnotations(List<JCCompilationUnit> roots,
                                           List<String> classnames)
        throws IOException  { // TODO: see TEMP note in JavacProcessingEnvironment
        try {//我加上的
		DEBUG.P(this,"processAnnotations(2)");
		DEBUG.P("errorCount()="+errorCount());
		DEBUG.P("processAnnotations="+processAnnotations);
		
        if (errorCount() != 0) {
            // Errors were encountered.  If todo is empty, then the
            // encountered errors were parse errors.  Otherwise, the
            // errors were found during the enter phase which should
            // be ignored when processing annotations.

            if (todo.isEmpty())
                return this;
        }

        // ASSERT: processAnnotations and procEnvImpl should have been set up by
        // by initProcessAnnotations

        // NOTE: The !classnames.isEmpty() checks should be refactored to Main.

        if (!processAnnotations) {
	    // If there are no annotation processors present, and
	    // annotation processing is to occur with compilation,
	    // emit a warning.
	    Options options = Options.instance(context);
	    if (options.get("-proc:only") != null) {
	    //警告：在未请求编译的情况下进行注释处理，但未找到处理程序。
		log.warning("proc.proc-only.requested.no.procs");
		todo.clear();
	    }
            // If not processing annotations, classnames must be empty
            if (!classnames.isEmpty()) {
                log.error("proc.no.explicit.annotation.processing.requested",
                          classnames);
            }
            return this; // continue regular compilation
        } 
        
        try {
        	DEBUG.P("classnames.isEmpty()="+classnames.isEmpty());
            List<ClassSymbol> classSymbols = List.nil();
            List<PackageSymbol> pckSymbols = List.nil();
            if (!classnames.isEmpty()) {
                 // Check for explicit request for annotation
                 // processing
                if (!explicitAnnotationProcessingRequested()) {
                    log.error("proc.no.explicit.annotation.processing.requested",
                              classnames);
                    return this; // TODO: Will this halt compilation?
               } else {
                    boolean errors = false;
                    for (String nameStr : classnames) {
                        Symbol sym = resolveIdent(nameStr);
                        DEBUG.P("sym="+sym);
                        if(sym!=null) {
                        	DEBUG.P("sym.kind="+Kinds.toString(sym.kind));
	                        DEBUG.P("processPcks="+processPcks);
	                    }
                        //加“-XDprocess.packages”选项时processPcks=true
                        if (sym == null || (sym.kind == Kinds.PCK && !processPcks)) {
                            log.error("proc.cant.find.class", nameStr);
                            errors = true;
                            continue;
                        }
                        try {
                            if (sym.kind == Kinds.PCK)
                                sym.complete();

							DEBUG.P("sym.exists()="+sym.exists());
                            if (sym.exists()) {
                                Name name = names.fromString(nameStr);
                                if (sym.kind == Kinds.PCK)
                                    pckSymbols = pckSymbols.prepend((PackageSymbol)sym);
                                else
                                    classSymbols = classSymbols.prepend((ClassSymbol)sym);
                                continue;
                            }
                            assert sym.kind == Kinds.PCK;
                            log.warning("proc.package.does.not.exist", nameStr);
                            pckSymbols = pckSymbols.prepend((PackageSymbol)sym);
                        } catch (CompletionFailure e) {
                            log.error("proc.cant.find.class", nameStr);
                            errors = true;
                            continue;
                        }
                    }
                    if (errors)
                        return this;
                }
            }
            JavaCompiler c = procEnvImpl.doProcessing(context, roots, classSymbols, pckSymbols);
            if (c != this) 
                annotationProcessingOccurred = c.annotationProcessingOccurred = true;
            return c;
        } catch (CompletionFailure ex) {
	    log.error("cant.access", ex.sym, ex.errmsg);
            return this;
            
        }
        
        }finally{//我加上的
		DEBUG.P(3,this,"processAnnotations(2)");
		}
    }