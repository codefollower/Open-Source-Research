    /** Main method: enter all classes in a list of toplevel trees.
     *	@param trees	  The list of trees to be processed.
     */
    public void main(List<JCCompilationUnit> trees) {
		DEBUG.P(this,"main(1)");
		complete(trees, null);
		DEBUG.P(0,this,"main(1)");
    }

    /** Main method: enter one class from a list of toplevel trees and
     *  place the rest on uncompleted for later processing.
     *  @param trees      The list of trees to be processed.
     *  @param c          The class symbol to be processed.
     */
     
    //在从MemberEnter阶段进行到Resolve.loadClass(Env<AttrContext> env, Name name)时，
    //如果一个类的超类还没有编译，则先从头开始编译超类，又会从JavaCompiler.complete(ClassSymbol c)
    //转到这里，此时 ClassSymbol c就不为null了
    public void complete(List<JCCompilationUnit> trees, ClassSymbol c) {
    	DEBUG.P(this,"complete(2)");
    	//DEBUG.P("完成Enter前List<JCCompilationUnit> trees的内容: trees.size="+trees.size());
    	//DEBUG.P("------------------------------------------------------------------------------");
    	//DEBUG.P(""+trees);
    	//DEBUG.P("------------------------------------------------------------------------------");
		/*
    	if(typeEnvs!=null) {
            DEBUG.P("");
            DEBUG.P("Env总数: "+typeEnvs.size());
            DEBUG.P("--------------------------");
            for(Map.Entry<TypeSymbol,Env<AttrContext>> myMapEntry:typeEnvs.entrySet())
                    DEBUG.P(""+myMapEntry);
            DEBUG.P("");	
        }
        DEBUG.P("memberEnter.completionEnabled="+memberEnter.completionEnabled);
		*/
    	
       
        annotate.enterStart();
        ListBuffer<ClassSymbol> prevUncompleted = uncompleted;
        if (memberEnter.completionEnabled) uncompleted = new ListBuffer<ClassSymbol>();

        DEBUG.P("ListBuffer<ClassSymbol> uncompleted.size()="+uncompleted.size());//0

        try {
            // enter all classes, and construct uncompleted list
            classEnter(trees, null);


            DEBUG.P(5);
            DEBUG.P("***进入第二阶段MemberEnter***");
            DEBUG.P("-----------------------------------------------");

            //uncompleted中不含本地类
            DEBUG.P("memberEnter.completionEnabled="+memberEnter.completionEnabled);
            //DEBUG.P("ListBuffer<ClassSymbol> uncompleted.size()="+uncompleted.size());//!=0

            // complete all uncompleted classes in memberEnter
            if (memberEnter.completionEnabled) {
                if(uncompleted!=null) DEBUG.P("uncompleted="+uncompleted.size()+" "+uncompleted.toList());
                else DEBUG.P("uncompleted=null");
                
                // <editor-fold defaultstate="collapsed">

                while (uncompleted.nonEmpty()) {
                    ClassSymbol clazz = uncompleted.next();
                    DEBUG.P("Uncompleted SymbolName="+clazz);
                    DEBUG.P("clazz.completer="+clazz.completer);
                    DEBUG.P("(c == null)="+(c == null));
                    DEBUG.P("(c == clazz)="+(c == clazz));
                    DEBUG.P("(prevUncompleted == null)="+(prevUncompleted == null));
                    /*
                    if(c!=null) DEBUG.P("c.name="+c.name+" c.kind="+c.kind);
                    else DEBUG.P("c.name=null c.kind=null");
                    if(clazz!=null) DEBUG.P("clazz.name="+clazz.name+" clazz.kind="+clazz.kind);
                    else DEBUG.P("clazz.name=null clazz.kind=null");
                    */

                    //当从MemberEnter阶段进行到这里时，c!=null，c在uncompleted中，
                    //条件c == clazz至少满足一次，所以对c调用complete()，
                    //但是如果c有内部类，因为c!=null且c != clazz(内部类)且
                    //prevUncompleted != null(因第一次进入MemberEnter阶段时uncompleted!=null)
                    //所以c的所有内部类暂时不调用complete()，先放入prevUncompleted中，留到后面调用
                    if (c == null || c == clazz || prevUncompleted == null)
                        clazz.complete();
                    else
                        // defer
                        prevUncompleted.append(clazz);

                    DEBUG.P("");
                }
                // </editor-fold>

				DEBUG.P("trees="+trees);

                // if there remain any unimported toplevels (these must have
                // no classes at all), process their import statements as well.
                for (JCCompilationUnit tree : trees) {
                    DEBUG.P(2);
                    DEBUG.P("tree.starImportScope="+tree.starImportScope);
                    DEBUG.P("tree.namedImportScope="+tree.namedImportScope);
					DEBUG.P("tree.starImportScope.elems="+tree.starImportScope.elems);
                    if (tree.starImportScope.elems == null) {
                        JavaFileObject prev = log.useSource(tree.sourcefile);
                        //有点怪typeEnvs =new HashMap<TypeSymbol,Env<AttrContext>>();
                        //而tree是JCCompilationUnit，怎么get???????????

						//同时编译package-info.java时就会出现这种情况
                        Env<AttrContext> env = typeEnvs.get(tree);
						DEBUG.P("env="+env);
                        if (env == null)
                            env = topLevelEnv(tree);
                        memberEnter.memberEnter(tree, env);
                        log.useSource(prev);
                    }
                }

				DEBUG.P("Enter结束:for (JCCompilationUnit tree : trees)");
				DEBUG.P(3);
            }
        } finally {
            uncompleted = prevUncompleted;
            annotate.enterDone();

            if(uncompleted!=null) DEBUG.P("uncompleted="+uncompleted.size()+" "+uncompleted.toList());
            else DEBUG.P("uncompleted=null");

            //DEBUG.P(2);
            //DEBUG.P("完成Enter后List<JCCompilationUnit> trees的内容: trees.size="+trees.size());
            //DEBUG.P("------------------------------------------------------------------------------");
            //DEBUG.P(""+trees);
            //DEBUG.P("------------------------------------------------------------------------------");
            DEBUG.P(2,this,"complete(2)");
        }
    }