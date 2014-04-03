    /** Construct a new compiler using a shared context.
     */
    public JavaCompiler(final Context context) {
    	DEBUG.P(this,"JavaCompiler(1)");
        this.context = context;
        context.put(compilerKey, this);

        // if fileManager not already set, register the JavacFileManager to be used
        if (context.get(JavaFileManager.class) == null)
            JavacFileManager.preRegister(context);

        names = Name.Table.instance(context);
        log = Log.instance(context);
        reader = ClassReader.instance(context);
        make = TreeMaker.instance(context);
        writer = ClassWriter.instance(context);
        enter = Enter.instance(context);
        todo = Todo.instance(context);//类全限定名称:com.sun.tools.javac.comp.Todo

        fileManager = context.get(JavaFileManager.class);
        parserFactory = Parser.Factory.instance(context);

        try {
            // catch completion problems with predefineds
            syms = Symtab.instance(context);//这一步值得注意
        } catch (CompletionFailure ex) { //类全限定名称:com.sun.tools.javac.code.Symbol.CompletionFailure
            // inlined Check.completionError as it is not initialized yet
            log.error("cant.access", ex.sym, ex.errmsg);
            if (ex instanceof ClassReader.BadClassFile)
                throw new Abort();
        }
        source = Source.instance(context);
        attr = Attr.instance(context);
        chk = Check.instance(context);
        gen = Gen.instance(context);
        flow = Flow.instance(context);
        transTypes = TransTypes.instance(context);
        lower = Lower.instance(context);
        annotate = Annotate.instance(context);
        types = Types.instance(context);
        taskListener = context.get(TaskListener.class);

        reader.sourceCompleter = this;

        Options options = Options.instance(context);
        DEBUG.P("options="+options);
        
        //下面的选项有些在com.sun.tools.javac.main.OptionName类中是没有的
        verbose       = options.get("-verbose")       != null;
        sourceOutput  = options.get("-printsource")   != null; // used to be -s
        stubOutput    = options.get("-stubs")         != null;
        relax         = options.get("-relax")         != null;
        printFlat     = options.get("-printflat")     != null;
        attrParseOnly = options.get("-attrparseonly") != null;
        encoding      = options.get("-encoding");
        lineDebugInfo = options.get("-g:")            == null ||
                        options.get("-g:lines")       != null;
                        
        genEndPos     = options.get("-Xjcov")         != null ||
        				//类全限定名称:javax.tools.DiagnosticListener
        				//见Log类的Log(4)方法
                        context.get(DiagnosticListener.class) != null;
                      
        devVerbose    = options.get("dev") != null;  
        processPcks   = options.get("process.packages") != null;

        verboseCompilePolicy = options.get("verboseCompilePolicy") != null;
        
        DEBUG.P("genEndPos="+genEndPos);  
        DEBUG.P("devVerbose="+devVerbose);  
        DEBUG.P("processPcks="+processPcks);  
        DEBUG.P("verboseCompilePolicy="+verboseCompilePolicy);  
        DEBUG.P("attrParseOnly="+attrParseOnly); 

        if (attrParseOnly)
            compilePolicy = CompilePolicy.ATTR_ONLY;
        else
            compilePolicy = CompilePolicy.decode(options.get("compilePolicy"));
        
        implicitSourcePolicy = ImplicitSourcePolicy.decode(options.get("-implicit"));

        completionFailureName =
            (options.get("failcomplete") != null)
            ? names.fromString(options.get("failcomplete"))
            : null;
            
        DEBUG.P(0,this,"JavaCompiler(1)");
    }