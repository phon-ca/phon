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

import javax.swing.*;

import ca.phon.script.params.*;

/**
 * Boolean script parameters
 */
public class BooleanScriptParamAction extends ScriptParamAction {

	public BooleanScriptParamAction(ScriptParam param, String id) {
		super(param, id);
	}

	private static final long serialVersionUID = -5924195124548378433L;

	@Override
	public void actionPerformed(ActionEvent e) {
		final JCheckBox checkBox = JCheckBox.class.cast(e.getSource());
		final ScriptParam param = getScriptParam();
		param.setValue(getParamId(), checkBox.isSelected());
	}
	
}
