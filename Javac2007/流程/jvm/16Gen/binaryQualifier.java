    /** Construct a symbol to reflect the qualifying type that should
     *  appear in the byte code as per JLS 13.1.
     *
     *  For target >= 1.2: Clone a method with the qualifier as owner (except
     *  for those cases where we need to work around VM bugs).
     *
     *  For target <= 1.1: If qualified variable or method is defined in a
     *  non-accessible class, clone it with the qualifier class as owner.
     *
     *  @param sym    The accessed symbol
     *  @param site   The qualifier's type.
     */
    Symbol binaryQualifier(Symbol sym, Type site) {
		try {//我加上的
		DEBUG.P(this,"binaryQualifier(Symbol sym, Type site)");
		DEBUG.P("sym="+sym);
		DEBUG.P("site="+site+" site.tag="+TypeTags.toString(site.tag));

		if (site.tag == ARRAY) {
			if (sym == syms.lengthVar ||
			sym.owner != syms.arrayClass)
				return sym;
			// array clone can be qualified by the array type in later targets
			Symbol qualifier = target.arrayBinaryCompatibility()
			? new ClassSymbol(Flags.PUBLIC, site.tsym.name,
					  site, syms.noSymbol)
			: syms.objectType.tsym;
			return sym.clone(qualifier);
		}

		DEBUG.P("");
		DEBUG.P("sym.owner="+sym.owner);
		DEBUG.P("site.tsym="+site.tsym);
		DEBUG.P("sym.flags()="+Flags.toString(sym.flags()));

		if (sym.owner == site.tsym ||
			(sym.flags() & (STATIC | SYNTHETIC)) == (STATIC | SYNTHETIC)) {
			return sym;
		}

		DEBUG.P("");
		DEBUG.P("target.obeyBinaryCompatibility()="+target.obeyBinaryCompatibility());
		if (!target.obeyBinaryCompatibility())
			return rs.isAccessible(attrEnv, (TypeSymbol)sym.owner)
			? sym
			: sym.clone(site.tsym);

		DEBUG.P("");
		DEBUG.P("target.interfaceFieldsBinaryCompatibility()="+target.interfaceFieldsBinaryCompatibility());
		if (!target.interfaceFieldsBinaryCompatibility()) {
			if ((sym.owner.flags() & INTERFACE) != 0 && sym.kind == VAR)
			return sym;
		}

		// leave alone methods inherited from Object
		// JLS2 13.1.
		if (sym.owner == syms.objectType.tsym)
			return sym;

		DEBUG.P("");
		DEBUG.P("target.interfaceObjectOverridesBinaryCompatibility()="+target.interfaceObjectOverridesBinaryCompatibility());
		if (!target.interfaceObjectOverridesBinaryCompatibility()) {
			if ((sym.owner.flags() & INTERFACE) != 0 &&
			syms.objectType.tsym.members().lookup(sym.name).scope != null)
			return sym;
		}

		return sym.clone(site.tsym);
		
		}finally{//我加上的
		DEBUG.P(0,this,"binaryQualifier(Symbol sym, Type site)");
		}
    }