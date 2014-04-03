    /** A class for variable symbols
     */
    public static class VarSymbol extends Symbol implements VariableElement {

        /** The variable's declaration position.
         */
        public int pos = Position.NOPOS;

        /** The variable's address. Used for different purposes during
         *  flow analysis, translation and code generation.
         *  Flow analysis:
         *    If this is a blank final or local variable, its sequence number.
         *  Translation:
         *    If this is a private field, its access number.
         *  Code generation:
         *    If this is a local variable, its logical slot number.
         */
        public int adr = -1;

        /** Construct a variable symbol, given its flags, name, type and owner.
         */
        public VarSymbol(long flags, Name name, Type type, Symbol owner) {
            super(VAR, flags, name, type, owner);
        }

        /** Clone this symbol with new owner.
         */
        public VarSymbol clone(Symbol newOwner) {
            VarSymbol v = new VarSymbol(flags_field, name, type, newOwner);
            v.pos = pos;
            v.adr = adr;
            v.data = data;
//          System.out.println("clone " + v + " in " + newOwner);//DEBUG
            return v;
        }

        public String toString() {
            return name.toString();
        }

        public Symbol asMemberOf(Type site, Types types) {
            return new VarSymbol(flags_field, name, types.memberType(site, this), owner);
        }

        public ElementKind getKind() {
            long flags = flags();
            if ((flags & PARAMETER) != 0) {
                if (isExceptionParameter())
                    return ElementKind.EXCEPTION_PARAMETER;
                else
                    return ElementKind.PARAMETER;
            } else if ((flags & ENUM) != 0) {
                return ElementKind.ENUM_CONSTANT;
            } else if (owner.kind == TYP || owner.kind == ERR) {
                return ElementKind.FIELD;
            } else {
                return ElementKind.LOCAL_VARIABLE;
            }
        }

        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            return v.visitVariable(this, p);
        }

        public Object getConstantValue() { // Mirror API
        	//com.sun.tools.javac.util.Constants
            return Constants.decode(getConstValue(), type);
        }

        public void setLazyConstValue(final Env<AttrContext> env,
                                      final Log log,
                                      final Attr attr,
                                      final JCTree.JCExpression initializer)
        {
            setData(new Callable<Object>() {
                public Object call() {
                    JavaFileObject source = log.useSource(env.toplevel.sourcefile);
                    try {
                        // In order to catch self-references, we set
                        // the variable's declaration position to
                        // maximal possible value, effectively marking
                        // the variable as undefined.
                        int pos = VarSymbol.this.pos;
                        VarSymbol.this.pos = Position.MAXPOS;
                        Type itype = attr.attribExpr(initializer, env, type);
                        VarSymbol.this.pos = pos;
                        if (itype.constValue() != null)
                            return attr.coerce(itype, type).constValue();
                        else
                            return null;
                    } finally {
                        log.useSource(source);
                    }
                }
            });
        }

        /**
         * The variable's constant value, if this is a constant.
         * Before the constant value is evaluated, it points to an
         * initalizer environment.  If this is not a constant, it can
         * be used for other stuff.
         */
        private Object data;

        public boolean isExceptionParameter() {
            return data == ElementKind.EXCEPTION_PARAMETER;
        }

        public Object getConstValue() {
            // TODO: Consider if getConstValue and getConstantValue can be collapsed
            if (data == ElementKind.EXCEPTION_PARAMETER) {
                return null;
            } else if (data instanceof Callable<?>) {
                // In this case, this is final a variable, with an as
                // yet unevaluated initializer.
                
                //是指java.util.concurrent.Callable<V>
                //javax.tools.JavaCompiler.CompilationTask是它的子接口
                Callable<?> eval = (Callable<?>)data;
                data = null; // to make sure we don't evaluate this twice.
                try {
                    data = eval.call();
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            }
            return data;
        }

        public void setData(Object data) {
            assert !(data instanceof Env<?>) : this;
            this.data = data;
        }
    }