    class AnnotationDeproxy implements ProxyVisitor {
        private ClassSymbol requestingOwner = currentOwner.kind == MTH
            ? currentOwner.enclClass() : (ClassSymbol)currentOwner;

        List<Attribute.Compound> deproxyCompoundList(List<CompoundAnnotationProxy> pl) {
            // also must fill in types!!!!
            ListBuffer<Attribute.Compound> buf =
                new ListBuffer<Attribute.Compound>();
            for (List<CompoundAnnotationProxy> l = pl; l.nonEmpty(); l=l.tail) {
                buf.append(deproxyCompound(l.head));
            }
            return buf.toList();
        }

        Attribute.Compound deproxyCompound(CompoundAnnotationProxy a) {
            ListBuffer<Pair<Symbol.MethodSymbol,Attribute>> buf =
                new ListBuffer<Pair<Symbol.MethodSymbol,Attribute>>();
            for (List<Pair<Name,Attribute>> l = a.values;
                 l.nonEmpty();
                 l = l.tail) {
                MethodSymbol meth = findAccessMethod(a.type, l.head.fst);
                buf.append(new Pair<Symbol.MethodSymbol,Attribute>
                           (meth, deproxy(meth.type.getReturnType(), l.head.snd)));
            }
            return new Attribute.Compound(a.type, buf.toList());
        }

        MethodSymbol findAccessMethod(Type container, Name name) {
            CompletionFailure failure = null;
            try {
                for (Scope.Entry e = container.tsym.members().lookup(name);
                     e.scope != null;
                     e = e.next()) {
                    Symbol sym = e.sym;
                    if (sym.kind == MTH && sym.type.getParameterTypes().length() == 0)
                        return (MethodSymbol) sym;
                }
            } catch (CompletionFailure ex) {
                failure = ex;
            }
            // The method wasn't found: emit a warning and recover
            JavaFileObject prevSource = log.useSource(requestingOwner.classfile);
            try {
                if (failure == null) {
                    log.warning("annotation.method.not.found",
                                container,
                                name);
                } else {
                    log.warning("annotation.method.not.found.reason",
                                container,
                                name,
                                failure.getMessage());
                }
            } finally {
                log.useSource(prevSource);
            }
            // Construct a new method type and symbol.  Use bottom
            // type (typeof null) as return type because this type is
            // a subtype of all reference types and can be converted
            // to primitive types by unboxing.
            MethodType mt = new MethodType(List.<Type>nil(),
                                           syms.botType,
                                           List.<Type>nil(),
                                           syms.methodClass);
            return new MethodSymbol(PUBLIC | ABSTRACT, name, mt, container.tsym);
        }

        Attribute result;
        Type type;
        Attribute deproxy(Type t, Attribute a) {
            Type oldType = type;
            try {
                type = t;
                a.accept(this);
                return result;
            } finally {
                type = oldType;
            }
        }

        // implement Attribute.Visitor below

        public void visitConstant(Attribute.Constant value) {
            // assert value.type == type;
            result = value;
        }

        public void visitClass(Attribute.Class clazz) {
            result = clazz;
        }

        public void visitEnum(Attribute.Enum e) {
            throw new AssertionError(); // shouldn't happen
        }

        public void visitCompound(Attribute.Compound compound) {
            throw new AssertionError(); // shouldn't happen
        }

        public void visitArray(Attribute.Array array) {
            throw new AssertionError(); // shouldn't happen
        }

        public void visitError(Attribute.Error e) {
            throw new AssertionError(); // shouldn't happen
        }

        public void visitEnumAttributeProxy(EnumAttributeProxy proxy) {
            // type.tsym.flatName() should == proxy.enumFlatName
            TypeSymbol enumTypeSym = proxy.enumType.tsym;
            VarSymbol enumerator = null;
            for (Scope.Entry e = enumTypeSym.members().lookup(proxy.enumerator);
                 e.scope != null;
                 e = e.next()) {
                if (e.sym.kind == VAR) {
                    enumerator = (VarSymbol)e.sym;
                    break;
                }
            }
            if (enumerator == null) {
                log.error("unknown.enum.constant",
                          currentClassFile, enumTypeSym, proxy.enumerator);
                result = new Attribute.Error(enumTypeSym.type);
            } else {
                result = new Attribute.Enum(enumTypeSym.type, enumerator);
            }
        }

        public void visitArrayAttributeProxy(ArrayAttributeProxy proxy) {
            int length = proxy.values.length();
            Attribute[] ats = new Attribute[length];
            Type elemtype = types.elemtype(type);
            int i = 0;
            for (List<Attribute> p = proxy.values; p.nonEmpty(); p = p.tail) {
                ats[i++] = deproxy(elemtype, p.head);
            }
            result = new Attribute.Array(type, ats);
        }

        public void visitCompoundAnnotationProxy(CompoundAnnotationProxy proxy) {
            result = deproxyCompound(proxy);
        }
    }