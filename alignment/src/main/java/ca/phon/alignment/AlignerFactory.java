package ca.phon.alignment;

/**
 * Factory for creating aligners.
 *
 */
public class AlignerFactory {
	
	
	private AlignerFactory() {
	}
	
	/**
	 * Create a new global aligner
	 * 
	 * @return aligner that uses the global alignment algorithm
	 */
	public <T> Aligner<T> createGlobalAligner(Class<T> type) {
		return null;
	}
	
	/**
	 * Create a new local aligner
	 * 
	 * @return aligner that uses the local alignment algorithm
	 */
	public <T> Aligner<T> createLocalAligner(Class<T> type) {
		return null;
	}
	
}
