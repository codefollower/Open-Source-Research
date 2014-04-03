    /**
     * Tries to complete lexically enclosing classes if c looks like a
     * nested class.  This is similar to completeOwners but handles
     * the situation when a nested class is accessed directly as it is
     * possible with the Tree API or javax.lang.model.*.
     */
    private void completeEnclosing(ClassSymbol c) {
        DEBUG.P(this,"completeEnclosing(1)");
    	DEBUG.P("c.owner.kind="+Kinds.toString(c.owner.kind));
        
        //如果有类名:my.test.ClassA$ClassB$ClassC
        //则分解为my.test.ClassA my.test.ClassB my.test.ClassC
        if (c.owner.kind == PCK) {
            Symbol owner = c.owner;
            DEBUG.P("c.owner="+c.owner);
            DEBUG.P("c.name="+c.name);
            DEBUG.P("Convert.shortName(c.name)="+Convert.shortName(c.name));
            DEBUG.P("Convert.enclosingCandidates(Convert.shortName(c.name)="+Convert.enclosingCandidates(Convert.shortName(c.name)));
            for (Name name : Convert.enclosingCandidates(Convert.shortName(c.name))) {
                Symbol encl = owner.members().lookup(name).sym;
                DEBUG.P("encl="+encl);
                if (encl == null)
                    encl = classes.get(TypeSymbol.formFlatName(name, owner));
                DEBUG.P("encl="+encl);
                if (encl != null)
                    encl.complete();
            }
        }
    	DEBUG.P(0,this,"completeEnclosing(1)");
    }