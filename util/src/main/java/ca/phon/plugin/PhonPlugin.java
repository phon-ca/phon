/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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
package ca.phon.plugin;

import java.lang.annotation.*;

/**
 * Annotation for used for Phon plugins.
 * 
 * This annotation must exist for any phon plug-in
 * extension point.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PhonPlugin {
	
	/**
	 * Author name
	 * 
	 */
	public String author() default "";
	
	/**
	 * Plugin name
	 */
	public String name() default "";
	
	/**
	 * Plugin version
	 */
	public String version() default "1.0";
	
	/**
	 * Minimum phon version
	 * default is 1.5.2
	 */
	public String minPhonVersion() default "1.5.2";
	
	/**
	 * Author comments
	 */
	public String comments() default "";
	
	/**
	 * Location of any native libraries required 
	 * for this plug-in.  Path can be absolute or
	 * relative to the application's plugins folder.
	 */
	public String[] nativeLibs() default {};
	
}
