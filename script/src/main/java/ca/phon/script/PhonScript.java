package ca.phon.script;

import java.net.URI;
import java.util.List;

import ca.phon.extensions.IExtendable;

/**
 * <p>Interface for Phon runtime scripts.  Scripts are written in
 * ECMAScript and use the Rhino engine directly (instead of using
 * the JSR.)</p>
 * 
 * <p>Phon scripts may also have parameters defined which can be
 * setup using either a comment at the beginning of the file or
 * by implementing the <code>setup_params</code> function in the 
 * script.</p>
 * 
 */
public interface PhonScript extends IExtendable {
	
	/**
	 * Get the script text.
	 * 
	 * @return script
	 */
	public String getScript();
	
	/**
	 * Get a script context for this script.  The context
	 * is used to compile and evaulate the script.
	 * 
	 * @return the script context
	 */
	public PhonScriptContext getContext();
	
	/**
	 * Get required packages that should be imported when
	 * the scope is created.  These packages will also be
	 * available to any script imported using the <code>require()</code>
	 * function.
	 * 
	 * @return the list of packages that should be available
	 *  to this script and any dependencies
	 */
	public List<String> getPackageImports();
	
	/**
	 * Get a list of classes that should be imported when
	 * the scope is created.  These classes will also
	 * be availble to any script imported using the <code>require()</code>
	 * function.
	 * 
	 * @return the list of classes that should be availble
	 *  to this script and any dependencies
	 */
	public List<String> getClassImports();
	
	/**
	 * Get the list of URLs that should be available
	 * for script loading using the <code>require</code>
	 * function.
	 * 
	 * @return list of javascript library folders
	 */
	public List<URI> getRequirePaths();
}
