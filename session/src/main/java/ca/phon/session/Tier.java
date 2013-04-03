package ca.phon.session;

/**
 * A tier in a record.  A tier has a name, type and a number
 * of groups.
 * 
 */
public interface Tier<T> extends TierDescription {
	
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
	 * Set the value of the specified group 
	 * idx.  idx must be between 0 and numberOfGroups()
	 * 
	 * @param idx
	 * @param val
	 * 
	 * @throws ArrayIndexOutOfBoundsException if idx is
	 *  out of bounds
	 * @throws NullPointerException if val is <code>null</code>
	 */
	public void setGroup(int idx, T val);
	
	/**
	 * Adds a new group to the end of this tier and increments
	 * the number of groups.
	 * 
	 * @param val
	 */
	public void addGroup(T val);
	
	/**
	 * Remove the specified group
	 * 
	 * @param idx
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void removeGroup(int idx);
	
	/**
	 * Removes all group data from this tier and sets
	 * the number of groups to 0
	 * 
	 */
	public void removeAll();
	
}
