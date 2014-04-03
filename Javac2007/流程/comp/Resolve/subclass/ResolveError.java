    /** Root class for resolve errors.
     *  Instances of this class indicate "Symbol not found".
     *  Instances of subclass indicate other errors.
     */
    private class ResolveError extends Symbol {

        ResolveError(int kind, Symbol sym, String debugName) {
            super(kind, 0, null, null, null);
            this.debugName = debugName;
            this.sym = sym;
        }

        /** The name of the kind of error, for debugging only.
         */
        final String debugName;

        /** The symbol that was determined by resolution, or errSymbol if none
         *  was found.
         */
        final Symbol sym;

        /** The symbol that was a close mismatch, or null if none was found.
         *  wrongSym is currently set if a simgle method with the correct name, but
         *  the wrong parameters was found.
         */
        Symbol wrongSym;

        /** An auxiliary explanation set in case of instantiation errors.
         */
        JCDiagnostic explanation;


        public <R, P> R accept(ElementVisitor<R, P> v, P p) {
            throw new AssertionError();
        }

        /** Print the (debug only) name of the kind of error.
         */
        public String toString() {
            return debugName + " wrongSym=" + wrongSym + " explanation=" + explanation;
        }

        /** Update wrongSym and explanation and return this.
         */
        ResolveError setWrongSym(Symbol sym, JCDiagnostic explanation) {
            this.wrongSym = sym;
            this.explanation = explanation;
            return this;
        }

        /** Update wrongSym and return this.
         */
        ResolveError setWrongSym(Symbol sym) {
            this.wrongSym = sym;
            this.explanation = null;
            return this;
        }

        public boolean exists() {
            switch (kind) {
            case HIDDEN:
            case ABSENT_VAR:
            case ABSENT_MTH:
            case ABSENT_TYP:
                return false;
            default:
                return true;
            }
        }

        /** Report error.
         *  @param log       The error log to be used for error reporting.
         *  @param pos       The position to be used for error reporting.
         *  @param site      The original type from where the selection took place.
         *  @param name      The name of the symbol to be resolved.
         *  @param argtypes  The invocation's value arguments,
         *                   if we looked for a method.
         *  @param typeargtypes  The invocation's type arguments,
         *                   if we looked for a method.
         */
        void report(Log log, DiagnosticPosition pos, Type site, Name name,
                    List<Type> argtypes, List<Type> typeargtypes) {
			try {//我加上的
            DEBUG.P(this,"report(6)");
            DEBUG.P("site="+site);
            DEBUG.P("name="+name);
            DEBUG.P("kind="+Kinds.toString(kind));
			DEBUG.P("argtypes="+argtypes);
			DEBUG.P("typeargtypes="+typeargtypes);

            if (name != name.table.error) {
                JCDiagnostic kindname = absentKindName(kind);
                String idname = name.toString();
                String args = "";
                String typeargs = "";
                if (kind >= WRONG_MTHS && kind <= ABSENT_MTH) {
                    if (isOperator(name)) {
                        log.error(pos, "operator.cant.be.applied",
                                  name, Type.toString(argtypes));
                        return;
                    }
                    if (name == name.table.init) {
                        kindname = JCDiagnostic.fragment("kindname.constructor");
                        idname = site.tsym.name.toString();
                    }
                    args = "(" + Type.toString(argtypes) + ")";
                    if (typeargtypes != null && typeargtypes.nonEmpty())
                        typeargs = "<" + Type.toString(typeargtypes) + ">";

					DEBUG.P("argtypes="+argtypes);
					DEBUG.P("typeargtypes="+typeargtypes);
                }
                if (kind == WRONG_MTH) {
                    log.error(pos,
                              "cant.apply.symbol" + (explanation != null ? ".1" : ""),
                              wrongSym.asMemberOf(site, types),
                              wrongSym.location(site, types),
                              typeargs,
                              Type.toString(argtypes),
                              explanation);
                } else if (site.tsym.name.len != 0) {
					DEBUG.P("site.tsym.kind="+Kinds.toString(site.tsym.kind));
                    if (site.tsym.kind == PCK && !site.tsym.exists())
                        log.error(pos, "doesnt.exist", site.tsym);
                    else
                        log.error(pos, "cant.resolve.location",
                                  kindname, idname, args, typeargs,
                                  typeKindName(site), site);
                } else {
                    log.error(pos, "cant.resolve", kindname, idname, args, typeargs);
                }
            }

			}finally{//我加上的
			DEBUG.P(0,this,"report(6)");
			}
        }
//where
            /** A name designates an operator if it consists
             *  of a non-empty sequence of operator symbols +-~!/*%&|^<>=
             */
            boolean isOperator(Name name) {
                int i = 0;
                while (i < name.len &&
                       "+-~!*/%&|^<>=".indexOf(name.byteAt(i)) >= 0) i++;
                return i > 0 && i == name.len;
            }
    }