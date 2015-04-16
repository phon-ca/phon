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
	
	private final static Logger LOGGER = Logger.getLogger(BasicScript.class.getName());
	
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

	public boolean addPackageImport(String pkgImport) {
		return pkgImports.add(pkgImport);
	}

	public boolean removePackageImport(String pkgImport) {
		return pkgImports.remove(pkgImport);
	}

	@Override
	public List<String> getClassImports() {
		return Collections.unmodifiableList(classImports);
	}
	
	public boolean addClassImport(String classImport) {
		return classImports.add(classImport);
	}
	
	public boolean removeClassImport(String classImport) {
		return classImports.remove(classImport);
	}

	@Override
	public List<URI> getRequirePaths() {
		return Collections.unmodifiableList(requirePaths);
	}
	
	public boolean addRequirePath(URI uri) {
		return requirePaths.add(uri);
	}
	
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
	
}
