/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
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
	
	public static final String TOOLTIP_TEXT_PROP = StringScriptParam.class.getName() + ".tooltipText";
	
	private String promptText = new String();
	
	private boolean validate = true;
	
	private boolean required = false;
	
	private String tooltipText = null;
	
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
		return (isRequired() ? getValue(getParamId()).toString().length() > 0 && validate : validate);
	}
	
	public boolean isRequired() {
		return this.required;
	}
	
	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setValidate(boolean validate) {
		boolean old = this.validate;
		this.validate = validate;
		super.propSupport.firePropertyChange(VALIDATE_PROP, old, this.validate);
	}
	
	public void setTooltipText(String tooltipText) {
		String oldVal = this.tooltipText;
		this.tooltipText = tooltipText;
		super.propSupport.firePropertyChange(TOOLTIP_TEXT_PROP, oldVal, this.tooltipText);
	}
	
	public String getTooltipText() {
		return this.tooltipText;
	}
	
}
