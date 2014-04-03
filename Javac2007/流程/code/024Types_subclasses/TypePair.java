        class TypePair {
            final Type t1;
            final Type t2;
            TypePair(Type t1, Type t2) {
                this.t1 = t1;
                this.t2 = t2;
            }
            @Override
            public int hashCode() {
                return 127 * Types.this.hashCode(t1) + Types.this.hashCode(t2);
            }
            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof TypePair))
                    return false;
                TypePair typePair = (TypePair)obj;
                return isSameType(t1, typePair.t1)
                    && isSameType(t2, typePair.t2);
            }
        }