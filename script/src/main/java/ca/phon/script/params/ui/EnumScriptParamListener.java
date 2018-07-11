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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JComboBox;

import ca.phon.script.params.EnumScriptParam;

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
