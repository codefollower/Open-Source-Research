    byte[] signatureBuffer = new byte[0];
    int sbp = 0;
    /** Convert class signature to type, where signature is implicit.
     */
    Type classSigToType() {
		try {//我加上的
		DEBUG.P(this,"classSigToType()");
		DEBUG.P("signature[sigp]="+(char)signature[sigp]);

        if (signature[sigp] != 'L')
            throw badClassFile("bad.class.signature",
                               Convert.utf2string(signature, sigp, 10));
        sigp++;
        Type outer = Type.noType;
        int startSbp = sbp;

        while (true) {
            final byte c = signature[sigp++];
			DEBUG.P("c="+(char)c);

            switch (c) {

            case ';': {         // end
                ClassSymbol t = enterClass(names.fromUtf(signatureBuffer,
                                                         startSbp,
                                                         sbp - startSbp));
				DEBUG.P("outer="+outer);
                if (outer == Type.noType)
                    outer = t.erasure(types);
                else
                    outer = new ClassType(outer, List.<Type>nil(), t);
                sbp = startSbp;
				DEBUG.P("outer="+outer);
                return outer;
            }

            case '<':           // generic arguments
                ClassSymbol t = enterClass(names.fromUtf(signatureBuffer,
                                                         startSbp,
                                                         sbp - startSbp));
                outer = new ClassType(outer, sigToTypes('>'), t) {
                        boolean completed = false;
                        public Type getEnclosingType() {
                            if (!completed) {
                                completed = true;
                                tsym.complete();
                                Type enclosingType = tsym.type.getEnclosingType();
                                if (enclosingType != Type.noType) {
                                    List<Type> typeArgs =
                                        super.getEnclosingType().allparams();
                                    List<Type> typeParams =
                                        enclosingType.allparams();
                                    if (typeParams.length() != typeArgs.length()) {
                                        // no "rare" types
                                        super.setEnclosingType(types.erasure(enclosingType));
                                    } else {
                                        super.setEnclosingType(types.subst(enclosingType,
                                                                           typeParams,
                                                                           typeArgs));
                                    }
                                } else {
                                    super.setEnclosingType(Type.noType);
                                }
                            }
                            return super.getEnclosingType();
                        }
                        public void setEnclosingType(Type outer) {
                            throw new UnsupportedOperationException();
                        }
                    };
				DEBUG.P("signature[sigp++]="+(char)signature[sigp+1]);
                switch (signature[sigp++]) {
                case ';':

					DEBUG.P("sigp="+sigp);
					DEBUG.P("signature.length="+signature.length);
					if(sigp < siglimit)
						DEBUG.P("signature[sigp]="+(char)signature[sigp]);
                    if (sigp < signature.length && signature[sigp] == '.') {
                        // support old-style GJC signatures
                        // The signature produced was
                        // Lfoo/Outer<Lfoo/X;>;.Lfoo/Outer$Inner<Lfoo/Y;>;
                        // rather than say
                        // Lfoo/Outer<Lfoo/X;>.Inner<Lfoo/Y;>;
                        // so we skip past ".Lfoo/Outer$"
                        sigp += (sbp - startSbp) + // "foo/Outer"
                            3;  // ".L" and "$"
                        signatureBuffer[sbp++] = (byte)'$';
                        break;
                    } else {
                        sbp = startSbp;
                        return outer;
                    }
                case '.':
                    signatureBuffer[sbp++] = (byte)'$';
                    break;
                default:
                    throw new AssertionError(signature[sigp-1]);
                }
                continue;

            case '.':
                signatureBuffer[sbp++] = (byte)'$';
                continue;
            case '/':
                signatureBuffer[sbp++] = (byte)'.';
                continue;
            default:
                signatureBuffer[sbp++] = c;
                continue;
            }
        }
        
        }finally{//我加上的
		DEBUG.P(0,this,"classSigToType(1)");
		}
    }