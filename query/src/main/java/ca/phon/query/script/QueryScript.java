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
package ca.phon.query.script;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;

import ca.phon.plugin.PluginManager;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptContext;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParameters;

/**
 * Holds the text for a query script.
 * Handles methods for parsing the script paramaters
 * and default tags.
 * 
 */
public class QueryScript extends LazyQueryScript {
	
	private final AtomicReference<QueryScriptContext> contextRef 
		= new AtomicReference<QueryScriptContext>();
	
	private static final class ScriptRequirementsInstalled {}
	/**
	 * Setup required package imports and require() library paths.
	 *
	 * @param script
	 */
	public static void setupScriptRequirements(PhonScript script) {
		if(script.getExtension(ScriptRequirementsInstalled.class) == null) {
			final ClassLoader cl = PluginManager.getInstance();
			Enumeration<URL> libUrls;
			try {
				libUrls = cl.getResources("ca/phon/query/script/");
				while(libUrls.hasMoreElements()) {
					final URL url = libUrls.nextElement();
					try {
						final URI uri = url.toURI();
						script.addRequirePath(uri);
					} catch (URISyntaxException e) {
						LOGGER.error( e.getLocalizedMessage(), e);
					}
				}
			} catch (IOException e1) {
				LOGGER.error( e1.getLocalizedMessage(), e1);
			}
			
			script.addPackageImport("Packages.ca.phon.orthography");
			script.addPackageImport("Packages.ca.phon.ipa");
			script.addPackageImport("Packages.ca.phon.ipa.features");
			script.addPackageImport("Packages.ca.phon.phonex");
			script.addPackageImport("Packages.ca.phon.syllable");
			script.addPackageImport("Packages.ca.phon.util");
			script.addPackageImport("Packages.ca.phon.project");
			script.addPackageImport("Packages.ca.phon.session");
			script.addPackageImport("Packages.ca.phon.session.format");
			script.addPackageImport("Packages.ca.phon.project");
			script.addPackageImport("Packages.ca.phon.query");
			script.addPackageImport("Packages.ca.phon.query.report");
			script.addPackageImport("Packages.ca.phon.query.report.datasource");

			
			script.addClassImport("Packages.org.apache.commons.lang3.StringUtils");
			
			script.putExtension(ScriptRequirementsInstalled.class, new ScriptRequirementsInstalled());
		}
	}
	
	/**
	 * Query functions
	 */
	public static enum QueryFunction {
		BEGIN_SEARCH("begin_search", 1),
		QUERY_RECORD("query_record", 2),
		END_SEARCH("end_search", 1);
		
		private String functionName;
		
		private int arity;
		
		private QueryFunction(String name, int arity) {
			this.functionName = name;
			this.arity = arity;
		}
		
		public int getArity() {
			return this.arity;
		}
		
		public String getName() {
			return this.functionName;
		}
	}
	
	/**
	 * Constructor
	 * 
	 */
	public QueryScript(String script) {
		super(script);
		setupLibraryFolders();
	}
	
	public QueryScript(URL url) {
		super(url);
		
		final QueryName queryName = new QueryName(url);
		putExtension(QueryName.class, queryName);
		
		setupLibraryFolders();
	}
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(QueryScript.class.getName());
	/**
	 * Setup library folders for 'require'
	 */
	private void setupLibraryFolders() {
		final ClassLoader cl = PluginManager.getInstance();
		Enumeration<URL> libUrls;
		try {
			libUrls = cl.getResources("ca/phon/query/script/");
			while(libUrls.hasMoreElements()) {
				final URL url = libUrls.nextElement();
				try {
					final URI uri = url.toURI();
					super.addRequirePath(uri);
				} catch (URISyntaxException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
				}
			}
		} catch (IOException e1) {
			LOGGER.error( e1.getLocalizedMessage(), e1);
		}
		
		super.addPackageImport("Packages.ca.phon.orthography");
		super.addPackageImport("Packages.ca.phon.ipa");
		super.addPackageImport("Packages.ca.phon.ipa.features");
		super.addPackageImport("Packages.ca.phon.phonex");
		super.addPackageImport("Packages.ca.phon.syllable");
		super.addPackageImport("Packages.ca.phon.util");
		super.addPackageImport("Packages.ca.phon.project");
		super.addPackageImport("Packages.ca.phon.session");
		super.addPackageImport("Packages.ca.phon.session.format");
		
		super.addClassImport("Packages.org.apache.commons.lang3.StringUtils");
	}
	
	@Override
	public PhonScriptContext resetContext() {
		return contextRef.getAndSet(null);
	}
	
	@Override
	public PhonScriptContext getContext() {
		return getQueryContext();
	}
	
	public QueryScriptContext getQueryContext() {
		if(contextRef.get() == null) {
			final QueryScriptContext context = new QueryScriptContext(this);
			contextRef.getAndSet(context);
		}
		return contextRef.get();
	}
	
	public String getHashString() {
		final String scriptText = getScript();
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			byte[] hash = digest.digest(scriptText.getBytes());

			StringBuffer buffer = new StringBuffer();
			for(int i = 0; i < hash.length; i++) {
				if((0xff & hash[i]) < 0x10) {
					buffer.append('0');
				}
				buffer.append(Integer.toHexString(0xff & hash[i]));
			}
			return buffer.toString();
		} catch (NoSuchAlgorithmException e) {
			return Integer.toHexString(hashCode());
		}
	}
	
	@Override
	public Object clone() {
		final QueryScript retVal = new QueryScript(getScript());
		
		try {
			final ScriptParameters myParams = getContext().getScriptParameters(getContext().getEvaluatedScope());
			final ScriptParameters clonedParams = 
					retVal.getContext().getScriptParameters(retVal.getContext().getEvaluatedScope());
			
			ScriptParameters.copyParams(myParams, clonedParams);
		} catch (PhonScriptException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		
		final QueryName myName = getExtension(QueryName.class);
		if(myName != null) {
			final QueryName name = new QueryName(myName.getName());
			name.setLocation(myName.getLocation());
			name.setCategory(myName.getCategory());
			retVal.putExtension(QueryName.class, myName);
		}
	
		return retVal;
	}
	
}
