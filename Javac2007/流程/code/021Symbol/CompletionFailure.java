    public static class CompletionFailure extends RuntimeException {
        private static final long serialVersionUID = 0;
        public Symbol sym;

        /** A localized string describing the failure.
         */
        public String errmsg;

        public CompletionFailure(Symbol sym, String errmsg) {
            this.sym = sym;
            this.errmsg = errmsg;
//          this.printStackTrace();//DEBUG
        }

        public String getMessage() {
            return errmsg;
        }

        @Override
        public CompletionFailure initCause(Throwable cause) {
            super.initCause(cause);
            return this;
        }

    }