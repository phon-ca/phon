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
package ca.phon.script.params.ui;

import ca.phon.script.params.ScriptParam;
import ca.phon.ui.text.PromptedTextField;
import ca.phon.ui.text.PromptedTextField.FieldState;

import javax.swing.event.*;
import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

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
