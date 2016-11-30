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
package ca.phon.script;

import java.net.URI;
import java.util.List;

import ca.phon.extensions.IExtendable;

/**
 * <p>Interface for Phon runtime scripts.  Scripts are written in
 * ECMAScript and use the Rhino engine directly (instead of using
 * the JSR.)</p>
 * 
 * <p>Phon scripts may also have parameters defined which can be
 * setup using either a comment at the beginning of the file or
 * by implementing the <code>setup_params</code> function in the 
 * script.</p>
 * 
 */
public interface PhonScript extends IExtendable, Appendable, CharSequence {
	
	/**
	 * Get the script text.
	 * 
	 * @return script
	 */
	public String getScript();
	
	/**
	 * Get a script context for this script.  The context
	 * is used to compile and evaulate the script.
	 * 
	 * @return the script context
	 */
	public PhonScriptContext getContext();
	
	/**
	 * Get required packages that should be imported when
	 * the scope is created.  These packages will also be
	 * available to any script imported using the <code>require()</code>
	 * function.
	 * 
	 * @return the list of packages that should be available
	 *  to this script and any dependencies
	 */
	public List<String> getPackageImports();
	
	public boolean addPackageImport(String pkgImport);

	public boolean removePackageImport(String pkgImport);
	
	/**
	 * Get a list of classes that should be imported when
	 * the scope is created.  These classes will also
	 * be availble to any script imported using the <code>require()</code>
	 * function.
	 * 
	 * @return the list of classes that should be availble
	 *  to this script and any dependencies
	 */
	public List<String> getClassImports();
	
	public boolean addClassImport(String classImport);
	
	public boolean removeClassImport(String classImport);
	
	/**
	 * Get the list of URLs that should be available
	 * for script loading using the <code>require</code>
	 * function.
	 * 
	 * @return list of javascript library folders
	 */
	public List<URI> getRequirePaths();
	
	public boolean addRequirePath(URI uri);
	
	public boolean removeRequirePath(URI uri);
	
	/* Buffer delegate methods */
	public PhonScript append(boolean arg0);

	public PhonScript append(char c);

	public PhonScript append(char[] str, int offset, int len);

	public PhonScript append(char[] str);

	public PhonScript append(CharSequence s, int start, int end);

	public PhonScript append(CharSequence s);

	public PhonScript append(double d);
	
	public PhonScript append(float f);

	public PhonScript append(int i);

	public PhonScript append(long lng);

	public PhonScript append(Object obj);

	public PhonScript append(String str);

	public PhonScript append(StringBuffer sb);

	public PhonScript appendCodePoint(int codePoint);

	public int capacity();

	public char charAt(int index);

	public int codePointAt(int index);

	public int codePointBefore(int index);

	public int codePointCount(int beginIndex, int endIndex);

	public PhonScript delete(int start, int end);
	
	public PhonScript deleteCharAt(int index);

	public void ensureCapacity(int minimumCapacity);

	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin);

	public int indexOf(String str, int fromIndex);

	public int indexOf(String str);

	public PhonScript insert(int offset, boolean b);

	public PhonScript insert(int offset, char c);

	public PhonScript insert(int index, char[] str, int offset, int len);

	public PhonScript insert(int offset, char[] str);

	public PhonScript insert(int dstOffset, CharSequence s, int start, int end);

	public PhonScript insert(int dstOffset, CharSequence s);

	public PhonScript insert(int offset, double d);

	public PhonScript insert(int offset, float f);

	public PhonScript insert(int offset, int i);

	public PhonScript insert(int offset, long l);

	public PhonScript insert(int offset, Object obj);

	public PhonScript insert(int offset, String str);

	public int lastIndexOf(String str, int fromIndex);

	public int lastIndexOf(String str);

	public int length();

	public int offsetByCodePoints(int index, int codePointOffset);

	public PhonScript replace(int start, int end, String str);

	public PhonScript reverse();

	public void setCharAt(int index, char ch);

	public void setLength(int newLength);

	public CharSequence subSequence(int start, int end);
	
	public String substring(int start, int end);

	public String substring(int start);

	public void trimToSize();
}
