    public void visitBinary(JCBinary tree) {
    	DEBUG.P(this,"visitBinary(1)");
        // Attribute arguments.
        Type left = chk.checkNonVoid(tree.lhs.pos(), attribExpr(tree.lhs, env));
        Type right = chk.checkNonVoid(tree.lhs.pos(), attribExpr(tree.rhs, env));

        // Find operator.
        Symbol operator = tree.operator =
            rs.resolveBinaryOperator(tree.pos(), tree.tag, env, left, right);

        Type owntype = syms.errType;
        if (operator.kind == MTH) {
            owntype = operator.type.getReturnType();
            int opc = chk.checkOperator(tree.lhs.pos(),
                                        (OperatorSymbol)operator,
                                        tree.tag,
                                        left,
                                        right);

            // If both arguments are constants, fold them.
            if (left.constValue() != null && right.constValue() != null) {
                Type ctype = cfolder.fold2(opc, left, right);
                if (ctype != null) {
                    owntype = cfolder.coerce(ctype, owntype);

                    // Remove constant types from arguments to
                    // conserve space. The parser will fold concatenations
                    // of string literals; the code here also
                    // gets rid of intermediate results when some of the
                    // operands are constant identifiers.
                    if (tree.lhs.type.tsym == syms.stringType.tsym) {
                        tree.lhs.type = syms.stringType;
                    }
                    if (tree.rhs.type.tsym == syms.stringType.tsym) {
                        tree.rhs.type = syms.stringType;
                    }
                }
            }

            // Check that argument types of a reference ==, != are
            // castable to each other, (JLS???).
            if ((opc == ByteCodes.if_acmpeq || opc == ByteCodes.if_acmpne)) {
                if (!types.isCastable(left, right, new Warner(tree.pos()))) {
                    log.error(tree.pos(), "incomparable.types", left, right);
                }
            }

            chk.checkDivZero(tree.rhs.pos(), operator, right);
        }
        result = check(tree, owntype, VAL, pkind, pt);
        DEBUG.P(0,this,"visitBinary(1)");
    }