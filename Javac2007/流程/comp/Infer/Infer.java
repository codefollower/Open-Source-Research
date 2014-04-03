public class Infer {
	private static my.Debug DEBUG=new my.Debug(my.Debug.Attr);//我加上的

    protected static final Context.Key<Infer> inferKey =
	new Context.Key<Infer>();

    /** A value for prototypes that admit any type, including polymorphic ones. */
    public static final Type anyPoly = new Type(NONE, null);

    Symtab syms;
    Types types;

    public static Infer instance(Context context) {
		Infer instance = context.get(inferKey);
		if (instance == null)
			instance = new Infer(context);
		return instance;
    }

    protected Infer(Context context) {
		context.put(inferKey, this);
		syms = Symtab.instance(context);
		types = Types.instance(context);
    }

    public static class NoInstanceException extends RuntimeException {
		private static final long serialVersionUID = 0;

		boolean isAmbiguous; // exist several incomparable best instances?

		JCDiagnostic diagnostic;

		NoInstanceException(boolean isAmbiguous) {
			this.diagnostic = null;
			this.isAmbiguous = isAmbiguous;
		}
		NoInstanceException setMessage(String key) {
			this.diagnostic = JCDiagnostic.fragment(key);
			return this;
		}
		NoInstanceException setMessage(String key, Object arg1) {
			this.diagnostic = JCDiagnostic.fragment(key, arg1);
			return this;
		}
		NoInstanceException setMessage(String key, Object arg1, Object arg2) {
			this.diagnostic = JCDiagnostic.fragment(key, arg1, arg2);
			return this;
		}
		NoInstanceException setMessage(String key, Object arg1, Object arg2, Object arg3) {
			this.diagnostic = JCDiagnostic.fragment(key, arg1, arg2, arg3);
			return this;
		}
		public JCDiagnostic getDiagnostic() {
			return diagnostic;
		}
    }
    private final NoInstanceException ambiguousNoInstanceException =
		new NoInstanceException(true);
    private final NoInstanceException unambiguousNoInstanceException =
		new NoInstanceException(false);

/***************************************************************************
 * Auxiliary type values and classes
 ***************************************************************************/

    /** A mapping that turns type variables into undetermined type variables.
     */
    Mapping fromTypeVarFun = new Mapping("fromTypeVarFun") {
	    public Type apply(Type t) {
			if (t.tag == TYPEVAR) return new UndetVar(t);
			else return t.map(this);
	    }
	};

    /** A mapping that returns its type argument with every UndetVar replaced
     *  by its `inst' field. Throws a NoInstanceException
     *  if this not possible because an `inst' field is null.
     */
    Mapping getInstFun = new Mapping("getInstFun") {
	    public Type apply(Type t) {
			switch (t.tag) {
			case UNKNOWN:
				throw ambiguousNoInstanceException
				.setMessage("undetermined.type");
			case UNDETVAR:
				UndetVar that = (UndetVar) t;
				if (that.inst == null)
				throw ambiguousNoInstanceException
					.setMessage("type.variable.has.undetermined.type",
						that.qtype);
				return apply(that.inst);
			default:
				return t.map(this);
			}
	    }
	};