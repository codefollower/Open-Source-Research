    /**
     * Enter the symbols found in a list of parse trees.
     * As a side-effect, this puts elements on the "todo" list.
     * Also stores a list of all top level classes in rootClasses.
     */
    public List<JCCompilationUnit> enterTrees(List<JCCompilationUnit> roots) {
        DEBUG.P(this,"enterTrees(1)");
        
        //enter symbols for all files
        if (taskListener != null) {
            for (JCCompilationUnit unit: roots) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.ENTER, unit);
                taskListener.started(e);
            }
        }
        
        enter.main(roots);
        
        if (taskListener != null) {
            for (JCCompilationUnit unit: roots) {
                TaskEvent e = new TaskEvent(TaskEvent.Kind.ENTER, unit);
                taskListener.finished(e);
            }
        }

        //If generating source, remember the classes declared in
        //the original compilation units listed on the command line.
        DEBUG.P("sourceOutput="+sourceOutput+" stubOutput="+stubOutput);
        if (sourceOutput || stubOutput) {
            ListBuffer<JCClassDecl> cdefs = lb();
            for (JCCompilationUnit unit : roots) {
                for (List<JCTree> defs = unit.defs;
                     defs.nonEmpty();
                     defs = defs.tail) {
                    if (defs.head instanceof JCClassDecl)
                        cdefs.append((JCClassDecl)defs.head);
                }
            }
            rootClasses = cdefs.toList();
        }
        
        DEBUG.P(2,this,"enterTrees(1)");
        //DEBUG.P("enterTrees(1) stop",true);
        return roots;
    }