    /** Initialize classes and packages, treating this as the definitive classreader. */
    public void init(Symtab syms) {
    	DEBUG.P(this,"init(1)");
        init(syms, true);
        DEBUG.P(1,this,"init(1)");
    }

    /** Initialize classes and packages, optionally treating this as
     *  the definitive classreader.
     */
    private void init(Symtab syms, boolean definitive) {
        if (classes != null) return;

        if (definitive) {
            assert packages == null || packages == syms.packages;
            packages = syms.packages;
            assert classes == null || classes == syms.classes;
            classes = syms.classes;
        } else {
            packages = new HashMap<Name, PackageSymbol>();
            classes = new HashMap<Name, ClassSymbol>();
        }

        packages.put(names.empty, syms.rootPackage);
        syms.rootPackage.completer = this;
        syms.unnamedPackage.completer = this;
        
        DEBUG.P("将<names.empty, syms.rootPackage>加进Map<Name, PackageSymbol> packages");
        DEBUG.P("将rootPackage unnamedPackage的Symbol.Completer置为ClassReader");
    }
