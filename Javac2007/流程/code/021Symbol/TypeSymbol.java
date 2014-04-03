    /** A class for type symbols. Type variables are represented by instances
     *  of this class, classes and packages by instances of subclasses.
     */
    public static class TypeSymbol
            extends Symbol implements TypeParameterElement {
        // Implements TypeParameterElement because type parameters don't
        // have their own TypeSymbol subclass.
        // TODO: type parameters should have their own TypeSymbol subclass

        public TypeSymbol(long flags, Name name, Type type, Symbol owner) {
            super(TYP, flags, name, type, owner);
        }

        /** form a fully qualified name from a name and an owner
         */
        static public Name formFullName(Name name, Symbol owner) {
            if (owner == null) return name;
            if (((owner.kind != ERR)) &&
                ((owner.kind & (VAR | MTH)) != 0
                 || (owner.kind == TYP && owner.type.tag == TYPEVAR)
                 )) return name;
            Name prefix = owner.getQualifiedName();
            if (prefix == null || prefix == prefix.table.empty)
                return name;
            else return prefix.append('.', name);
        }

        /** form a fully qualified name from a name and an owner, after
         *  converting to flat representation
         */
        static public Name formFlatName(Name name, Symbol owner) {
            if (owner == null ||
                (owner.kind & (VAR | MTH)) != 0
                || (owner.kind == TYP && owner.type.tag == TYPEVAR)
                ) return name;
            char sep = owner.kind == TYP ? '$' : '.';
            Name prefix = owner.flatName();
            if (prefix == null || prefix == prefix.table.empty)
                return name;
            else return prefix.append(sep, name);
        }

        /**
         * A total ordering between type symbols that refines the
         * class inheritance graph.
         *
         * Typevariables always precede other kinds of symbols.
         */
        public final boolean precedes(TypeSymbol that, Types types) {
            if (this == that)
                return false;
            if (this.type.tag == that.type.tag) {
                if (this.type.tag == CLASS) {
                    return
                        types.rank(that.type) < types.rank(this.type) ||
                        types.rank(that.type) == types.rank(this.type) &&
                        that.getQualifiedName().compareTo(this.getQualifiedName()) < 0;
                } else if (this.type.tag == TYPEVAR) {
                    return types.isSubtype(this.type, that.type);
                }
            }
            return this.type.tag == TYPEVAR;
        }

        // For type params; overridden in subclasses.
        public ElementKind getKind() {
            return ElementKind.TYPE_PARAMETER;
        }

        public java.util.List<Symbol> getEnclosedElements() {
            List<Symbol> list = List.nil();
            for (Scope.Entry e = members().elems; e != null; e = e.sibling) {
                if (e.sym != null && (e.sym.flags() & SYNTHETIC) == 0 && e.sym.owner == this)
                    list = list.prepend(e.sym);
            }
            return list;
        }

        // For type params.
        // Perhaps not needed if getEnclosingElement can be spec'ed
        // to do the same thing.
        // TODO: getGenericElement() might not be needed
        public Symbol getGenericElement() {
            return owner;
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            assert type.tag == TYPEVAR; // else override will be invoked
            return v.visitTypeParameter(this, p);
        }

        public List<Type> getBounds() {
            TypeVar t = (TypeVar)type;
            Type bound = t.getUpperBound();
            if (!bound.isCompound())
                return List.of(bound);
            ClassType ct = (ClassType)bound;
            if (!ct.tsym.erasure_field.isInterface()) {
                return ct.interfaces_field.prepend(ct.supertype_field);
            } else {
                // No superclass was given in bounds.
                // In this case, supertype is Object, erasure is first interface.
                return ct.interfaces_field;
            }
        }
    }