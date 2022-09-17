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

import ca.phon.script.params.MultiboolScriptParam;

import javax.swing.*;
import java.awt.*;
import java.beans.*;

/**
 *
 */
public class MultiboolPanel extends JPanel {

	private static final long serialVersionUID = -4838747187986688335L;

	private final JCheckBox[] checkboxes;

	private final MultiboolScriptParam param;

	public MultiboolPanel(MultiboolScriptParam param) {
		super();
		this.param = param;
		this.checkboxes = new JCheckBox[param.getNumberOfOptions()];
		init();
		param.addPropertyChangeListener(listener);
	}

	private void init() {
		setOpaque(false);
		
		if(param.getCols() == 0) {
			setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		} else {
			final GridLayout gl = new GridLayout(0, param.getCols());
			setLayout(gl);
		}

		for(int i = 0; i < param.getNumberOfOptions(); i++) {
			final String paramId = param.getOptionId(i);
			final BooleanScriptParamAction action =
					new BooleanScriptParamAction(param, paramId);
			action.putValue(ScriptParamAction.NAME, param.getOptionText(paramId));
			action.putValue(ScriptParamAction.SELECTED_KEY,
					(param.getValue(paramId) != null ? (Boolean)param.getValue(paramId) : param.getDefaultValue(paramId)));

			final JCheckBox checkBox = new JCheckBox(action);
			add(checkBox);

			checkBox.setEnabled(param.isEnabled(i));
			checkBox.setVisible(param.isVisible(i));

			param.addPropertyChangeListener(paramId, (e) -> {
				checkBox.setSelected(Boolean.parseBoolean(param.getValue(paramId).toString()));
			});

			checkboxes[i] = checkBox;
		}
	}



	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for(int i = 0; i < checkboxes.length; i++) {
			checkboxes[i].setEnabled(enabled && param.isEnabled(i));
		}
	}

	private final PropertyChangeListener listener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			final String propName = evt.getPropertyName();
			final int lastDot = propName.lastIndexOf('.');
			final String id = (lastDot > 0 ? propName.substring(0, lastDot) : propName);
			final int optIdx = param.getOptionIndex(id);
			if(optIdx < 0) return;
			if(propName.endsWith(".enabled")) {
				checkboxes[optIdx].setEnabled(param.isEnabled(optIdx));
			} else if(propName.endsWith(".visible")) {
				checkboxes[optIdx].setVisible(param.isVisible(optIdx));
			}
		}

	};

}
