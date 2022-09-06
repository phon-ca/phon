/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import java.io.*;
import java.net.*;

import ca.phon.extensions.*;
import ca.phon.query.db.*;

/**
 * Extension for {@link QueryScript} objects which provides
 * name information for the script object.
 */
@Extension(QueryScript.class)
public class QueryName {
	
	private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(QueryName.class
			.getName());

	private String name;
	
	private URL location;
	
	private ScriptLibrary scriptLibrary;
	
	private String category;
	
	public QueryName(URL url) {
		this.location = url;
		String path = url.getPath();
		final int lastSlash = path.lastIndexOf("/");

		this.name = (lastSlash > 0 ? path.substring(lastSlash + 1) : path);
		
		final URLDecoder urlDecoder = null;
		try {
			this.name = urlDecoder.decode(this.name, "UTF-8");

			final int lastDot = this.name.lastIndexOf('.');
			if(lastDot > 0) {
				this.name = this.name.substring(0, lastDot);
			}
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn( e.getLocalizedMessage(), e);
		}
	}
	
	public QueryName(String name) {
		this.name = name;
		this.location = null;
	}
	
	public String getName() {
		 return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public URL getLocation() {
		return location;
	}

	public void setLocation(URL location) {
		this.location = location;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	public String getCategory() {
		return (this.category != null ? this.category : "default");
	}
	
	public ScriptLibrary getScriptLibrary() {
		return this.scriptLibrary;
	}
	
	public void setScriptLibrary(ScriptLibrary scriptLibrary) {
		this.scriptLibrary = scriptLibrary;
	}
	
}
