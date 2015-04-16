/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
