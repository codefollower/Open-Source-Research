    void assembleSig(List<Type> types) {
        for (List<Type> ts = types; ts.nonEmpty(); ts = ts.tail)
            assembleSig(ts.head);
    }

	/** Assemble signature of given type in string buffer.
     */
    void assembleSig(Type type) {
		try {//我加上的
		DEBUG.P(this,"assembleSig(Type type)");
		DEBUG.P("type="+type+" type.tag="+TypeTags.toString(type.tag));

        switch (type.tag) {
        case BYTE:
            sigbuf.appendByte('B');
            break;
        case SHORT:
            sigbuf.appendByte('S');
            break;
        case CHAR:
            sigbuf.appendByte('C');
            break;
        case INT:
            sigbuf.appendByte('I');
            break;
        case LONG:
            sigbuf.appendByte('J');
            break;
        case FLOAT:
            sigbuf.appendByte('F');
            break;
        case DOUBLE:
            sigbuf.appendByte('D');
            break;
        case BOOLEAN:
            sigbuf.appendByte('Z');
            break;
        case VOID:
            sigbuf.appendByte('V');
            break;
        case CLASS:
            sigbuf.appendByte('L');
            assembleClassSig(type);
            sigbuf.appendByte(';');
            break;
        case ARRAY:
            ArrayType at = (ArrayType)type;
            sigbuf.appendByte('[');
            assembleSig(at.elemtype);
            break;
        case METHOD:
            MethodType mt = (MethodType)type;
            sigbuf.appendByte('(');
            assembleSig(mt.argtypes);
            sigbuf.appendByte(')');
            assembleSig(mt.restype);
            if (hasTypeVar(mt.thrown)) {
                for (List<Type> l = mt.thrown; l.nonEmpty(); l = l.tail) {
                    sigbuf.appendByte('^');
                    assembleSig(l.head);
                }
            }
            break;
        case WILDCARD: {
            WildcardType ta = (WildcardType) type;
            switch (ta.kind) {
            case SUPER:
                sigbuf.appendByte('-');
                assembleSig(ta.type);
                break;
            case EXTENDS:
                sigbuf.appendByte('+');
                assembleSig(ta.type);
                break;
            case UNBOUND:
                sigbuf.appendByte('*');
                break;
            default:
                throw new AssertionError(ta.kind);
            }
            break;
        }
        case TYPEVAR:
            sigbuf.appendByte('T');
            sigbuf.appendName(type.tsym.name);
            sigbuf.appendByte(';');
            break;
        case FORALL:
            ForAll ft = (ForAll)type;
            assembleParamsSig(ft.tvars);
            assembleSig(ft.qtype);
            break;
        case UNINITIALIZED_THIS:
        case UNINITIALIZED_OBJECT:
            // we don't yet have a spec for uninitialized types in the
            // local variable table
            assembleSig(types.erasure(((UninitializedType)type).qtype));
            break;
        default:
            throw new AssertionError("typeSig " + type.tag);
        }

		}finally{//我加上的
		DEBUG.P(0,this,"assembleSig(Type type)");
		}
    }