    /** Resolve error class indicating an ambiguous reference.
     */
    class AmbiguityError extends ResolveError {
        Symbol sym1;
        Symbol sym2;

        AmbiguityError(Symbol sym1, Symbol sym2) {
            super(AMBIGUOUS, sym1, "ambiguity error");
			DEBUG.P(this,"AmbiguityError(2)");
            DEBUG.P("sym1="+sym1);
			DEBUG.P("sym2="+sym2);

            this.sym1 = sym1;
            this.sym2 = sym2;

			DEBUG.P(1,this,"AmbiguityError(2)");
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

            AmbiguityError pair = this;
            while (true) {
                if (pair.sym1.kind == AMBIGUOUS)
                    pair = (AmbiguityError)pair.sym1;
                else if (pair.sym2.kind == AMBIGUOUS)
                    pair = (AmbiguityError)pair.sym2;
                else break;
            }
            Name sname = pair.sym1.name;
			DEBUG.P("sname="+sname);
            if (sname == sname.table.init) sname = pair.sym1.owner.name;
            log.error(pos, "ref.ambiguous", sname,
                      kindName(pair.sym1),
                      pair.sym1,
                      pair.sym1.location(site, types),
                      kindName(pair.sym2),
                      pair.sym2,
                      pair.sym2.location(site, types));

			}finally{//我加上的
			DEBUG.P(0,this,"report(6)");
			}
        }
    }