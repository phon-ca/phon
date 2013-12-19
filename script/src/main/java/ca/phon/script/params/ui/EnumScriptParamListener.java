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
