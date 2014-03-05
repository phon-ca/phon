package ca.phon.session;

/**
 * Listener for tier group changes.
 */
public interface TierListener<T> {

	/**
	 * Called when a new group has been added to a tier
	 * 
	 * @param tier
	 * @param index
	 * @param value
	 */
	public void groupAdded(Tier<T> tier, int index, T value);
	
	/**
	 * Called when a group is removed from a tier
	 * 
	 * @param tier
	 * @param index
	 * @param value
	 */
	public void groupRemoved(Tier<T> tier, int index, T value);
	
	/**
	 * Called when the value of a group changes
	 * 
	 * @param tier
	 * @param index
	 * @param oldValue
	 * @param value
	 */
	public void groupChanged(Tier<T> tier, int index, T oldValue, T value);
	
	/**
	 * Called when all groups have been removed from a tier
	 * 
	 * @param tier
	 */
	public void groupsCleared(Tier<T> tier);
	
}
