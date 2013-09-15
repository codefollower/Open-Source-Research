package junit.framework;

import java.util.List;

import org.junit.Ignore;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;

public class JUnit4TestAdapter implements Test {
	private static my.Debug DEBUG=new my.Debug(my.Debug.JUnitCore);//我加上的

	private final Class<?> fNewTestClass;

	private Runner fRunner;

	private JUnit4TestAdapterCache fCache;

	public JUnit4TestAdapter(Class<?> newTestClass) {
		this(newTestClass, JUnit4TestAdapterCache.getDefault());
	}

	public JUnit4TestAdapter(final Class<?> newTestClass,
			JUnit4TestAdapterCache cache) {
		DEBUG.P(this,"JUnit4TestAdapter(2)");
		DEBUG.P("newTestClass="+newTestClass);
		DEBUG.P("cache="+cache);

		fCache = cache;
		fNewTestClass = newTestClass;
		fRunner = Request.classWithoutSuiteMethod(newTestClass).getRunner();

		DEBUG.P(0,this,"JUnit4TestAdapter(2)");
	}

	public int countTestCases() {
		return fRunner.testCount();
	}

	public void run(TestResult result) {
		fRunner.run(fCache.getNotifier(result, this));
	}

	// reflective interface for Eclipse
	public List<Test> getTests() {
		return fCache.asTestList(getDescription());
	}

	// reflective interface for Eclipse
	public Class<?> getTestClass() {
		return fNewTestClass;
	}
	
	public Description getDescription() {
		try {//我加上的
		DEBUG.P(this,"getDescription()");
		DEBUG.P("fRunner="+fRunner);

		Description description= fRunner.getDescription();		
		return removeIgnored(description);

		}finally{//我加上的
		DEBUG.P(0,this,"getDescription()");
		}
	}

	private Description removeIgnored(Description description) {
		try {//我加上的
		DEBUG.P(this,"removeIgnored(1)");
		DEBUG.P("description="+description);
		DEBUG.P("isIgnored(description)="+isIgnored(description));

		if (isIgnored(description))
			return Description.EMPTY;
		Description result = description.childlessCopy();
		for (Description each : description.getChildren()) {
			Description child= removeIgnored(each);

			DEBUG.P("child="+child);
			DEBUG.P("(! child.isEmpty())="+(! child.isEmpty()));

			if (! child.isEmpty())
				result.addChild(child);
		}
		return result;

		}finally{//我加上的
		DEBUG.P(0,this,"removeIgnored(1)");
		}
	}

	private boolean isIgnored(Description description) {
		return description.getAnnotation(Ignore.class) != null;
	}

	@Override
	public String toString() {
		return fNewTestClass.getName();
	}

	public void filter(Filter filter) throws NoTestsRemainException {
		filter.apply(fRunner);
	}

	public void sort(Sorter sorter) {
		sorter.apply(fRunner);
	}
}