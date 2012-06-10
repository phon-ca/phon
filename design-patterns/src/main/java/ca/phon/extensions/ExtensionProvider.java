package ca.phon.extensions;

/**
 * Handles installation of extensions on {@link IExtendable}
 * object.  Extensions which require automatic loading
 * should provide an implementation of {@link ExtensionProvider}.
 */
public interface ExtensionProvider {

	/**
	 * Install extension on an extendable object.
	 */
	public void installExtension(IExtendable obj);
	
}
