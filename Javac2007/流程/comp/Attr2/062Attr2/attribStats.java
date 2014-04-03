    /** Attribute a list of statements, returning nothing.
     */
    <T extends JCTree> void attribStats(List<T> trees, Env<AttrContext> env) {
    	DEBUG.P(this,"attribStats(2)");
        for (List<T> l = trees; l.nonEmpty(); l = l.tail)
            attribStat(l.head, env);
        DEBUG.P(0,this,"attribStats(2)");
    }