    /**
     * A statement block.
     * @param stats statements
     * @param flags flags
     */
    public static class JCBlock extends JCStatement implements BlockTree {
        public long flags;
        public List<JCStatement> stats;
        /** Position of closing brace, optional. */
        public int endpos = Position.NOPOS;
        protected JCBlock(long flags, List<JCStatement> stats) {
            super(BLOCK);
            this.stats = stats;
            this.flags = flags;
        }
        @Override
        public void accept(Visitor v) { v.visitBlock(this); }

        public Kind getKind() { return Kind.BLOCK; }
        public List<JCStatement> getStatements() {
            return stats;
        }
        public boolean isStatic() { return (flags & Flags.STATIC) != 0; }
        @Override
        public <R,D> R accept(TreeVisitor<R,D> v, D d) {
            return v.visitBlock(this, d);
        }
    }