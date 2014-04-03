    /** Merge (intersect) inits/uninits from WhenTrue/WhenFalse sets.
     */
    void merge() {
		DEBUG.P(this,"merge()");
		DEBUG.P("inits  前="+inits);
		DEBUG.P("uninits前="+uninits);

		inits = initsWhenFalse.andSet(initsWhenTrue);
		uninits = uninitsWhenFalse.andSet(uninitsWhenTrue);

		DEBUG.P("inits  后="+inits);
		DEBUG.P("uninits后="+uninits);
		DEBUG.P(0,this,"merge()");
    }