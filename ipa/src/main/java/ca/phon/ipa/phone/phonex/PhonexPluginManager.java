package ca.phon.ipa.phone.phonex;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.TreeMap;

/**
 * Class to help with loading/finding
 * phonex matcher plugins.
 */
public class PhonexPluginManager {
	
	/**
	 * List of available plug-in matchers
	 */
	private Map<String, PluginMatcher> _pluginMatchers = new TreeMap<String, PluginMatcher>();
	
	/**
	 * Shared instance
	 */
	private static PhonexPluginManager sharedManager;
	
	/**
	 * Return shared instance
	 * 
	 * @return shared plugin manager instance
	 */
	public static PhonexPluginManager getSharedInstance() {
		if(sharedManager == null) {
			sharedManager = new PhonexPluginManager();
		}
		return sharedManager;
	}
	
	/**
	 * Hidden constructor
	 */
	private PhonexPluginManager() {
		loadPlugins();
	}

	/**
	 * Load all phonex plug-in matchers.
	 * Plug-ins are discovered by {@link ServiceLoader}.
	 * 
	 * @return the list of available plug-in matchers
	 */
	public PluginMatcher[] getPluginMatchers() {
		PluginMatcher[] retVal = _pluginMatchers.values().toArray(new PluginMatcher[0]);
		return retVal;
	}
	
	/**
	 * Find an appropriate matcher plug-in for the
	 * given name.
	 * 
	 * @param name
	 * @return the requested plug-in matcher or <code>null</code>
	 *  if not found
	 */
	public PluginMatcher getMatcher(String name) {
		return _pluginMatchers.get(name);
	}

	
	/**
	 * Load available plug-ins
	 */
	private void loadPlugins() {
		// load via ServiceLoader
		ServiceLoader<PluginMatcher> loader = ServiceLoader.load(PluginMatcher.class);
		for(PluginMatcher pluginMatcher:loader) {
			checkAndAddPlugin(pluginMatcher);
		}
	}
	
	/**
	 * Check plugin annotation to ensure it
	 * has a type name specified that is not
	 * already used.
	 */
	private void checkAndAddPlugin(PluginMatcher matcher) {
		Class<?> matcherClass = matcher.getClass();
		
		PhonexPlugin pluginAnnotation = matcherClass.getAnnotation(PhonexPlugin.class);
		if(pluginAnnotation != null) {
			String typeName = pluginAnnotation.value();
			if(_pluginMatchers.get(typeName) == null) {
				_pluginMatchers.put(typeName, matcher);
			}
		}
	}
}
