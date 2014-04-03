/************************************************************************
 * Loading Packages
 ***********************************************************************/

    /** Check to see if a package exists, given its fully qualified name.
     */
    public boolean packageExists(Name fullname) {
    	try {//我加上的
		DEBUG.P(this,"packageExists(Name fullname)");
		DEBUG.P("fullname="+fullname);

        return enterPackage(fullname).exists();
        
        }finally{//我加上的
		DEBUG.P(0,this,"packageExists(Name fullname)");
		}
    }