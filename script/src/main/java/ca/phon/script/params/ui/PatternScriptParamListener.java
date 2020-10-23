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
package ca.phon.script.params.ui;

import java.awt.event.*;
import java.lang.ref.*;

import javax.swing.event.*;

import org.fife.ui.rsyntaxtextarea.*;

import ca.phon.script.params.*;

public class PatternScriptParamListener extends ScriptParamAction implements DocumentListener {

	private WeakReference<RSyntaxTextArea> textAreaRef;
	
	public PatternScriptParamListener(PatternScriptParam param, String id, RSyntaxTextArea textArea) {
		super(param, id);
		
		this.textAreaRef = new WeakReference<RSyntaxTextArea>(textArea);
	}
	
	public RSyntaxTextArea getTextArea() {
		return this.textAreaRef.get();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		updateParam(getTextArea());
	}
	
	private void updateParam(RSyntaxTextArea textArea) {
		final String val = getTextArea().getText();
		final String paramId = getParamId();
		final ScriptParam param = getScriptParam();
		param.setValue(paramId, val);
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		final PatternScriptParam param = (PatternScriptParam)getScriptParam();
		final RSyntaxTextArea textArea = getTextArea();
		
		updateParam(textArea);
		
		int lc = textArea.getLineCount();
		int numVisibleLines = Math.min(param.getMaxRows(), Math.max(lc, param.getMinRows()));
		param.setVisibleRows(numVisibleLines);
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		final PatternScriptParam param = (PatternScriptParam)getScriptParam();
		final RSyntaxTextArea textArea = getTextArea();
		
		updateParam(textArea);
		
		int lc = textArea.getLineCount();
		int numVisibleLines = Math.min(param.getMaxRows(), Math.max(lc, param.getMinRows()));
		param.setVisibleRows(numVisibleLines);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		
	}

}
