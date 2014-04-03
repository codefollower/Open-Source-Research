    /** Find an identifier in a package which matches a specified kind set.
     *  @param env       The current environment.
     *  @param name      The identifier's name.
     *  @param kind      Indicates the possible symbol kinds
     *                   (a nonempty subset of TYP, PCK).
     */
    Symbol findIdentInPackage(Env<AttrContext> env, TypeSymbol pck,
                              Name name, int kind) {
        try {//我加上的
        DEBUG.P(this,"findIdentInPackage(4)");
        DEBUG.P("env="+env);
        DEBUG.P("pck="+pck); 
        DEBUG.P("name="+name);   
        DEBUG.P("kind="+Kinds.toString(kind));
			                      	
        Name fullname = TypeSymbol.formFullName(name, pck);
        DEBUG.P("fullname="+fullname);   
        Symbol bestSoFar = typeNotFound;
        PackageSymbol pack = null;
        if ((kind & PCK) != 0) {
            pack = reader.enterPackage(fullname);
            DEBUG.P("pack.exists()="+pack.exists());
            if (pack.exists()) return pack;
        }
        if ((kind & TYP) != 0) {
            Symbol sym = loadClass(env, fullname);
            DEBUG.P("sym.exists()="+sym.exists());
            DEBUG.P("name="+name);
            DEBUG.P("sym.name="+sym.name);
            if (sym.exists()) {
                // don't allow programs to use flatnames
                if (name == sym.name) return sym;
            }
            else if (sym.kind < bestSoFar.kind) bestSoFar = sym;
        }
        return (pack != null) ? pack : bestSoFar;
        
        }finally{//我加上的
        DEBUG.P(0,this,"findIdentInPackage(4)");
        }
    }