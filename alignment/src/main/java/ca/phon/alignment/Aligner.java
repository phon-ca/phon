package ca.phon.alignment;

/**
 * Calculates pairwise alignment of two sequences of
 * objects.
 * 
 * @param T the type of object being aligned
 */
public interface Aligner<T> {

	/**
	 * Calculate the alignment.  Alignment calculation
	 * is determined by implementation.  See global vs
	 * local alignment.
	 * 
	 * @return the alignment map
	 */
	public AlignmentMap<T> calculateAlignment(T[] top, T[] bottom);
	
	/**
	 * Returns the AlignmentMap calculated during
	 * the last call to 'calculateAlignment'.  Undefined
	 * if calculateAlignment was not called.
	 * 
	 * @return alignment map
	 */
	public AlignmentMap<T> getAlignmentMap();
	
}
