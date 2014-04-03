    protected void lazy() {
		DEBUG.P(this,"lazy()");
		DEBUG.P("inited="+inited);
		
		//在初始化时执行(也就是在parser之前)
		if (!inited) {
			//是否加了Xlint:中的path选项,一般为没加
			//如果加了-Xlint:path时，如果路径名有错时，会发出警告
			warn = lint.isEnabled(Lint.LintCategory.PATH);
			
			pathsForLocation.put(PLATFORM_CLASS_PATH, computeBootClassPath());
			
			DEBUG.P(this,"computeUserClassPath()");
			pathsForLocation.put(CLASS_PATH, computeUserClassPath());
			DEBUG.P(2,this,"computeUserClassPath()");
			
			DEBUG.P(this,"computeSourcePath()");
			pathsForLocation.put(SOURCE_PATH, computeSourcePath());
			DEBUG.P(2,this,"computeSourcePath()");

			inited = true;
		}
		
		DEBUG.P(0,this,"lazy()");
    }