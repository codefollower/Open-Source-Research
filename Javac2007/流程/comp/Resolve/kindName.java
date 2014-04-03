/* ***************************************************************************
 *  Methods related to kinds
 ****************************************************************************/

    /** A localized string describing a given kind.
     */
    static JCDiagnostic kindName(int kind) {
        switch (kind) {
        case PCK: return JCDiagnostic.fragment("kindname.package");
        case TYP: return JCDiagnostic.fragment("kindname.class");
        case VAR: return JCDiagnostic.fragment("kindname.variable");
        case VAL: return JCDiagnostic.fragment("kindname.value");
        case MTH: return JCDiagnostic.fragment("kindname.method");
        default : return JCDiagnostic.fragment("kindname",
                                               Integer.toString(kind)); //debug
        }
    }

    static JCDiagnostic kindName(Symbol sym) {
        switch (sym.getKind()) {
        case PACKAGE:
            return JCDiagnostic.fragment("kindname.package");

        case ENUM:
        case ANNOTATION_TYPE:
        case INTERFACE:
        case CLASS:
            return JCDiagnostic.fragment("kindname.class");

        case TYPE_PARAMETER:
            return JCDiagnostic.fragment("kindname.type.variable");

        case ENUM_CONSTANT:
        case FIELD:
        case PARAMETER:
        case LOCAL_VARIABLE:
        case EXCEPTION_PARAMETER:
            return JCDiagnostic.fragment("kindname.variable");

        case METHOD:
        case CONSTRUCTOR:
        case STATIC_INIT:
        case INSTANCE_INIT:
            return JCDiagnostic.fragment("kindname.method");

        default:
            if (sym.kind == VAL)
                // I don't think this can happen but it can't harm
                // playing it safe --ahe
                return JCDiagnostic.fragment("kindname.value");
            else
                return JCDiagnostic.fragment("kindname", sym.getKind()); // debug
        }
    }

    /** A localized string describing a given set of kinds.
     */
    static JCDiagnostic kindNames(int kind) {
        StringBuffer key = new StringBuffer();
        key.append("kindname");
        if ((kind & VAL) != 0)
            key.append(((kind & VAL) == VAR) ? ".variable" : ".value");
        if ((kind & MTH) != 0) key.append(".method");
        if ((kind & TYP) != 0) key.append(".class");
        if ((kind & PCK) != 0) key.append(".package");
        return JCDiagnostic.fragment(key.toString(), kind);
    }
