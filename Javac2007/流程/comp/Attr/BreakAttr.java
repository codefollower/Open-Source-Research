    private JCTree breakTree = null;
    
    private static class BreakAttr extends RuntimeException {
        static final long serialVersionUID = -6924771130405446405L;
        private Env<AttrContext> env;
        private BreakAttr(Env<AttrContext> env) {
            this.env = env;
        }
    }