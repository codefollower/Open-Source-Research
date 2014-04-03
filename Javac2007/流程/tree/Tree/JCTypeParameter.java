    /**
     * A formal class parameter.
     * @param name name
     * @param bounds bounds
     */
    //从JCTree继承的字段“type”的值在com.sun.tools.javac.comp.Enter类的
    //visitTypeParameter(JCTypeParameter tree)方法中设置，type字段指向TypeVar的实例
    public static class JCTypeParameter extends JCTree implements TypeParameterTree {
        //如:泛型类Test<S extends TestBound & MyInterfaceA>
        //name对应“S”，
        //bounds[0]对应“TestBound”，
        //bounds[1]对应“MyInterfaceA”
        //如果没有extends这个关键字，bounds.size=0
        public Name name;
        public List<JCExpression> bounds;
        protected JCTypeParameter(Name name, List<JCExpression> bounds) {
            super(TYPEPARAMETER);
            this.name = name;
            this.bounds = bounds;
        }
        @Override
        public void accept(Visitor v) { v.visitTypeParameter(this); }

        public Kind getKind() { return Kind.TYPE_PARAMETER; }
        public Name getName() { return name; }
        public List<JCExpression> getBounds() {
            return bounds;
        }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitTypeParameter(this, d);
        }
    }