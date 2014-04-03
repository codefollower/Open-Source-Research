    Attribute readAttributeValue() {
        char c = (char) buf[bp++];
        switch (c) {
        case 'B':
            return new Attribute.Constant(syms.byteType, readPool(nextChar()));
        case 'C':
            return new Attribute.Constant(syms.charType, readPool(nextChar()));
        case 'D':
            return new Attribute.Constant(syms.doubleType, readPool(nextChar()));
        case 'F':
            return new Attribute.Constant(syms.floatType, readPool(nextChar()));
        case 'I':
            return new Attribute.Constant(syms.intType, readPool(nextChar()));
        case 'J':
            return new Attribute.Constant(syms.longType, readPool(nextChar()));
        case 'S':
            return new Attribute.Constant(syms.shortType, readPool(nextChar()));
        case 'Z':
            return new Attribute.Constant(syms.booleanType, readPool(nextChar()));
        case 's':
            return new Attribute.Constant(syms.stringType, readPool(nextChar()).toString());
        case 'e':
            return new EnumAttributeProxy(readEnumType(nextChar()), readName(nextChar()));
        case 'c':
            return new Attribute.Class(types, readTypeOrClassSymbol(nextChar()));
        case '[': {
            int n = nextChar();
            ListBuffer<Attribute> l = new ListBuffer<Attribute>();
            for (int i=0; i<n; i++)
                l.append(readAttributeValue());
            return new ArrayAttributeProxy(l.toList());
        }
        case '@':
            return readCompoundAnnotation();
        default:
            throw new AssertionError("unknown annotation tag '" + c + "'");
        }
    }
