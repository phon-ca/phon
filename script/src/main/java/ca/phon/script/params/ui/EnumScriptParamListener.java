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

import ca.phon.script.params.EnumScriptParam;

import javax.swing.*;
import java.awt.event.*;

public class EnumScriptParamListener extends ScriptParamAction implements ItemListener {

	private static final long serialVersionUID = 286152629127143044L;

	public EnumScriptParamListener(EnumScriptParam param, String id) {
		super(param, id);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// not implemented
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		final JComboBox comboBox = JComboBox.class.cast(e.getSource());
		final int selectedIdx = comboBox.getSelectedIndex();
		final EnumScriptParam param = EnumScriptParam.class.cast(getScriptParam());
		final String paramId = param.getParamIds().iterator().next();
		if(selectedIdx >= 0 && selectedIdx < param.getChoices().length) {
			final Object choice = param.getChoices()[selectedIdx];
			param.setValue(paramId, choice);
		}
	}

}
