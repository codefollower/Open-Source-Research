    /** A localized string describing the kind of a missing symbol, given an
     *  error kind.
     */
    static JCDiagnostic absentKindName(int kind) {
        switch (kind) {
        case ABSENT_VAR:
            return JCDiagnostic.fragment("kindname.variable");
        case WRONG_MTHS: case WRONG_MTH: case ABSENT_MTH:
            return JCDiagnostic.fragment("kindname.method");
        case ABSENT_TYP:
            return JCDiagnostic.fragment("kindname.class");
        default:
            return JCDiagnostic.fragment("kindname", kind);
        }
    }