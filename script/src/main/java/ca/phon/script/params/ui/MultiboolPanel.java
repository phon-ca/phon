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

import java.awt.GridLayout;
import java.beans.*;

import javax.swing.*;

import ca.phon.script.params.MultiboolScriptParam;

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
		final GridLayout gl = new GridLayout(0, param.getCols());
		setLayout(gl);
		
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
