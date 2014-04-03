    /** A visitor to write an attribute including its leading
     *  single-character marker.
     */
    class AttributeWriter implements Attribute.Visitor {
        public void visitConstant(Attribute.Constant _value) {
			DEBUG.P(this,"visitConstant(1)");
			DEBUG.P("_value="+_value);
			DEBUG.P("_value.type.tag="+TypeTags.toString(_value.type.tag));
		
            Object value = _value.value;
            switch (_value.type.tag) {
            case BYTE:
                databuf.appendByte('B');
                break;
            case CHAR:
                databuf.appendByte('C');
                break;
            case SHORT:
                databuf.appendByte('S');
                break;
            case INT:
                databuf.appendByte('I');
                break;
            case LONG:
                databuf.appendByte('J');
                break;
            case FLOAT:
                databuf.appendByte('F');
                break;
            case DOUBLE:
                databuf.appendByte('D');
                break;
            case BOOLEAN:
                databuf.appendByte('Z');
                break;
            case CLASS:
                assert value instanceof String;
                databuf.appendByte('s');
                value = names.fromString(value.toString()); // CONSTANT_Utf8
                break;
            default:
                throw new AssertionError(_value.type);
            }
            databuf.appendChar(pool.put(value));

			DEBUG.P(0,this,"visitConstant(1)");
        }
        public void visitEnum(Attribute.Enum e) {
			DEBUG.P(this,"visitEnum(1)");
            databuf.appendByte('e');
            databuf.appendChar(pool.put(typeSig(e.value.type)));
            databuf.appendChar(pool.put(e.value.name));
			DEBUG.P(0,this,"visitEnum(1)");
        }
        public void visitClass(Attribute.Class clazz) {
			DEBUG.P(this,"visitClass(1)");
            databuf.appendByte('c');
            databuf.appendChar(pool.put(typeSig(clazz.type)));
			DEBUG.P(0,this,"visitClass(1)");
        }
        public void visitCompound(Attribute.Compound compound) {
			DEBUG.P(this,"visitCompound(1)");
            databuf.appendByte('@');
            writeCompoundAttribute(compound);
			DEBUG.P(0,this,"visitCompound(1)");
        }
        public void visitError(Attribute.Error x) {
            throw new AssertionError(x);
        }
        public void visitArray(Attribute.Array array) {
			DEBUG.P(this,"visitArray(1)");
            databuf.appendByte('[');
            databuf.appendChar(array.values.length);
            for (Attribute a : array.values) {
                a.accept(this);
            }
			DEBUG.P(0,this,"visitArray(1)");
        }
    }