package ca.phon.ipa.phone.phonex;

/**
 * <p>Extension point for the phonex language.  To add a new plug-in matcher
 * for the phonex langauge, perform the following steps:
 * <ul>
 * <li>Create a type implementing this interface</li>
 * <li>Add to the new type an annotation '@PhonexPlugin("&lt;name&gt;")', where
 * name is the identifier for the plug-in matcher.</li>
 * <li>Create (or modifiy) a file META-INF/services/ca.phon.ipa.phone.phonex.PluginMatcher with a
 * line containing the full name of the new type.</li>
 * </p>
 * 
 * <p>For an example, see {@link SyllabificationPhonexPlugin}.</p>
 */
public interface PluginMatcher {
	
//	/**
//	 * Check the given input string
//	 * for errors.
//	 * 
//	 * @param input the input string for the plug-in matcher.
//	 *  This is the data contained within parenthesis.
//	 *  
//	 * @throws PhonexPatternException if the given string
//	 *  is not well formed
//	 */
//	public void checkInput(String input)
//		throws PhonexPatternException;
	
	/**
	 * Create a new matcher for the given input string.
	 * 
	 * @param input the input string for the plug-in matcher.
	 *  This is the data contained within parenthesis.
	 * @return PhoneMatcher
	 * @throws PhonexPatternException if the input string
	 *  is not well formed.
	 */
	public PhoneMatcher createMatcher(String input)
		throws PhonexPatternException;

}
