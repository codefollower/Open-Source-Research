    /** A localized string describing the kind -- either class or interface --
     *  of a given type.
     */
    static JCDiagnostic typeKindName(Type t) {
        if (t.tag == TYPEVAR ||
            t.tag == CLASS && (t.tsym.flags() & COMPOUND) != 0)
            return JCDiagnostic.fragment("kindname.type.variable.bound");
        else if (t.tag == PACKAGE)
            return JCDiagnostic.fragment("kindname.package");
        else if ((t.tsym.flags_field & ANNOTATION) != 0)
            return JCDiagnostic.fragment("kindname.annotation");
        else if ((t.tsym.flags_field & INTERFACE) != 0)
            return JCDiagnostic.fragment("kindname.interface");
        else
            return JCDiagnostic.fragment("kindname.class");
    }