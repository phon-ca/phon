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
package ca.phon.query.script;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.query.db.Query;
import ca.phon.query.db.QueryManager;
import ca.phon.script.BasicScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;

/**
 * Defers loading of the query script until data is needed.
 * 
 */
public class LazyQueryScript extends BasicScript {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(LazyQueryScript.class.getName());
	
	private boolean loaded = false;
	
	private final URL scriptURL;
	
	public LazyQueryScript(String script) {
		super(script);
		scriptURL = null;
	}
	
	public LazyQueryScript(URL url) {
		super("");
		this.scriptURL = url;
		
		putExtension(QueryName.class, new QueryName(url.getFile()));
	}
	
	@Override
	public String getScript() {
		if(!loaded) {
			// load script
			readScript();
			loaded = true;
		}
		return super.getScript();
	}
	
	private void readScript() {
		if(scriptURL == null) return;
		
		try {
			final String name = scriptURL.getPath();
			if(name != null && name.trim().length() > 0) {
				if(name.endsWith(".js")) {
					readRawScript();
				} else if(name.endsWith(".xml")) {
					readXmlScript();
				} else {
					throw new IOException("Unknown query script type " + name);
				}
			}
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	private void readRawScript() throws IOException {
		final InputStream in = getScriptURL().openStream();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		final StringBuffer buffer = getBuffer();
		String line = null;
		while((line = reader.readLine()) != null) {
			buffer.append(line);
			buffer.append("\n");
		}
		in.close();
	}
	
	private void readXmlScript() throws IOException {
		final InputStream in = getScriptURL().openStream();
		final QueryManager qm = QueryManager.getInstance();
		final Query q = qm.loadQuery(in);
		
		getBuffer().append(q.getScript().getSource());
		loaded = true;
		
		// setup saved parameters
		ScriptParameters params = new ScriptParameters();
		try {
			params = getContext().getScriptParameters(getContext().getEvaluatedScope());
		} catch (PhonScriptException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		for(ScriptParam sp:params) {
			for(String id:sp.getParamIds()) {
				Object v = q.getScript().getParameters().get(id);
				if(v != null) {
					sp.setValue(id, v);
				}
			}
		}
	}
	
	public URL getScriptURL() {
		return scriptURL;
	}
	
}
