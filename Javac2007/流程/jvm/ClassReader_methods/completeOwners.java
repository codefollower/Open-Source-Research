    /** complete up through the enclosing package. */
    private void completeOwners(Symbol o) {
		DEBUG.P(this,"completeOwners(1)");
		DEBUG.P("o.kind="+Kinds.toString(o.kind));
		
        if (o.kind != PCK) completeOwners(o.owner);
        o.complete();
		DEBUG.P(0,this,"completeOwners(1)");
    }