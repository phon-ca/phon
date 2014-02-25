package ca.phon.script.params.ui;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.phon.script.params.ScriptParam;
import ca.phon.ui.PromptedTextField;
import ca.phon.ui.PromptedTextField.FieldState;

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
		if(textField.getState() == FieldState.INPUT) {
			final String val = textField.getText();
			final String paramId = getParamId();
			final ScriptParam param = getScriptParam();
			param.setValue(paramId, val);
		}
	}

}
