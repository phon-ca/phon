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
package ca.phon.script;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.*;

import ca.phon.extensions.*;
import ca.phon.script.params.*;

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
			LOGGER.error( e.getLocalizedMessage(), e);
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
