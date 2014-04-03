    public Iterable<? extends File> getLocation(Location location) {
    	try {//我加上的
        DEBUG.P(this,"getLocation(1)");
        DEBUG.P("location="+location);
		
    	nullCheck(location);
        paths.lazy();
        if (location == CLASS_OUTPUT) {
            return (getClassOutDir() == null ? null : List.of(getClassOutDir()));
        } else if (location == SOURCE_OUTPUT) {
            return (getSourceOutDir() == null ? null : List.of(getSourceOutDir()));
        } else
            return paths.getPathForLocation(location);

        }finally{//我加上的
        DEBUG.P(0,this,"getLocation(1)");
        }
    }