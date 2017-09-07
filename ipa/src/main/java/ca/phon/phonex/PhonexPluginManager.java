/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.phonex;

import java.util.*;

/**
 * Class to help with loading/finding
 * phonex matcher plugins.
 */
public class PhonexPluginManager {
	
	/**
	 * List of available plug-in matchers
	 */
	private Map<String, PluginProvider> _pluginProviders = new TreeMap<String, PluginProvider>();
	
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
	public PluginProvider[] getPluginProviders() {
		PluginProvider[] retVal = _pluginProviders.values().toArray(new PluginProvider[0]);
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
	public PluginProvider getProvider(String name) {
		return _pluginProviders.get(name);
	}

	
	/**
	 * Load available plug-ins
	 */
	private void loadPlugins() {
		// load via ServiceLoader
		ServiceLoader<PluginProvider> loader = ServiceLoader.load(PluginProvider.class);
		for(PluginProvider pluginMatcher:loader) {
			checkAndAddPlugin(pluginMatcher);
		}
	}
	
	/**
	 * Check plugin annotation to ensure it
	 * has a type name specified that is not
	 * already used.
	 */
	private void checkAndAddPlugin(PluginProvider matcher) {
		Class<?> matcherClass = matcher.getClass();
		
		PhonexPlugin pluginAnnotation = matcherClass.getAnnotation(PhonexPlugin.class);
		if(pluginAnnotation != null) {
			String typeName = pluginAnnotation.name();
			if(_pluginProviders.get(typeName) == null) {
				_pluginProviders.put(typeName, matcher);
			}
		}
	}
}
