    void readEnclosingMethodAttr(Symbol sym) {
        // sym is a nested class with an "Enclosing Method" attribute
        // remove sym from it's current owners scope and place it in
        // the scope specified by the attribute
        sym.owner.members().remove(sym);
        ClassSymbol self = (ClassSymbol)sym;
        ClassSymbol c = readClassSymbol(nextChar());
        NameAndType nt = (NameAndType)readPool(nextChar());

        MethodSymbol m = findMethod(nt, c.members_field, self.flags());
        if (nt != null && m == null)
            throw badClassFile("bad.enclosing.method", self);

        self.name = simpleBinaryName(self.flatname, c.flatname) ;
        self.owner = m != null ? m : c;
        if (self.name.len == 0)
            self.fullname = null;
        else
            self.fullname = ClassSymbol.formFullName(self.name, self.owner);

        if (m != null) {
            ((ClassType)sym.type).setEnclosingType(m.type);
        } else if ((self.flags_field & STATIC) == 0) {
            ((ClassType)sym.type).setEnclosingType(c.type);
        } else {
            ((ClassType)sym.type).setEnclosingType(Type.noType);
        }
        enterTypevars(self);
        if (!missingTypeVariables.isEmpty()) {
            ListBuffer<Type> typeVars =  new ListBuffer<Type>();
            for (Type typevar : missingTypeVariables) {
                typeVars.append(findTypeVar(typevar.tsym.name));
            }
            foundTypeVariables = typeVars.toList();
        } else {
            foundTypeVariables = List.nil();
        }
    }