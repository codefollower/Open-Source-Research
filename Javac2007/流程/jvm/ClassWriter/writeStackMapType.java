        //where
        void writeStackMapType(Type t) {
            if (t == null) {
                if (debugstackmap) System.out.print("empty");
                databuf.appendByte(0);
            }
            else switch(t.tag) {
            case BYTE:
            case CHAR:
            case SHORT:
            case INT:
            case BOOLEAN:
                if (debugstackmap) System.out.print("int");
                databuf.appendByte(1);
                break;
            case FLOAT:
                if (debugstackmap) System.out.print("float");
                databuf.appendByte(2);
                break;
            case DOUBLE:
                if (debugstackmap) System.out.print("double");
                databuf.appendByte(3);
                break;
            case LONG:
                if (debugstackmap) System.out.print("long");
                databuf.appendByte(4);
                break;
            case BOT: // null
                if (debugstackmap) System.out.print("null");
                databuf.appendByte(5);
                break;
            case CLASS:
            case ARRAY:
                if (debugstackmap) System.out.print("object(" + t + ")");
                databuf.appendByte(7);
                databuf.appendChar(pool.put(t));
                break;
            case TYPEVAR:
                if (debugstackmap) System.out.print("object(" + types.erasure(t).tsym + ")");
                databuf.appendByte(7);
                databuf.appendChar(pool.put(types.erasure(t).tsym));
                break;
            case UNINITIALIZED_THIS:
                if (debugstackmap) System.out.print("uninit_this");
                databuf.appendByte(6);
                break;
            case UNINITIALIZED_OBJECT:
                { UninitializedType uninitType = (UninitializedType)t;
                databuf.appendByte(8);
                if (debugstackmap) System.out.print("uninit_object@" + uninitType.offset);
                databuf.appendChar(uninitType.offset);
                }
                break;
            default:
                throw new AssertionError();
            }
        }