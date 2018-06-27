package ca.phon.script.params.ui;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import ca.phon.script.params.PatternScriptParam;
import ca.phon.script.params.ScriptParam;

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
