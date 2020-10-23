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

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import ca.phon.script.params.*;
import ca.phon.script.params.EnumScriptParam.*;

public class RadiobuttonEnumPanel extends JPanel {

	private EnumScriptParam param;

	private List<JRadioButton> buttons = new ArrayList<>();

	private ButtonGroup buttonGroup = new ButtonGroup();

	public RadiobuttonEnumPanel(EnumScriptParam param) {
		super();

		this.param = param;
		init();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		for(JRadioButton btn:buttons) {
			btn.setEnabled(enabled);
		}
	}

	private void init() {
		setOpaque(false);
		
		param.addPropertyChangeListener(param.getParamId(), paramListener);

		if(param.getColumns() == 0) {
			setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		} else {
			final GridLayout gl = new GridLayout(0, param.getColumns());
			setLayout(gl);
		}

		final String paramId = param.getParamId();
		for(int i = 0; i < param.getChoices().length; i++) {
			EnumScriptParam.ReturnValue choice = param.getChoices()[i];

			final JRadioButton btn = new JRadioButton(choice.toString());
			btn.setSelected(((EnumScriptParam.ReturnValue)param.getValue(paramId)).getIndex() == choice.getIndex());

			btn.addActionListener(new RadioButtonListener(param, choice));

			buttons.add(btn);
			buttonGroup.add(btn);
			add(btn);
		}
	}

	private class RadioButtonListener implements ActionListener {

		EnumScriptParam param;

		ReturnValue choice;

		public RadioButtonListener(EnumScriptParam param, ReturnValue choice) {
			this.param = param;
			this.choice = choice;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			param.setValue(param.getParamId(), choice);
		}

	}

	private PropertyChangeListener paramListener = new PropertyChangeListener() {

		@Override
		public void propertyChange(PropertyChangeEvent e) {
			final ReturnValue r = (ReturnValue)param.getValue(param.getParamId());
			if(r.getIndex() >= 0 && r.getIndex() < buttons.size()) {
				buttons.get(r.getIndex()).setSelected(true);
			}
		}

	};

}
