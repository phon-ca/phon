package ca.phon.capability;

/**
 * 
 */
public interface IExtension<T extends IExtendable> {

	/**
	 * Install extension on an extendable object.
	 */
	public void installExtension(T obj);
	
}
