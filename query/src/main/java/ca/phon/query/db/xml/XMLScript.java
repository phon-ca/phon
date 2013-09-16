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

package ca.phon.query.db.xml;

import java.util.HashMap;
import java.util.Map;

import ca.phon.query.db.Script;
import ca.phon.query.db.xml.io.query.ParamType;
import ca.phon.query.db.xml.io.query.ScriptType;


/**
 * An XML-based implementation of {@link Script}.
 */
public class XMLScript implements Script, JAXBWrapper<ScriptType> {
	/** JAXB object */
	ScriptType script;
	
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
		if(this.script.getSource() == null)
			this.script.setSource("");
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
		return script.getSource();
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
