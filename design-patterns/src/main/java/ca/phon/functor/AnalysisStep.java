package ca.phon.functor;


public interface AnalysisStep<R, T> extends Functor<R, T> {
	
	public Class<R> getReturnType();
	
	public Class<T> getParameterType();

}
