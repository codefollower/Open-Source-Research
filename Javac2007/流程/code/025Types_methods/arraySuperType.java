        private Type arraySuperType = null;
        private Type arraySuperType() {
            // initialized lazily to avoid problems during compiler startup
            if (arraySuperType == null) {
                synchronized (this) {
                    if (arraySuperType == null) {
                        // JLS 10.8: all arrays implement Cloneable and Serializable.
                        arraySuperType = makeCompoundType(List.of(syms.serializableType,
                                                                  syms.cloneableType),
                                                          syms.objectType);
                    }
                }
            }
            return arraySuperType;
        }