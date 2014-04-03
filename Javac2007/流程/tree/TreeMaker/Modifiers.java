    public JCModifiers Modifiers(long flags, List<JCAnnotation> annotations) {
        JCModifiers tree = new JCModifiers(flags, annotations);
        boolean noFlags = (flags & Flags.StandardFlags) == 0;
        tree.pos = (noFlags && annotations.isEmpty()) ? Position.NOPOS : pos;
        return tree;
    }

    public JCModifiers Modifiers(long flags) {
        return Modifiers(flags, List.<JCAnnotation>nil());
    }