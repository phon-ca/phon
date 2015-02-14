/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
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
package ca.phon.query.script;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.plugin.PluginManager;
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
	private static final Logger LOGGER = Logger
			.getLogger(QueryScript.class.getName());
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
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		} catch (IOException e1) {
			LOGGER.log(Level.SEVERE, e1.getLocalizedMessage(), e1);
		}
		
		super.addPackageImport("Packages.ca.phon.ipa");
		super.addPackageImport("Packages.ca.phon.ipa.features");
		super.addPackageImport("Packages.ca.phon.phonex");
		super.addPackageImport("Packages.ca.phon.syllable");
		super.addPackageImport("Packages.ca.phon.util");
		super.addPackageImport("Packages.ca.phon.project");
		super.addPackageImport("Packages.ca.phon.session");
		
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
	
	@Override
	public Object clone() {
		final QueryScript retVal = new QueryScript(getScript());
		
		try {
			final ScriptParameters myParams = getContext().getScriptParameters(getContext().getEvaluatedScope());
			final ScriptParameters clonedParams = 
					retVal.getContext().getScriptParameters(retVal.getContext().getEvaluatedScope());
			
			ScriptParameters.copyParams(myParams, clonedParams);
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
