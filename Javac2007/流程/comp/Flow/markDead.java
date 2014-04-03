    /** Record that statement is unreachable.
     */
    void markDead() {
		DEBUG.P(this,"markDead()");
		DEBUG.P("firstadr="+firstadr+"  nextadr="+nextadr);
		DEBUG.P("inits  前="+inits);
		DEBUG.P("uninits前="+uninits);
		
		inits.inclRange(firstadr, nextadr);
		uninits.inclRange(firstadr, nextadr);
		
		DEBUG.P("inits  后="+inits);
		DEBUG.P("uninits后="+uninits);
		
		alive = false;
		DEBUG.P("alive="+alive);
		DEBUG.P(0,this,"markDead()");
    }