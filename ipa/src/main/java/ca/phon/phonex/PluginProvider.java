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
