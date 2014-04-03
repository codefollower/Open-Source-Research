    /** We can only read a single class file at a time; this
     *  flag keeps track of when we are currently reading a class
     *  file.
     */
    private boolean filling = false;

    /** Fill in definition of class `c' from corresponding class or
     *  source file.
     */
    /** Fill in definition of class `c' from corresponding class or
     *  source file.
     */
    private void fillIn(ClassSymbol c) {
    	try {//我加上的
        DEBUG.P(this,"fillIn(ClassSymbol c)");
        DEBUG.P("completionFailureName="+completionFailureName);
        DEBUG.P("c.fullname="+c.fullname);
	
        //加-XDfailcomplete=-XDfailcomplete=java.lang.annotation
        if (completionFailureName == c.fullname) {
            throw new CompletionFailure(c, "user-selected completion failure by class name");
        }
        currentOwner = c;
        JavaFileObject classfile = c.classfile;
        DEBUG.P("classfile="+classfile);
        if (classfile != null) {
            // <editor-fold defaultstate="collapsed">
            JavaFileObject previousClassFile = currentClassFile;
            try {
                assert !filling :
                    "Filling " + classfile.toUri() +
                    " during " + previousClassFile;
                currentClassFile = classfile;
                if (verbose) {
                    printVerbose("loading", currentClassFile.toString());
                }
                if (classfile.getKind() == JavaFileObject.Kind.CLASS) {
                    filling = true;
                    try {
                        bp = 0;
                        buf = readInputStream(buf, classfile.openInputStream());
                        DEBUG.P("");
                        DEBUG.P("bp="+bp);
                        DEBUG.P("buf.length="+buf.length);
                        DEBUG.P("currentOwner="+currentOwner);
                        DEBUG.P("currentOwner.type="+currentOwner.type);
                        readClassFile(c);
                        
                        DEBUG.P("missingTypeVariables="+missingTypeVariables);
                        DEBUG.P("foundTypeVariables  ="+foundTypeVariables);
                        if (!missingTypeVariables.isEmpty() && !foundTypeVariables.isEmpty()) {
                            List<Type> missing = missingTypeVariables;
                            List<Type> found = foundTypeVariables;
                            missingTypeVariables = List.nil();
                            foundTypeVariables = List.nil();
                            filling = false;
                            ClassType ct = (ClassType)currentOwner.type;
                            ct.supertype_field =
                                types.subst(ct.supertype_field, missing, found);
                            ct.interfaces_field =
                                types.subst(ct.interfaces_field, missing, found);
                        } else if (missingTypeVariables.isEmpty() !=
                                   foundTypeVariables.isEmpty()) {
                            /*注意:
                            false!=false => false
                            false!=true  => true
                            true!=false  => true
                            true!=true   => false
                            */
                            Name name = missingTypeVariables.head.tsym.name;
                            throw badClassFile("undecl.type.var", name);
                        }
                    } finally {
                        missingTypeVariables = List.nil();
                        foundTypeVariables = List.nil();
                        filling = false;
                    }
                } else {
                    //如果找到的是(.java)源文件则调用JavaCompiler.complete(1)方法从源码编译
                    if (sourceCompleter != null) {
                        sourceCompleter.complete(c);
                    } else {
                        throw new IllegalStateException("Source completer required to read "
                                                        + classfile.toUri());
                    }
                }
                return;
            } catch (IOException ex) {
                throw badClassFile("unable.to.access.file", ex.getMessage());
            } finally {
                currentClassFile = previousClassFile;
            }
            // </editor-fold>
        } else {
            throw
                newCompletionFailure(c,
                                     Log.getLocalizedString("class.file.not.found",
                                                            c.flatname));
        }
        
        }finally{//我加上的
        DEBUG.P(0,this,"fillIn(ClassSymbol c)");
        }
		
    }