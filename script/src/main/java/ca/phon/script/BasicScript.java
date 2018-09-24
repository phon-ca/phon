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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.script.params.ScriptParameters;

/**
 * Basic phon script in memory.  This script stores the script
 * text in an internal {@link StringBuffer}.  This object is intended
 * to be immutable, however sub-classes may alter the internal script
 * using the provided buffer.
 */
public class BasicScript implements PhonScript, Cloneable {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(BasicScript.class.getName());
	
	/*
	 * Script stored in a buffer.  Sub-classes have
	 * access so they may alter the script buffer internally.
	 */
	private final StringBuffer buffer = new StringBuffer();
	
	private final ExtensionSupport extSupport = new ExtensionSupport(BasicScript.class, this);
	
	private final List<String> pkgImports = new ArrayList<String>();
	
	private final List<String> classImports = new ArrayList<String>();
	
	private final List<URI> requirePaths = new ArrayList<URI>();
	
	/**
	 * Used by sub-classes to allow direct access to buffer
	 * 
	 * @return script buffer
	 */
	protected StringBuffer getBuffer() {
		return buffer;
	}
	
	// maintain a single context
	private AtomicReference<PhonScriptContext> contextRef = new AtomicReference<PhonScriptContext>();
	
	public BasicScript(String text) {
		super();
		buffer.append(text);
		
		setupImports();
	}
	
	public BasicScript(File file) throws IOException {
		super();
		
		final FileInputStream fin = new FileInputStream(file);
		final InputStreamReader reader = new InputStreamReader(fin, "UTF-8");
		final char[] buf = new char[1024];
		int read = -1;
		while((read = reader.read(buf)) > 0) {
			buffer.append(buf, 0, read);
		}
		reader.close();
		
		setupImports();
	}
	
	private void setupImports() {
		addPackageImport("Packages.ca.phon.script");
		addPackageImport("Packages.ca.phon.script.params");
	}
	
	@Override
	public String getScript() {
		return buffer.toString();
	}

	@Override
	public PhonScriptContext getContext() {
		if(contextRef.get() == null) {
			final PhonScriptContext context = new PhonScriptContext(this);
			contextRef.getAndSet(context);
		}
		return contextRef.get();
	}
	
	/**
	 * Resets query context.
	 * 
	 * @return the current context (before reset)
	 */
	public PhonScriptContext resetContext() {
		return contextRef.getAndSet(null);
	}

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}

	@Override
	public List<String> getPackageImports() {
		return Collections.unmodifiableList(pkgImports);
	}

	@Override
	public boolean addPackageImport(String pkgImport) {
		return pkgImports.add(pkgImport);
	}

	@Override
	public boolean removePackageImport(String pkgImport) {
		return pkgImports.remove(pkgImport);
	}

	@Override
	public List<String> getClassImports() {
		return Collections.unmodifiableList(classImports);
	}
	
	@Override
	public boolean addClassImport(String classImport) {
		return classImports.add(classImport);
	}
	
	@Override
	public boolean removeClassImport(String classImport) {
		return classImports.remove(classImport);
	}

	@Override
	public List<URI> getRequirePaths() {
		return Collections.unmodifiableList(requirePaths);
	}
	
	@Override
	public boolean addRequirePath(URI uri) {
		return requirePaths.add(uri);
	}
	
	@Override
	public boolean removeRequirePath(URI uri) {
		return requirePaths.remove(uri);
	}
	
	@Override
	public Object clone() {
		final BasicScript retVal = new BasicScript(getScript());
		
		try {
			final ScriptParameters myParams = getContext().getScriptParameters(getContext().getEvaluatedScope());
			final ScriptParameters clonedParams = 
					retVal.getContext().getScriptParameters(retVal.getContext().getEvaluatedScope());
			
			ScriptParameters.copyParams(myParams, clonedParams);
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	
		retVal.pkgImports.clear();
		retVal.pkgImports.addAll(pkgImports);
		
		retVal.classImports.clear();
		retVal.classImports.addAll(classImports);
		
		retVal.requirePaths.clear();
		retVal.requirePaths.addAll(requirePaths);
		
		return retVal;
	}
	
	/* Delegate methods */
	public BasicScript append(boolean arg0) {
		resetContext();
		buffer.append(arg0); return this;
	}

	public BasicScript append(char c) {
		resetContext();
		buffer.append(c); return this;
	}

	public BasicScript append(char[] str, int offset, int len) {
		resetContext();
		buffer.append(str, offset, len); return this;
	}

	public BasicScript append(char[] str) {
		buffer.append(str); return this;
	}

	public BasicScript append(CharSequence s, int start, int end) {
		resetContext();
		buffer.append(s, start, end); return this;
	}

	public BasicScript append(CharSequence s) {
		resetContext();
		buffer.append(s); return this;
	}

	public BasicScript append(double d) {
		resetContext();
		buffer.append(d); return this;
	}

	public BasicScript append(float f) {
		resetContext();
		buffer.append(f); return this;
	}

	public BasicScript append(int i) {
		resetContext();
		buffer.append(i); return this;
	}

	public BasicScript append(long lng) {
		resetContext();
		buffer.append(lng); return this;
	}

	public BasicScript append(Object obj) {
		resetContext();
		buffer.append(obj); return this;
	}

	public BasicScript append(String str) {
		resetContext();
		buffer.append(str); return this;
	}

	public BasicScript append(StringBuffer sb) {
		resetContext();
		buffer.append(sb); return this;
	}

	public BasicScript appendCodePoint(int codePoint) {
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

	public BasicScript delete(int start, int end) {
		resetContext();
		buffer.delete(start, end); return this;
	}

	public BasicScript deleteCharAt(int index) {
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

	public BasicScript insert(int offset, boolean b) {
		resetContext();
		buffer.insert(offset, b); return this;
	}

	public BasicScript insert(int offset, char c) {
		resetContext();
		buffer.insert(offset, c); return this;
	}

	public BasicScript insert(int index, char[] str, int offset, int len) {
		resetContext();
		buffer.insert(index, str, offset, len); return this;
	}

	public BasicScript insert(int offset, char[] str) {
		resetContext();
		buffer.insert(offset, str); return this;
	}

	public BasicScript insert(int dstOffset, CharSequence s, int start, int end) {
		resetContext();
		buffer.insert(dstOffset, s, start, end); return this;
	}

	public BasicScript insert(int dstOffset, CharSequence s) {
		resetContext();
		buffer.insert(dstOffset, s); return this;
	}

	public BasicScript insert(int offset, double d) {
		resetContext();
		buffer.insert(offset, d); return this;
	}

	public BasicScript insert(int offset, float f) {
		resetContext();
		buffer.insert(offset, f); return this;
	}

	public BasicScript insert(int offset, int i) {
		resetContext();
		buffer.insert(offset, i); return this;
	}

	public BasicScript insert(int offset, long l) {
		resetContext();
		buffer.insert(offset, l); return this;
	}

	public BasicScript insert(int offset, Object obj) {
		resetContext();
		buffer.insert(offset, obj); return this;
	}

	public BasicScript insert(int offset, String str) {
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

	public BasicScript replace(int start, int end, String str) {
		resetContext();
		buffer.replace(start, end, str); return this;
	}

	public BasicScript reverse() {
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
