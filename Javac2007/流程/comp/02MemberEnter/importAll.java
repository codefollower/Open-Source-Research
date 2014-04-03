    /** Import all classes of a class or package on demand.
     *  @param pos           Position to be used for error reporting.
     *  @param tsym          The class or package the members of which are imported.
     *  @param toScope   The (import) scope in which imported classes
     *               are entered.
     */
    //tsym可能是一个包也可能是一个类，
    //如果是一个包，就把这个包中的所有类导入env.toplevel.starImportScope
    //如果是一个类，就把在这个类中定义的所有成员类导入env.toplevel.starImportScope
    private void importAll(int pos,
                           final TypeSymbol tsym,
                           Env<AttrContext> env) {
        DEBUG.P(this,"importAll(3)");
        DEBUG.P("tsym="+tsym+" tsym.kind="+Kinds.toString(tsym.kind));
        
        //当tsym.kind == PCK时说明tsym是PackageSymbol的实例引用，当执行
        //tsym.members()时会调用ClassReader类的complete()导入tsym所表示的包中的所有类
        // Check that packages imported from exist (JLS ???).
        if (tsym.kind == PCK && tsym.members().elems == null && !tsym.exists()) {
        	//EXISTS标志在com.sun.tools.javac.jvm.ClassReader.includeClassFile(2)里设置
        	
            // If we can't find java.lang, exit immediately.
            if (((PackageSymbol)tsym).fullname.equals(names.java_lang)) {
                JCDiagnostic msg = JCDiagnostic.fragment("fatal.err.no.java.lang");
                //类全限定名称:com.sun.tools.javac.util.FatalError
                throw new FatalError(msg);
            } else {
                //例:import test2.*;(假设test2不存在)
                log.error(pos, "doesnt.exist", tsym);
            }
        }
        final Scope fromScope = tsym.members();
        //java.lang包中的所有类在默认情况下不用import
        final Scope toScope = env.toplevel.starImportScope;
        
        DEBUG.P("fromScope="+fromScope);
        DEBUG.P("toScope(for前)="+toScope);

        for (Scope.Entry e = fromScope.elems; e != null; e = e.sibling) {
        	//调用Symbol.ClassSymbol.getKind()会触发complete()
        	//所以调试时最好别用
        	//DEBUG.P("Entry e.sym="+e.sym+" (kind="+e.sym.getKind()+")");
        	//DEBUG.P("e.sym="+e.sym);
        	//DEBUG.P("toScope.nelems="+toScope.nelems);
            if (e.sym.kind == TYP && !toScope.includes(e.sym))
                toScope.enter(e.sym, fromScope);//注意这里,是ImportEntry
            else //if (e.sym.kind == TYP && toScope.includes(e.sym))
            	DEBUG.P("e.sym="+e.sym+"  已存在");
            //DEBUG.P("toScope.nelems="+toScope.nelems);
        }
        
        DEBUG.P("toScope(for后)="+toScope);
        DEBUG.P(1,this,"importAll(3)");    
    }
