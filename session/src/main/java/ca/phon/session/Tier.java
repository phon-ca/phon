package ca.phon.session;

/**
 * A tier in a record.  A tier has a name, type and a number
 * of groups.
 * 
 */
public interface Tier<T> {
	
	/**
	 * Get the number of groups in the tier
	 * 
	 * @return number of groups
	 */
	public int numberOfGroups();
	
	/**
	 * Get value at given group
	 * 
	 * @param idx group indes
	 * @return value for the given group
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public T getGroup(int idx);
	
	/**
	 * Get tier description.  Tier description includes
	 * name and if the tier is 'grouped'.
	 * 
	 * @return tier description
	 */
	public TierDescription getDescription();
	
	/**
	 * Get type.
	 * 
	 * Gets the declared type for the tier.
	 * 
	 * @return class type
	 */
	public Class<T> getDeclaredType();
	
	
}
