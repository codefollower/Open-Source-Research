    public static class DelegatedSymbol extends Symbol {
        protected Symbol other;
        public DelegatedSymbol(Symbol other) {
            super(other.kind, other.flags_field, other.name, other.type, other.owner);
            this.other = other;
        }
        public String toString() { return other.toString(); }
        public String location() { return other.location(); }
        public String location(Type site, Types types) { return other.location(site, types); }
        public Type erasure(Types types) { return other.erasure(types); }
        public Type externalType(Types types) { return other.externalType(types); }
        public boolean isLocal() { return other.isLocal(); }
        public boolean isConstructor() { return other.isConstructor(); }
        public Name getQualifiedName() { return other.getQualifiedName(); }
        public Name flatName() { return other.flatName(); }
        public Scope members() { return other.members(); }
        public boolean isInner() { return other.isInner(); }
        public boolean hasOuterInstance() { return other.hasOuterInstance(); }
        public ClassSymbol enclClass() { return other.enclClass(); }
        public ClassSymbol outermostClass() { return other.outermostClass(); }
        public PackageSymbol packge() { return other.packge(); }
        public boolean isSubClass(Symbol base, Types types) { return other.isSubClass(base, types); }
        public boolean isMemberOf(TypeSymbol clazz, Types types) { return other.isMemberOf(clazz, types); }
        public boolean isEnclosedBy(ClassSymbol clazz) { return other.isEnclosedBy(clazz); }
        public boolean isInheritedIn(Symbol clazz, Types types) { return other.isInheritedIn(clazz, types); }
        public Symbol asMemberOf(Type site, Types types) { return other.asMemberOf(site, types); }
        public void complete() throws CompletionFailure { other.complete(); }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return other.accept(v, p);
        }
    }