package ca.phon.phonex;

import java.util.List;

/**
 * <p>Extension point for the phonex language.  To add a new plug-in matcher
 * for the phonex langauge, perform the following steps:
 * <ul>
 * <li>Create a type implementing this interface</li>
 * <li>Add to the new type an annotation '@PhonexPlugin("&lt;name&gt;")', where
 * name is the identifier for the plug-in matcher.</li>
 * <li>Create (or modifiy) a file META-INF/services/ca.phon.phonex.PluginProvider with a
 * line containing the full name of the new type.</li>
 * </p>
 * 
 * <p>For an example, see {@link SyllabificationPhonexPlugin}.</p>
 */
public interface PluginProvider {
	
	/**
	 * Create a new matcher for the given input string.
	 * 
	 * @args arguments to the matcher
	 * @return PhoneMatcher
	 * @throws IllegalArgumentException if there was a problem
	 *  creating the plug-in matcher
	 * @throws NullPointerException if the provided argument list
	 *  is <code>null</code>
	 */
	public PhoneMatcher createMatcher(List<String> args)
		throws IllegalArgumentException;

}
