    /** Queue annotations for later processing. */
    void annotateLater(final List<JCAnnotation> annotations,
                       final Env<AttrContext> localEnv,
                       final Symbol s) {
        try {
        DEBUG.P(this,"annotateLater(3)");
        DEBUG.P("List<JCAnnotation> annotations.size()="+annotations.size());
        DEBUG.P("annotations="+annotations);
        DEBUG.P("annotations.isEmpty()="+annotations.isEmpty());
        DEBUG.P("localEnv="+localEnv);
        DEBUG.P("s="+s);
        
        
        if (annotations.isEmpty()) return;
        DEBUG.P("sym.kind="+Kinds.toString(s.kind));
        
        if (s.kind != PCK) s.attributes_field = null; // mark it incomplete for now
        DEBUG.P("s.attributes_field="+s.attributes_field);
        annotate.later(new Annotate.Annotator() {
                public String toString() {
                    return "annotate " + annotations + " onto " + s + " in " + s.owner;
                }
                public void enterAnnotation() {
					DEBUG.P(this,"enterAnnotation()");
                    assert s.kind == PCK || s.attributes_field == null;
                    JavaFileObject prev = log.useSource(localEnv.toplevel.sourcefile);
                    try {
						//同时编译
						//test/memberEnter/package-info.java
						//test/memberEnter/subdir/package-info.java
						//两文件的内容都为:
						//@PackageAnnotations
						//package test.memberEnter;

						//就可测试下面的错误:
						//test\memberEnter\subdir\package-info.java:1: 软件包 test.memberEnter 已被注释


                        if (s.attributes_field != null &&
                            s.attributes_field.nonEmpty() &&
                            annotations.nonEmpty())
                            log.error(annotations.head.pos,
                                      "already.annotated",
                                      Resolve.kindName(s), s);
                        enterAnnotations(annotations, localEnv, s);
                    } finally {
                        log.useSource(prev);
						DEBUG.P(0,this,"enterAnnotation()");
                    }
                }
            });
            
            
        } finally {
        DEBUG.P(0,this,"annotateLater(3)");
        } 
    }