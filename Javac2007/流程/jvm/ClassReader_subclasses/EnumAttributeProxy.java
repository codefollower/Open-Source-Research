    static class EnumAttributeProxy extends Attribute {
        Type enumType;
        Name enumerator;
        public EnumAttributeProxy(Type enumType, Name enumerator) {
            super(null);
            this.enumType = enumType;
            this.enumerator = enumerator;
        }
        public void accept(Visitor v) { ((ProxyVisitor)v).visitEnumAttributeProxy(this); }
        public String toString() {
            return "/*proxy enum*/" + enumType + "." + enumerator;
        }
    }