/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
package ca.phon.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
