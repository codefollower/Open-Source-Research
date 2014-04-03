    /** Check the annotations of a symbol.
     */
    public void validateAnnotations(List<JCAnnotation> annotations, Symbol s) {
    try {//我加上的
	DEBUG.P(this,"validateAnnotations(2)");
	//DEBUG.P("暂时跳过注释，不检测");
	
	DEBUG.P("annotations="+annotations);
	DEBUG.P("s="+s);
	DEBUG.P("skipAnnotations="+skipAnnotations);
	
	
	if (skipAnnotations) return;
	for (JCAnnotation a : annotations)
	    validateAnnotation(a, s);
	   
	}finally{//我加上的
	DEBUG.P(2,this,"validateAnnotations(2)");
	}
	
    }

    /** Check an annotation of a symbol.
     */
    public void validateAnnotation(JCAnnotation a, Symbol s) {
    DEBUG.P(this,"validateAnnotation(2)");
    DEBUG.P("a="+a);
	DEBUG.P("s="+s);
	
	validateAnnotation(a);
	/*
	if (!annotationApplicable(a, s))
	    log.error(a.pos(), "annotation.type.not.applicable");
	if (a.annotationType.type.tsym == syms.overrideType.tsym) {
	    if (!isOverrider(s))
		log.error(a.pos(), "method.does.not.override.superclass");
	}
	*/
	//下面两个log.error()的位置都是a.pos()，所以当两个同时出现时，只报告一个错误
	boolean annotationApplicableFlag=annotationApplicable(a, s);
	DEBUG.P("annotationApplicableFlag="+annotationApplicableFlag);
	if (!annotationApplicableFlag)
	    log.error(a.pos(), "annotation.type.not.applicable");

	DEBUG.P("a.annotationType.type.tsym="+a.annotationType.type.tsym);
	DEBUG.P("syms.overrideType.tsym="+syms.overrideType.tsym);
	if (a.annotationType.type.tsym == syms.overrideType.tsym) {
		boolean isOverriderFlag=isOverrider(s);
		DEBUG.P("isOverriderFlag="+isOverriderFlag);
	    if (!isOverriderFlag)
		log.error(a.pos(), "method.does.not.override.superclass");
	}
	
	DEBUG.P(1,this,"validateAnnotation(2)");
    }
