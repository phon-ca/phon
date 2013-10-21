package ca.phon.script;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;

import ca.phon.extensions.ExtensionSupport;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;

/**
 * Basic phon script in memory.  This script stores the script
 * text in an internal {@link StringBuffer}.  This object is intended
 * to be immutable, however sub-classes may alter the internal script
 * using the provided buffer.
 */
public class BasicScript implements PhonScript {
	
	/*
	 * Script stored in a buffer.  Sub-classes have
	 * access so they may alter the script buffer internally.
	 */
	private final StringBuffer buffer = new StringBuffer();
	
	private final ExtensionSupport extSupport = new ExtensionSupport(BasicScript.class, this);
	
	private final List<String> pkgImports = new ArrayList<>();
	
	private final List<String> classImports = new ArrayList<>();
	
	private final List<URI> requirePaths = new ArrayList<>();
	
	protected StringBuffer getBuffer() {
		return buffer;
	}
	
	// maintain a single context
	private PhonScriptContext context = null;
	
	public BasicScript(String text) {
		super();
		buffer.append(text);
		
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
		if(context == null) {
			context = new PhonScriptContext(this);
		}
		return context;
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
	
}
