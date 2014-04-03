    /** A temporary proxy representing a compound attribute.
     */
    static class CompoundAnnotationProxy extends Attribute {
        final List<Pair<Name,Attribute>> values;
        public CompoundAnnotationProxy(Type type,
                                      List<Pair<Name,Attribute>> values) {
            super(type);
            this.values = values;
        }
        public void accept(Visitor v) { ((ProxyVisitor)v).visitCompoundAnnotationProxy(this); }
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("@");
            buf.append(type.tsym.getQualifiedName());
            buf.append("/*proxy*/{");
            boolean first = true;
            for (List<Pair<Name,Attribute>> v = values;
                 v.nonEmpty(); v = v.tail) {
                Pair<Name,Attribute> value = v.head;
                if (!first) buf.append(",");
                first = false;
                buf.append(value.fst);
                buf.append("=");
                buf.append(value.snd);
            }
            buf.append("}");
            return buf.toString();
        }
    }