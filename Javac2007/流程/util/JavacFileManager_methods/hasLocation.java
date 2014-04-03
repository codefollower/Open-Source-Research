    public boolean hasLocation(Location location) {
    	//return getLocation(location) != null;
    	
    	DEBUG.P(this,"hasLocation(1)");
    	DEBUG.P("location="+location);
    	boolean b=getLocation(location) != null;
    	DEBUG.P(location+" : hasLocation="+b);
        DEBUG.P(3,this,"hasLocation(1)");
        return b;
    }