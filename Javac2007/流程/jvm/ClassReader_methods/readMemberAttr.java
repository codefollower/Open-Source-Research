    /** Read member attribute.
     */
    void readMemberAttr(Symbol sym, Name attrName, int attrLen) {
		DEBUG.P(this,"readMemberAttr(3)");
		DEBUG.P("sym="+sym);
		DEBUG.P("attrName="+attrName);
		DEBUG.P("attrLen="+attrLen);

        //- System.err.println(" z " + sym + ", " + attrName + ", " + attrLen);
        if (attrName == names.ConstantValue) {
            Object v = readPool(nextChar());
            // Ignore ConstantValue attribute if field not final.
            if ((sym.flags() & FINAL) != 0)
                ((VarSymbol)sym).setData(v);
        } else if (attrName == names.Code) {
            if (readAllOfClassFile || saveParameterNames)
                ((MethodSymbol)sym).code = readCode(sym);
            else
                bp = bp + attrLen;
        } else if (attrName == names.Exceptions) {
            int nexceptions = nextChar();
            List<Type> thrown = List.nil();
            for (int j = 0; j < nexceptions; j++)
                thrown = thrown.prepend(readClassSymbol(nextChar()).type);
            if (sym.type.getThrownTypes().isEmpty())
                sym.type.asMethodType().thrown = thrown.reverse();
        } else if (attrName == names.Synthetic) {
            // bridge methods are visible when generics not enabled
            if (allowGenerics || (sym.flags_field & BRIDGE) == 0)
                sym.flags_field |= SYNTHETIC;
        } else if (attrName == names.Bridge) {
            sym.flags_field |= BRIDGE;
            if (!allowGenerics)
                sym.flags_field &= ~SYNTHETIC;
        } else if (attrName == names.Deprecated) {
            sym.flags_field |= DEPRECATED;
        } else if (attrName == names.Varargs) {
            if (allowVarargs) sym.flags_field |= VARARGS;
        } else if (attrName == names.Annotation) {
            if (allowAnnotations) sym.flags_field |= ANNOTATION;
        } else if (attrName == names.Enum) {
            sym.flags_field |= ENUM;
        } else if (allowGenerics && attrName == names.Signature) {
            List<Type> thrown = sym.type.getThrownTypes();
            sym.type = readType(nextChar());
            //- System.err.println(" # " + sym.type);
            if (sym.kind == MTH && sym.type.getThrownTypes().isEmpty())
                sym.type.asMethodType().thrown = thrown;
        } else if (attrName == names.RuntimeVisibleAnnotations) {
            attachAnnotations(sym);
        } else if (attrName == names.RuntimeInvisibleAnnotations) {
            attachAnnotations(sym);
        } else if (attrName == names.RuntimeVisibleParameterAnnotations) {
            attachParameterAnnotations(sym);
        } else if (attrName == names.RuntimeInvisibleParameterAnnotations) {
            attachParameterAnnotations(sym);
        } else if (attrName == names.LocalVariableTable) {
            int newbp = bp + attrLen;
            if (saveParameterNames) {
                // pick up parameter names from the variable table
                List<Name> parameterNames = List.nil();
                int firstParam = ((sym.flags() & STATIC) == 0) ? 1 : 0;
                int endParam = firstParam + Code.width(sym.type.getParameterTypes());
                int numEntries = nextChar();
                for (int i=0; i<numEntries; i++) {
                    int start_pc = nextChar();
                    int length = nextChar();
                    int nameIndex = nextChar();
                    int sigIndex = nextChar();
                    int register = nextChar();
                    if (start_pc == 0 &&
                        firstParam <= register &&
                        register < endParam) {
                        int index = firstParam;
                        for (Type t : sym.type.getParameterTypes()) {
                            if (index == register) {
                                parameterNames = parameterNames.prepend(readName(nameIndex));
                                break;
                            }
                            index += Code.width(t);
                        }
                    }
                }
                parameterNames = parameterNames.reverse();
                ((MethodSymbol)sym).savedParameterNames = parameterNames;
            }
            bp = newbp;
        } else if (attrName == names.AnnotationDefault) {
            attachAnnotationDefault(sym);
        } else if (attrName == names.EnclosingMethod) {
            int newbp = bp + attrLen;
            readEnclosingMethodAttr(sym);
            bp = newbp;
        } else {
            unrecognized(attrName);
            bp = bp + attrLen;
        }

		DEBUG.P(0,this,"readMemberAttr(3)");
    }