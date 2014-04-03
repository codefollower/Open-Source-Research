    protected Scanner.Factory getScannerFactory() {
    	try {//我加上的
    	DEBUG.P(this,"getScannerFactory()");
    	
        return Scanner.Factory.instance(context);
        
        }finally{//我加上的
		DEBUG.P(0,this,"getScannerFactory()");
		}
    }







































