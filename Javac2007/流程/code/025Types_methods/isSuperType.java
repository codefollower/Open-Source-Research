//isSuperType
    // <editor-fold defaultstate="collapsed" desc="isSuperType">
    /**
     * Is t a supertype of s?
     */
    public boolean isSuperType(Type t, Type s) {
        switch (t.tag) {
        case ERROR:
            return true;
        case UNDETVAR: {
            UndetVar undet = (UndetVar)t;
            if (t == s ||
                undet.qtype == s ||
                s.tag == ERROR ||
                s.tag == BOT) return true;
            if (undet.inst != null)
                return isSubtype(s, undet.inst);
            undet.lobounds = undet.lobounds.prepend(s);
            return true;
        }
        default:
            return isSubtype(s, t);
        }
    }
    // </editor-fold>
//