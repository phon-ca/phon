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
package ca.phon.script.params;

/**
 * String entry for script parameters.
 */
public class StringScriptParam extends ScriptParam {
	
	/**
	 * Property for prompt text
	 */
	public static final String PROMPT_PROP = StringScriptParam.class.getName() + ".promptText";
	
	public static final String VALIDATE_PROP = StringScriptParam.class.getName() + ".validate";
	
	private String promptText = new String();
	
	private boolean validate = true;
	
	/**
	 * Constructor
	 * @param id
	 * @param desc
	 * @param defaultValue
	 */
	public StringScriptParam(String id, String desc, String defaultValue) {
		super();
		
		setParamType("string");
		
		setParamDesc(desc);
		setValue(id, null);
		setDefaultValue(id, defaultValue);
	}

	@Override
	public String getStringRepresentation() {
		String retVal = "{";

		String id = super.getParamIds().iterator().next();
		retVal += "string, ";
		retVal += id + ", ";
		retVal += "\"" + super.getDefaultValue(id) + "\", ";
		retVal += "\"" + super.getParamDesc() + "\"";
		
		retVal += "}";

		return retVal;
	}
	
	public void setPrompt(String text) {
		String oldText = promptText;
		promptText = text;
		super.propSupport.firePropertyChange(PROMPT_PROP, oldText, text);
	}
	
	public String getPrompt() {
		return this.promptText;
	}

	public boolean isValidate() {
		return validate;
	}

	public void setValidate(boolean validate) {
		boolean old = this.validate;
		this.validate = validate;
		super.propSupport.firePropertyChange(VALIDATE_PROP, old, this.validate);
	}
	
	
	
}
