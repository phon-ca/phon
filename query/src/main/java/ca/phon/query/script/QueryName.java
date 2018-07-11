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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.extensions.Extension;
import ca.phon.query.db.ScriptLibrary;

/**
 * Extension for {@link QueryScript} objects which provides
 * name information for the script object.
 */
@Extension(QueryScript.class)
public class QueryName {
	
	private static final Logger LOGGER = Logger.getLogger(QueryName.class
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
		
		final URLDecoder urlDecoder = new URLDecoder();
		try {
			this.name = urlDecoder.decode(this.name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
		
		final int lastDot = this.name.lastIndexOf('.');
		if(lastDot > 0) {
			this.name = this.name.substring(0, lastDot);
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
