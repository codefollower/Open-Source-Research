package org.junit.internal.requests;

import org.junit.internal.runners.CompositeRunner;
import org.junit.runner.Request;
import org.junit.runner.Runner;

public class ClassesRequest extends Request {
	private static my.Debug DEBUG=new my.Debug(my.Debug.JUnitCore);//我加上的

	private final Class<?>[] fClasses;
	private final String fName;
	
	public ClassesRequest(String name, Class<?>... classes) {
		DEBUG.P(this,"ClassesRequest(2)");
		DEBUG.P("name="+name);

		fClasses= classes;
		fName= name;

		DEBUG.P(0,this,"ClassesRequest(2)");
	}

	/** @inheritDoc */
	@Override 
	public Runner getRunner() {
		try {//我加上的
		DEBUG.P(this,"getRunner()");

		CompositeRunner runner= new CompositeRunner(fName);
		for (Class<?> each : fClasses) {
			Runner childRunner= Request.aClass(each).getRunner();

			DEBUG.P("childRunner="+childRunner);
			if (childRunner != null)
				runner.add(childRunner);
		}
		return runner;

		}finally{//我加上的
		DEBUG.P(0,this,"getRunner()");
		}
	}
}