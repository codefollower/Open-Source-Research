    public void visitConditional(JCConditional tree) {
		DEBUG.P(this,"visitConditional(1)");

        attribExpr(tree.cond, env, syms.booleanType);
        attribExpr(tree.truepart, env);
        attribExpr(tree.falsepart, env);
        result = check(tree,
                       capture(condType(tree.pos(), tree.cond.type,
                                        tree.truepart.type, tree.falsepart.type)),
                       VAL, pkind, pt);

		DEBUG.P(0,this,"visitConditional(1)");
    }
    //where
        /** Compute the type of a conditional expression, after
         *  checking that it exists. See Spec 15.25.
         *
         *  @param pos      The source position to be used for
         *                  error diagnostics.
         *  @param condtype The type of the expression's condition.
         *  @param thentype The type of the expression's then-part.
         *  @param elsetype The type of the expression's else-part.
         */
        private Type condType(DiagnosticPosition pos,
                              Type condtype,
                              Type thentype,
                              Type elsetype) {
			try {//我加上的
			DEBUG.P(this,"condType(4)");

            Type ctype = condType1(pos, condtype, thentype, elsetype);

            // If condition and both arms are numeric constants,
            // evaluate at compile-time.
            return ((condtype.constValue() != null) &&
                    (thentype.constValue() != null) &&
                    (elsetype.constValue() != null))
                ? cfolder.coerce(condtype.isTrue()?thentype:elsetype, ctype)
                : ctype;

			}finally{//我加上的
			DEBUG.P(0,this,"condType(4)");
			}
        }
        /** Compute the type of a conditional expression, after
         *  checking that it exists.  Does not take into
         *  account the special case where condition and both arms
         *  are constants.
         *
         *  @param pos      The source position to be used for error
         *                  diagnostics.
         *  @param condtype The type of the expression's condition.
         *  @param thentype The type of the expression's then-part.
         *  @param elsetype The type of the expression's else-part.
         */
        private Type condType1(DiagnosticPosition pos, Type condtype,
                               Type thentype, Type elsetype) {
			try {//我加上的
			DEBUG.P(this,"condType1(4)");
			DEBUG.P("condtype="+condtype);
			DEBUG.P("thentype="+thentype);
			DEBUG.P("elsetype="+elsetype);

            // If same type, that is the result
            if (types.isSameType(thentype, elsetype))
                return thentype.baseType();

            Type thenUnboxed = (!allowBoxing || thentype.isPrimitive())
                ? thentype : types.unboxedType(thentype);
            Type elseUnboxed = (!allowBoxing || elsetype.isPrimitive())
                ? elsetype : types.unboxedType(elsetype);

			DEBUG.P("thenUnboxed="+thenUnboxed);
			DEBUG.P("elseUnboxed="+elseUnboxed);

            // Otherwise, if both arms can be converted to a numeric
            // type, return the least numeric type that fits both arms
            // (i.e. return larger of the two, or return int if one
            // arm is short, the other is char).
            if (thenUnboxed.isPrimitive() && elseUnboxed.isPrimitive()) {
                // If one arm has an integer subrange type (i.e., byte,
                // short, or char), and the other is an integer constant
                // that fits into the subrange, return the subrange type.
                if (thenUnboxed.tag < INT && elseUnboxed.tag == INT &&
                    types.isAssignable(elseUnboxed, thenUnboxed))
                    return thenUnboxed.baseType();
                if (elseUnboxed.tag < INT && thenUnboxed.tag == INT &&
                    types.isAssignable(thenUnboxed, elseUnboxed))
                    return elseUnboxed.baseType();

                for (int i = BYTE; i < VOID; i++) {
                    Type candidate = syms.typeOfTag[i];
                    if (types.isSubtype(thenUnboxed, candidate) &&
                        types.isSubtype(elseUnboxed, candidate))
                        return candidate;
                }
            }

            // Those were all the cases that could result in a primitive
            if (allowBoxing) {
                if (thentype.isPrimitive())
                    thentype = types.boxedClass(thentype).type;
                if (elsetype.isPrimitive())
                    elsetype = types.boxedClass(elsetype).type;
            }

            if (types.isSubtype(thentype, elsetype))
                return elsetype.baseType();
            if (types.isSubtype(elsetype, thentype))
                return thentype.baseType();

            if (!allowBoxing || thentype.tag == VOID || elsetype.tag == VOID) {
                log.error(pos, "neither.conditional.subtype",
                          thentype, elsetype);
                return thentype.baseType();
            }

            // both are known to be reference types.  The result is
            // lub(thentype,elsetype). This cannot fail, as it will
            // always be possible to infer "Object" if nothing better.
            return types.lub(thentype.baseType(), elsetype.baseType());

			}finally{//我加上的
			DEBUG.P(0,this,"condType1(2)");
			}
        }
