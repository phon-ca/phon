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

package ca.phon.query.db.xml;

import java.io.*;
import java.net.*;
import java.util.*;

import ca.phon.query.db.*;
import ca.phon.query.db.xml.io.query.*;
import ca.phon.query.script.*;
import ca.phon.util.resources.*;


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
