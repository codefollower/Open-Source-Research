/************************************************************************
 * Reading Types
 ***********************************************************************/

    /** The unread portion of the currently read type is
     *  signature[sigp..siglimit-1].
     */
    byte[] signature;
    int sigp;
    int siglimit;
    boolean sigEnterPhase = false;

    /** Convert signature to type, where signature is a name.
     */
    Type sigToType(Name sig) {
		try {//我加上的
		DEBUG.P(this,"sigToType(1)");
		DEBUG.P("sig="+sig);

        return sig == null
            ? null
            : sigToType(sig.table.names, sig.index, sig.len);

		}finally{//我加上的
		DEBUG.P(0,this,"sigToType(1)");
		}
    }

    /** Convert signature to type, where signature is a byte array segment.
     */
    Type sigToType(byte[] sig, int offset, int len) {
		try {//我加上的
		DEBUG.P(this,"sigToType(3)");
		DEBUG.P("offset="+offset);
		DEBUG.P("len="+len);

        signature = sig;
        sigp = offset;
        siglimit = offset + len;
        return sigToType();

		}finally{//我加上的
		DEBUG.P(0,this,"sigToType(3)");
		}
    }

    /** Convert signature to type, where signature is implicit.
     */
    Type sigToType() {
		try {//我加上的
		DEBUG.P(this,"sigToType()");
		DEBUG.P("signature[sigp]="+(char)signature[sigp]);

        switch ((char) signature[sigp]) {
        case 'T':
            sigp++;
            int start = sigp;
			DEBUG.P("signature[sigp]="+(char)signature[sigp]);
            while (signature[sigp] != ';') sigp++;
            sigp++;
			DEBUG.P("sigEnterPhase="+sigEnterPhase);
            return sigEnterPhase
                ? Type.noType
                : findTypeVar(names.fromUtf(signature, start, sigp - 1 - start));
        case '+': {
            sigp++;
            Type t = sigToType();
            return new WildcardType(t, BoundKind.EXTENDS,
                                    syms.boundClass);
        }
        case '*':
            sigp++;
            return new WildcardType(syms.objectType, BoundKind.UNBOUND,
                                    syms.boundClass);
        case '-': {
            sigp++;
            Type t = sigToType();
            return new WildcardType(t, BoundKind.SUPER,
                                    syms.boundClass);
        }
        case 'B':
            sigp++;
            return syms.byteType;
        case 'C':
            sigp++;
            return syms.charType;
        case 'D':
            sigp++;
            return syms.doubleType;
        case 'F':
            sigp++;
            return syms.floatType;
        case 'I':
            sigp++;
            return syms.intType;
        case 'J':
            sigp++;
            return syms.longType;
        case 'L':
            {
                // int oldsigp = sigp;
                Type t = classSigToType();
				DEBUG.P("sigp="+sigp);
				DEBUG.P("siglimit="+siglimit);
				if(sigp < siglimit)
					DEBUG.P("signature[sigp]="+(char)signature[sigp]);

                if (sigp < siglimit && signature[sigp] == '.')
                    throw badClassFile("deprecated inner class signature syntax " +
                                       "(please recompile from source)");
                /*
                System.err.println(" decoded " +
                                   new String(signature, oldsigp, sigp-oldsigp) +
                                   " => " + t + " outer " + t.outer());
                */
                return t;
            }
        case 'S':
            sigp++;
            return syms.shortType;
        case 'V':
            sigp++;
            return syms.voidType;
        case 'Z':
            sigp++;
            return syms.booleanType;
        case '[':
            sigp++;
            return new ArrayType(sigToType(), syms.arrayClass);
        case '(':
            sigp++;
            List<Type> argtypes = sigToTypes(')');
            Type restype = sigToType();
            List<Type> thrown = List.nil();

			DEBUG.P("signature[sigp]="+(char)signature[sigp]);
            while (signature[sigp] == '^') {
                sigp++;
                thrown = thrown.prepend(sigToType());
            }
            return new MethodType(argtypes,
                                  restype,
                                  thrown.reverse(),
                                  syms.methodClass);
        case '<':
			DEBUG.P("typevars="+typevars);
            typevars = typevars.dup(currentOwner);
            Type poly = new ForAll(sigToTypeParams(), sigToType());
            typevars = typevars.leave();
			DEBUG.P("typevars="+typevars);
            return poly;
        default:
            throw badClassFile("bad.signature",
                               Convert.utf2string(signature, sigp, 10));
        }

		}finally{//我加上的
		DEBUG.P(0,this,"sigToType()");
		}
    }