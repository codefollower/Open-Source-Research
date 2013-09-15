package org.junit.internal.runners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;

public class CompositeRunner extends Runner implements Filterable, Sortable {
	private static my.Debug DEBUG=new my.Debug(my.Debug.JUnitCore);//我加上的
	
	private final List<Runner> fRunners= new ArrayList<Runner>();
	private final String fName;
	
	public CompositeRunner(String name) {
		DEBUG.P(this,"CompositeRunner(1)");
		DEBUG.P("name="+name);
		DEBUG.P("fRunners.size="+fRunners.size());

		fName= name;

		DEBUG.P(0,this,"CompositeRunner(1)");
	}
	
	@Override
	public void run(RunNotifier notifier) {
		DEBUG.P(this,"run(1)");

		runChildren(notifier);

		DEBUG.P(0,this,"run(1)");
	}

	protected void runChildren(RunNotifier notifier) {
		DEBUG.P(this,"runChildren(1)");
		for (Runner each : fRunners)
			DEBUG.P("each="+each);

		for (Runner each : fRunners)
			each.run(notifier);

		DEBUG.P(0,this,"runChildren(1)");
	}

	@Override
	public Description getDescription() {
		try {//我加上的
		DEBUG.P(this,"getDescription()");
		DEBUG.P("fName="+fName);

		//如果是第一次加载Description类，会先初始化EMPTY和TEST_MECHANISM
		Description spec= Description.createSuiteDescription(fName);

		DEBUG.P("spec="+spec);
		DEBUG.P("fRunners.size="+fRunners.size());
		for (Runner runner : fRunners)
			spec.addChild(runner.getDescription());
		return spec;

		}finally{//我加上的
		DEBUG.P(0,this,"getDescription()");
		}
	}

	public List<Runner> getRunners() {
		return fRunners;
	}

	public void addAll(List<? extends Runner> runners) {
		fRunners.addAll(runners);
	}

	public void add(Runner runner) {
		DEBUG.P(this,"add(1)");
		DEBUG.P("runner="+runner);
		//DEBUG.e();

		fRunners.add(runner);

		DEBUG.P(0,this,"add(1)");
	}
	
	public void filter(Filter filter) throws NoTestsRemainException {
		for (Iterator<Runner> iter= fRunners.iterator(); iter.hasNext();) {
			Runner runner= iter.next();
			if (filter.shouldRun(runner.getDescription()))
				filter.apply(runner);
			else
				iter.remove();
		}
	}

	protected String getName() {
		return fName;
	}

	public void sort(final Sorter sorter) {
		Collections.sort(fRunners, new Comparator<Runner>() {
			public int compare(Runner o1, Runner o2) {
				return sorter.compare(o1.getDescription(), o2.getDescription());
			}
		});
		for (Runner each : fRunners)
			sorter.apply(each);
	}
}
