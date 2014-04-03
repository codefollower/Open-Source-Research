    public static class ErrorType extends ClassType
            implements javax.lang.model.type.ErrorType {

        public ErrorType() {
            super(noType, List.<Type>nil(), null);
            tag = ERROR;
        }

        public ErrorType(ClassSymbol c) {
            this();
            tsym = c;
            c.type = this;
            c.kind = ERR;
            c.members_field = new Scope.ErrorScope(c);
        }

        public ErrorType(Name name, TypeSymbol container) {
            this(new ClassSymbol(PUBLIC|STATIC|ACYCLIC, name, null, container));
        }

        @Override
        public <R,S> R accept(Type.Visitor<R,S> v, S s) {
            return v.visitErrorType(this, s);
        }

        public Type constType(Object constValue) { return this; }
        public Type getEnclosingType()          { return this; }
        public Type getReturnType()              { return this; }
        public Type asSub(Symbol sym)            { return this; }
        public Type map(Mapping f)               { return this; }

        public boolean isGenType(Type t)         { return true; }
        public boolean isErroneous()             { return true; }
        public boolean isCompound()              { return false; }
        public boolean isInterface()             { return false; }

        public List<Type> allparams()            { return List.nil(); }
        public List<Type> getTypeArguments()     { return List.nil(); }

        public TypeKind getKind() {
            return TypeKind.ERROR;
        }

        public <R, P> R accept(TypeVisitor<R, P> v, P p) {
            return v.visitError(this, p);
        }
    }