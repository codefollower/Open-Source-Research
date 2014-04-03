    public static class JCAnnotation extends JCExpression implements AnnotationTree {
    	//注释的语法为:Annotation= "@" Qualident [ "(" AnnotationFieldValues ")" ]
        public JCTree annotationType;//对应Qualident
        public List<JCExpression> args;//对应[ "(" AnnotationFieldValues ")" ]
        protected JCAnnotation(JCTree annotationType, List<JCExpression> args) {
            super(ANNOTATION);
            this.annotationType = annotationType;
            this.args = args;
        }
        @Override
        public void accept(Visitor v) { v.visitAnnotation(this); }

        public Kind getKind() { return Kind.ANNOTATION; }
        public JCTree getAnnotationType() { return annotationType; }
        public List<JCExpression> getArguments() {
            return args;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitAnnotation(this, d);
        }
    }