    /** AnnotationsOpt = { '@' Annotation }
     */
    List<JCAnnotation> annotationsOpt() {
    	try {//我加上的
		DEBUG.P(this,"annotationsOpt()");
		DEBUG.P("S.token()="+S.token());
		
        if (S.token() != MONKEYS_AT) return List.nil(); // optimization
        ListBuffer<JCAnnotation> buf = new ListBuffer<JCAnnotation>();
        while (S.token() == MONKEYS_AT) {
            int pos = S.pos();
            S.nextToken();
            buf.append(annotation(pos));
        }
        return buf.toList();
        
		}finally{//我加上的
		DEBUG.P(0,this,"annotationsOpt()");
		}
    }