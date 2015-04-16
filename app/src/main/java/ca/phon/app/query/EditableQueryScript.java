/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.query;

import java.net.URI;
import java.util.List;

import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParameters;

/**
 * Query script implementation with exposed
 * methods for use in text editors.
 *
 */
public class EditableQueryScript extends QueryScript {

	private final StringBuffer buffer;
	
	public EditableQueryScript(QueryScript queryScript) {
		super(queryScript.getScript());
		
		// copy name if provided
		final QueryName queryName = queryScript.getExtension(QueryName.class);
		putExtension(QueryName.class, queryName);
		
		final List<URI> currentRequiredPaths = getRequirePaths();
		// copy require paths
		for(URI path:queryScript.getRequirePaths()) {
			if(!currentRequiredPaths.contains(path))
				addRequirePath(path);
		}
		
		// copy params
		try {
			ScriptParameters origParams = queryScript.getContext().getScriptParameters(queryScript.getContext().getEvaluatedScope());
			ScriptParameters myParams = getContext().getScriptParameters(getContext().getEvaluatedScope());
			ScriptParameters.copyParams(origParams, myParams);
		} catch (PhonScriptException e) {
		}
		
		
		buffer = super.getBuffer();
	}

	/* Delegate methods */
	public EditableQueryScript append(boolean arg0) {
		resetContext();
		buffer.append(arg0); return this;
	}

	public EditableQueryScript append(char c) {
		resetContext();
		buffer.append(c); return this;
	}

	public EditableQueryScript append(char[] str, int offset, int len) {
		resetContext();
		buffer.append(str, offset, len); return this;
	}

	public EditableQueryScript append(char[] str) {
		buffer.append(str); return this;
	}

	public EditableQueryScript append(CharSequence s, int start, int end) {
		resetContext();
		buffer.append(s, start, end); return this;
	}

	public EditableQueryScript append(CharSequence s) {
		resetContext();
		buffer.append(s); return this;
	}

	public EditableQueryScript append(double d) {
		resetContext();
		buffer.append(d); return this;
	}

	public EditableQueryScript append(float f) {
		resetContext();
		buffer.append(f); return this;
	}

	public EditableQueryScript append(int i) {
		resetContext();
		buffer.append(i); return this;
	}

	public EditableQueryScript append(long lng) {
		resetContext();
		buffer.append(lng); return this;
	}

	public EditableQueryScript append(Object obj) {
		resetContext();
		buffer.append(obj); return this;
	}

	public EditableQueryScript append(String str) {
		resetContext();
		buffer.append(str); return this;
	}

	public EditableQueryScript append(StringBuffer sb) {
		resetContext();
		buffer.append(sb); return this;
	}

	public EditableQueryScript appendCodePoint(int codePoint) {
		resetContext();
		buffer.appendCodePoint(codePoint); return this;
	}

	public int capacity() {
		return buffer.capacity();
	}

	public char charAt(int index) {
		return buffer.charAt(index);
	}

	public int codePointAt(int index) {
		return buffer.codePointAt(index);
	}

	public int codePointBefore(int index) {
		return buffer.codePointBefore(index);
	}

	public int codePointCount(int beginIndex, int endIndex) {
		return buffer.codePointCount(beginIndex, endIndex);
	}

	public EditableQueryScript delete(int start, int end) {
		resetContext();
		buffer.delete(start, end); return this;
	}

	public EditableQueryScript deleteCharAt(int index) {
		resetContext();
		buffer.deleteCharAt(index); return this;
	}

	public void ensureCapacity(int minimumCapacity) {
		resetContext();
		buffer.ensureCapacity(minimumCapacity);
	}

	public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
		buffer.getChars(srcBegin, srcEnd, dst, dstBegin);
	}

	public int indexOf(String str, int fromIndex) {
		return buffer.indexOf(str, fromIndex);
	}

	public int indexOf(String str) {
		return buffer.indexOf(str);
	}

	public EditableQueryScript insert(int offset, boolean b) {
		resetContext();
		buffer.insert(offset, b); return this;
	}

	public EditableQueryScript insert(int offset, char c) {
		resetContext();
		buffer.insert(offset, c); return this;
	}

	public EditableQueryScript insert(int index, char[] str, int offset, int len) {
		resetContext();
		buffer.insert(index, str, offset, len); return this;
	}

	public EditableQueryScript insert(int offset, char[] str) {
		resetContext();
		buffer.insert(offset, str); return this;
	}

	public EditableQueryScript insert(int dstOffset, CharSequence s, int start, int end) {
		resetContext();
		buffer.insert(dstOffset, s, start, end); return this;
	}

	public EditableQueryScript insert(int dstOffset, CharSequence s) {
		resetContext();
		buffer.insert(dstOffset, s); return this;
	}

	public EditableQueryScript insert(int offset, double d) {
		resetContext();
		buffer.insert(offset, d); return this;
	}

	public EditableQueryScript insert(int offset, float f) {
		resetContext();
		buffer.insert(offset, f); return this;
	}

	public EditableQueryScript insert(int offset, int i) {
		resetContext();
		buffer.insert(offset, i); return this;
	}

	public EditableQueryScript insert(int offset, long l) {
		resetContext();
		buffer.insert(offset, l); return this;
	}

	public EditableQueryScript insert(int offset, Object obj) {
		resetContext();
		buffer.insert(offset, obj); return this;
	}

	public EditableQueryScript insert(int offset, String str) {
		resetContext();
		buffer.insert(offset, str); return this;
	}

	public int lastIndexOf(String str, int fromIndex) {
		return buffer.lastIndexOf(str, fromIndex);
	}

	public int lastIndexOf(String str) {
		return buffer.lastIndexOf(str);
	}

	public int length() {
		return buffer.length();
	}

	public int offsetByCodePoints(int index, int codePointOffset) {
		resetContext();
		return buffer.offsetByCodePoints(index, codePointOffset);
	}

	public EditableQueryScript replace(int start, int end, String str) {
		resetContext();
		buffer.replace(start, end, str); return this;
	}

	public EditableQueryScript reverse() {
		resetContext();
		buffer.reverse(); return this;
	}

	public void setCharAt(int index, char ch) {
		resetContext();
		buffer.setCharAt(index, ch);
	}

	public void setLength(int newLength) {
		resetContext();
		buffer.setLength(newLength);
	}

	public CharSequence subSequence(int start, int end) {
		return buffer.subSequence(start, end);
	}

	public String substring(int start, int end) {
		return buffer.substring(start, end);
	}

	public String substring(int start) {
		return buffer.substring(start);
	}

	public void trimToSize() {
		resetContext();
		buffer.trimToSize();
	}
	
	

}
