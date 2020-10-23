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
package ca.phon.script.params;

public class LabelScriptParam extends ScriptParam {
	
	public final static String LABEL_TEXT_PROP = LabelScriptParam.class.getName() + ".text";
	
	private String lblText = "";
	
	public LabelScriptParam(String labelText, String desc) {
		super();
		
		setParamType("label");
		lblText = labelText;
		setParamDesc(desc);
	}
	
	public String getText() {
		return lblText;
	}
	
	public void setText(String text) {
		final String oldVal = lblText;
		lblText = text;
		super.propSupport.firePropertyChange(LABEL_TEXT_PROP, oldVal, text);
	}

	@Override
	public String getStringRepresentation() {
		String retVal = "{";

		retVal += "label ,";
		retVal += "\"" + lblText + "\", ";
		retVal += "\"" + super.getParamDesc() + "\"";
		
		retVal += "}";

		return retVal;
	}
}
