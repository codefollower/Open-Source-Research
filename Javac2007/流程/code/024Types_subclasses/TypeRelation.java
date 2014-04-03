/**
     * A plain relation on types.  That is a 2-ary function on the
     * form Type&nbsp;&times;&nbsp;Type&nbsp;&rarr;&nbsp;Boolean.
     * <!-- In plain text: Type x Type -> Boolean -->
     */
    public static abstract class TypeRelation extends SimpleVisitor<Boolean,Type> {}
