package ca.phon.functor;



/**
 * 
 * @param <R> type for the return value of the analysis
 * @param <T> type for the input value of the anlysis
 */
public interface Analysis<R, T> {
	
	/**
	 * List of functors which compose the analysis.
	 */
	public Functor<?, ?>[] getAnalysis();
	
	/**
	 * Perform the analysis.
	 * 
	 * @param val
	 * 
	 * @return the result of the analysis
	 */
	public R performAnalysis(T val);
	
}
