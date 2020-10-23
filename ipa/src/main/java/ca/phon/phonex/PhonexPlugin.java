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

import java.lang.annotation.*;

/**
 * Annotation required for phonex plug-ins.  
 * Identifies the name used for matcher
 * part of the phonex expression.
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PhonexPlugin {
	
	/**
	 * <p>The name used in the phonex expression to identify
	 * the matcher.</p>
	 * 
	 * <p>
	 * For example, if the a class FooMatcher implements {@link PluginProvider} and
	 * has the annotation <code>@PhonexPlugin(name="foo")</code> then FooMatcher will
	 * be used to parse phonex matchers identified with the "foo" string.
	 * <pre>
	 * {}:foo(&lt;expression&gt;)
	 * </pre>
	 * 
	 * The value of expression will be passed into the {@link PluginProvider#checkInput(String)}
	 * and {@link PluginProvider#createMatcher(String)} methods.
	 * </p>
	 */
	public String name();
	
	/**
	 * Description of plug-in
	 */
	public String description() default "";
	
	/**
	 * List of argument names
	 * 
	 * 
	 */
	public String[] arguments() default {};
		
}
