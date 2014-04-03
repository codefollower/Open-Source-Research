    /**
     * A new[...] operation.
     */
    public static class JCNewArray extends JCExpression implements NewArrayTree {
        //例1:int a1[]=new int[2];
        //例2:byte a2[][]=new byte[][]{{1,2,3},{4,5,6}};
        public JCExpression elemtype;//例1:int   例2:byte[]
        public List<JCExpression> dims;//例1:dims.size=2   例2:dims.size=0
        public List<JCExpression> elems;//例1:null   例2:{1, 2, 3},{4, 5, 6}
        protected JCNewArray(JCExpression elemtype,
			   List<JCExpression> dims,
			   List<JCExpression> elems)
	{
            super(NEWARRAY);

            DEBUG.P(this,"JCNewArray(3)");
            DEBUG.P("elemtype="+elemtype);
            DEBUG.P("dims="+dims);
            DEBUG.P("elems="+elems);
            
            this.elemtype = elemtype;
            this.dims = dims;
            this.elems = elems;
            
            DEBUG.P(0,this,"JCNewArray(3)");
        }
        @Override
        public void accept(Visitor v) { v.visitNewArray(this); }

        public Kind getKind() { return Kind.NEW_ARRAY; }
        public JCExpression getType() { return elemtype; }
        public List<JCExpression> getDimensions() {
            return dims;
        }
        public List<JCExpression> getInitializers() {
            return elems;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitNewArray(this, d);
        }
    }