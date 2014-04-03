//toString
    // <editor-fold defaultstate="collapsed" desc="toString">
    /**
     * This toString is slightly more descriptive than the one on Type.
     */
    public String toString(Type t) {
        if (t.tag == FORALL) {
            ForAll forAll = (ForAll)t;
            return typaramsString(forAll.tvars) + forAll.qtype;
        }
        return "" + t;
    }
    // where
        private String typaramsString(List<Type> tvars) {
            StringBuffer s = new StringBuffer();
            s.append('<');
            boolean first = true;
            for (Type t : tvars) {
                if (!first) s.append(", ");
                first = false;
                appendTyparamString(((TypeVar)t), s);
            }
            s.append('>');
            return s.toString();
        }
        private void appendTyparamString(TypeVar t, StringBuffer buf) {
            buf.append(t);
            if (t.bound == null ||
                t.bound.tsym.getQualifiedName() == names.java_lang_Object)
                return;
            buf.append(" extends "); // Java syntax; no need for i18n
            Type bound = t.bound;
            if (!bound.isCompound()) {
                buf.append(bound);
            } else if ((erasure(t).tsym.flags() & INTERFACE) == 0) {
                buf.append(supertype(t));
                for (Type intf : interfaces(t)) {
                    buf.append('&');
                    buf.append(intf);
                }
            } else {
                // No superclass was given in bounds.
                // In this case, supertype is Object, erasure is first interface.
                boolean first = true;
                for (Type intf : interfaces(t)) {
                    if (!first) buf.append('&');
                    first = false;
                    buf.append(intf);
                }
            }
        }
    // </editor-fold>
//