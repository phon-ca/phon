/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
