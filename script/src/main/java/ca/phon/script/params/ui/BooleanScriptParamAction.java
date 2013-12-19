package ca.phon.script.params.ui;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;

import ca.phon.script.params.ScriptParam;

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
