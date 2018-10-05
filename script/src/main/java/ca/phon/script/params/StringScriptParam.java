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
