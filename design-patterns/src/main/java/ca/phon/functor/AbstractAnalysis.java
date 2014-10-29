package ca.phon.functor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractAnalysis<R, T> implements Analysis<R, T> {
	
	protected final List<AnalysisStep<?, ?>> functors = 
			Collections.synchronizedList(new ArrayList<AnalysisStep<?, ?>>());

	public AbstractAnalysis() {
	}
	
	public void addStep(AnalysisStep<?, ?> step) {
		functors.add(step);
	}
	
	@Override
	public AnalysisStep<?, ?>[] getAnalysis() {
		return functors.toArray(new AnalysisStep<?, ?>[0]);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public R performAnalysis(T val) {
		checkAnalysis();
		Object prevReturn = val;
		for(AnalysisStep step:getAnalysis()) {
			prevReturn = step.op(prevReturn);
		}
		return (R)getReturnType().cast( prevReturn );
	}

	public abstract Class<R> getReturnType();
	
	public abstract Class<T> getParameterType();

	// ensure sanity
	private void checkAnalysis() {
		if(getAnalysis().length == 0)
			throw new IllegalStateException("Empty analysis");
		
		Class<?> paramType = getParameterType();
		for(AnalysisStep<?, ?> step:getAnalysis()) {
			// ensure param type match
			if(!paramType.isAssignableFrom(step.getParameterType())) {
				throw new IllegalStateException("Analysis chain does not link");
			}
			paramType = step.getReturnType();
		}
		
		final AnalysisStep<?, ?> lastStep = getAnalysis()[(getAnalysis().length-1)];
		if(!getReturnType().isAssignableFrom(lastStep.getReturnType())) {
			throw new IllegalStateException("Analysis chain is incomplete");
		}
	}

}
