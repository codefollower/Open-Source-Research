    /**
     * A wrapper for a type that allows use in sets.
     */
    class SingletonType {
        final Type t;
        SingletonType(Type t) {
            this.t = t;
        }
        public int hashCode() {
            return Types.this.hashCode(t);
        }
        public boolean equals(Object obj) {
            return (obj instanceof SingletonType) &&
                isSameType(t, ((SingletonType)obj).t);
        }
        public String toString() {
            return t.toString();
        }
    }