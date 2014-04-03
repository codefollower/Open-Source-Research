    /** Annotation              = "@" Qualident [ "(" AnnotationFieldValues ")" ]
     * @param pos position of "@" token
     */
    JCAnnotation annotation(int pos) {
    	try {//我加上的
        DEBUG.P(this,"annotation(int pos)");
        DEBUG.P("pos="+pos);


        // accept(AT); // AT consumed by caller
        checkAnnotations();
        JCTree ident = qualident();
        List<JCExpression> fieldValues = annotationFieldValuesOpt();
        JCAnnotation ann = F.at(pos).Annotation(ident, fieldValues);
        storeEnd(ann, S.prevEndPos());
        return ann;
        
        
        }finally{//我加上的
        DEBUG.P(0,this,"annotation(int pos)");
        }
    }

    List<JCExpression> annotationFieldValuesOpt() {
        return (S.token() == LPAREN) ? annotationFieldValues() : List.<JCExpression>nil();
    }

    /** AnnotationFieldValues   = "(" [ AnnotationFieldValue { "," AnnotationFieldValue } ] ")" */
    List<JCExpression> annotationFieldValues() {
    	try {//我加上的
		DEBUG.P(this,"annotationFieldValues()");

        accept(LPAREN);
        ListBuffer<JCExpression> buf = new ListBuffer<JCExpression>();
        if (S.token() != RPAREN) {
            buf.append(annotationFieldValue());
            while (S.token() == COMMA) {
                S.nextToken();
                buf.append(annotationFieldValue());
            }
        }
        accept(RPAREN);
        return buf.toList();
        
        }finally{//我加上的
		DEBUG.P(0,this,"annotationFieldValues()");
		}        
    }

    /** AnnotationFieldValue    = AnnotationValue
     *                          | Identifier "=" AnnotationValue
     */
    JCExpression annotationFieldValue() {
    	try {//我加上的
		DEBUG.P(this,"annotationFieldValue()");
		
        if (S.token() == IDENTIFIER) {
            mode = EXPR;
            JCExpression t1 = term1();
            if (t1.tag == JCTree.IDENT && S.token() == EQ) {
                int pos = S.pos();
                accept(EQ);
                return toP(F.at(pos).Assign(t1, annotationValue()));
            } else {
                return t1;
            }
        }
        return annotationValue();
        
        }finally{//我加上的
		DEBUG.P(0,this,"annotationFieldValue()");
		} 
    }

    /* AnnotationValue          = ConditionalExpression
     *                          | Annotation
     *                          | "{" [ AnnotationValue { "," AnnotationValue } ] "}"
     */
    JCExpression annotationValue() {
    	try {//我加上的
		DEBUG.P(this,"annotationValue()");
		
        int pos;
        //JDK1.6中有关于注释字段取值的文档在technotes/guides/language/annotations.html
        switch (S.token()) {
        case MONKEYS_AT:  //注释字段的值是注释的情况
            pos = S.pos();
            S.nextToken();
            return annotation(pos);
        case LBRACE:  //注释字段的值是数组的情况
            pos = S.pos();
            accept(LBRACE);
            ListBuffer<JCExpression> buf = new ListBuffer<JCExpression>();
            if (S.token() != RBRACE) {
                buf.append(annotationValue());
                while (S.token() == COMMA) {
                    S.nextToken();
                    if (S.token() == RPAREN) break;
                    buf.append(annotationValue());
                }
            }
            accept(RBRACE);
            
            //JCNewArray的语法类似如下:
            //new type dimensions initializers 或
            //new type dimensions [ ] initializers
            //看com.sun.source.tree.NewArrayTree
            return toP(F.at(pos).NewArray(null, List.<JCExpression>nil(), buf.toList()));
        default:
            mode = EXPR;
            return term1();
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"annotationValue()");
		} 
    }