    static class ArrayAttributeProxy extends Attribute {
        List<Attribute> values;
        ArrayAttributeProxy(List<Attribute> values) {
            super(null);
            this.values = values;
        }
        public void accept(Visitor v) { ((ProxyVisitor)v).visitArrayAttributeProxy(this); }
        public String toString() {
            return "{" + values + "}";
        }
    }