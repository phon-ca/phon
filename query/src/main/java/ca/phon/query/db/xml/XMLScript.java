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

package ca.phon.query.db.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.query.db.Script;
import ca.phon.query.db.ScriptLibrary;
import ca.phon.query.db.ScriptURL;
import ca.phon.query.db.xml.io.query.ObjectFactory;
import ca.phon.query.db.xml.io.query.ParamType;
import ca.phon.query.db.xml.io.query.ScriptRelType;
import ca.phon.query.db.xml.io.query.ScriptType;
import ca.phon.query.db.xml.io.query.ScriptURLType;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.util.resources.ResourceLoader;


/**
 * An XML-based implementation of {@link Script}.
 */
public class XMLScript implements Script, JAXBWrapper<ScriptType> {
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(XMLScript.class.getName());
	
	/** JAXB object */
	ScriptType script;
	
	private String cachedSource;
	
	/**
	 * Default constructor.
	 */
	XMLScript() {
		this(new ScriptType());
	}
	
	/**
	 * Constructs script from a JAXB script object.
	 */
	XMLScript(ScriptType script) {
		this.script = script;
	}
	
	/**
	 * Get the JAXB element associated with this object.
	 * @return
	 */
	@Override
	public ScriptType getXMLObject() {
		return script;
	}
	
	@Override
	public String getSource() {
		String retVal = null;
		if(script.getSource() != null) {
			retVal = script.getSource();
		} else if(script.getUrl() != null) {
			if(cachedSource == null) {
				try {
					cachedSource = readFromUrl(script.getUrl());
				} catch (IOException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
				}
			}
			retVal = cachedSource;
		}
		return retVal;
	}
	
	private String readFromUrl(ScriptURLType url) throws IOException {
		final QueryScriptLibrary scriptLibrary = new QueryScriptLibrary();
		ResourceLoader<QueryScript> scriptLoader = null;
		switch(url.getRel()) {
		case ABSOLUTE:
			StringBuilder sb = new StringBuilder();
			try {
				final URI uri = new URI(url.getRef());
				final URL scriptUrl = uri.toURL();
				
				final BufferedReader reader = new BufferedReader(new InputStreamReader(scriptUrl.openStream()));
				String line = null;
				while((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
				reader.close();
			} catch (URISyntaxException e) {
				throw new IOException(e);
			}
			return sb.toString();
			
		case STOCK:
			scriptLoader = scriptLibrary.stockScriptFiles();
			break;
			
		case USER:
			scriptLoader = scriptLibrary.userScriptFiles();
			break;
			
		case PROJECT:
		case PLUGINS:
		default:
				break;
		}
		
		if(scriptLoader != null) {
			for(QueryScript script:scriptLoader) {
				final QueryName qn = script.getExtension(QueryName.class);
				if(qn != null && qn.getName().equals(url.getRef())) {
					return script.getScript();
				}
			}
		}
		return null;
	}
	
	@Override
	public void setUrl(ScriptURL url) {
		final ScriptURLType scriptUrl = (new ObjectFactory()).createScriptURLType();
		scriptUrl.setRef(url.getPath());
		switch(url.getLibrary()) {
		case STOCK:
			scriptUrl.setRel(ScriptRelType.STOCK);
			break;
			
		case USER:
			scriptUrl.setRel(ScriptRelType.USER);
			break;
			
		case PROJECT:
			scriptUrl.setRel(ScriptRelType.PROJECT);
			break;
			
		case PLUGINS:
			scriptUrl.setRel(ScriptRelType.PLUGINS);
			break;
			
		default:
			scriptUrl.setRel(ScriptRelType.ABSOLUTE);
		}
		script.setUrl(scriptUrl);
	}
	
	@Override
	public ScriptURL getUrl() {
		if(script.getUrl() != null) {
			final ScriptURL retVal = new ScriptURL();
			retVal.setPath(script.getUrl().getRef());
			switch(script.getUrl().getRel()) {
			case ABSOLUTE:
				retVal.setLibrary(ScriptLibrary.OTHER);
				break;
				
			case PLUGINS:
				retVal.setLibrary(ScriptLibrary.PLUGINS);
				break;
				
			case PROJECT:
				retVal.setLibrary(ScriptLibrary.PROJECT);
				break;
				
			case STOCK:
				retVal.setLibrary(ScriptLibrary.STOCK);
				break;
				
			case USER:
				retVal.setLibrary(ScriptLibrary.USER);
				break;
				
			default:
				break;
			}
			return retVal;
		} else {
			return null;
		}
	}

	@Override
	public void setSource(String source) {
		script.setSource(source);
	}

	@Override
	public Map<String, String> getParameters() {
		Map<String, String> paramMap = new HashMap<String, String>();
		for(ParamType param : script.getParam())
			paramMap.put(param.getId(), param.getValue());
		return paramMap;
	}

	@Override
	public void setParameters(Map<String, String> params) {
		script.getParam().clear();
		for(Map.Entry<String, String> param : params.entrySet()) {
			ParamType xmlParam = new ParamType();
			xmlParam.setId(param.getKey());
			xmlParam.setValue(param.getValue());
			script.getParam().add(xmlParam);
		}
	}

	@Override
	public String getMimeType() {
		return script.getMimetype();
	}

	@Override
	public void setMimeType(String mimeType) {
		script.setMimetype(mimeType);
	}
	
}
