    /** Split (duplicate) inits/uninits into WhenTrue/WhenFalse sets
     */
    void split() {
		DEBUG.P(this,"split()");
		
		initsWhenFalse = inits.dup();
		uninitsWhenFalse = uninits.dup();
		initsWhenTrue = inits;
		uninitsWhenTrue = uninits;
		inits = uninits = null;
		
		DEBUG.P(0,this,"split()");
    }