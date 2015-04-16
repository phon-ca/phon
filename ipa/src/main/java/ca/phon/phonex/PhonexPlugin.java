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
package ca.phon.phonex;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
	
}
