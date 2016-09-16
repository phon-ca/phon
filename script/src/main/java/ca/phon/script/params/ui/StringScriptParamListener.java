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
package ca.phon.script.params.ui;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.phon.script.params.ScriptParam;
import ca.phon.ui.text.PromptedTextField;
import ca.phon.ui.text.PromptedTextField.FieldState;

public class StringScriptParamListener extends ScriptParamAction implements DocumentListener {
	
	private static final long serialVersionUID = 781017500275958557L;
	
	private WeakReference<PromptedTextField> textFieldRef;
	
	public StringScriptParamListener(ScriptParam param, String id, PromptedTextField textField) {
		super(param, id);
		this.textFieldRef = new WeakReference<PromptedTextField>(textField);
	}
	
	public PromptedTextField getTextField() {
		return textFieldRef.get();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		updateParam(getTextField());
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateParam(getTextField());
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateParam(getTextField());
	}
	
	private void updateParam(PromptedTextField textField) {
		if(textField.getState() != FieldState.PROMPT) {
			final String val = textField.getText();
			final String paramId = getParamId();
			final ScriptParam param = getScriptParam();
			param.setValue(paramId, val);
		}
	}

}
