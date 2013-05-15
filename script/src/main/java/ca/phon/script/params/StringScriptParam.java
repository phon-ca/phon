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

import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.phon.ui.PhonGuiConstants;
import ca.phon.ui.PromptedTextField;

public class StringScriptParam extends ScriptParam {
	
	/** The editor comp */
	private final PromptedTextField textField;
	
	private String _id;
	
	public StringScriptParam(String id, String desc, String defaultValue) {
		super();
		
		_id = id;
		setParamType("string");
		
		setParamDesc(desc);
		setValue(id, null);
		setDefaultValue(id, defaultValue);
		
		textField = new PromptedTextField();
		textField.setText((String)getDefaultValue(id));
		textField.setFont(Font.decode(PhonGuiConstants.DEFAULT_TRANSCRIPT_FONT));
		textField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent e) {
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				updateVal();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				updateVal();
			}
			
			private void updateVal() {
				setValue(_id, textField.getText());
			}
		});
	}

	@Override
	public JComponent getEditorComponent() {
		textField.setText(getValue(_id).toString());
		return textField;
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
}
