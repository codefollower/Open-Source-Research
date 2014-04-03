public static class StringOverflow extends Exception {
        private static final long serialVersionUID = 0;
        public final String value;
        public StringOverflow(String s) {
            value = s;
        }
    }