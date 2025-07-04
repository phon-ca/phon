package ca.phon.app.script;

import org.fife.ui.rsyntaxtextarea.*;

/**
 * RSyntaxTextArea with ability to control discarding of edits.
 *
 */
public class PhonScriptTextArea extends RSyntaxTextArea {

	private boolean allowDiscardEdits = true;

	public PhonScriptTextArea() {
	}

	public PhonScriptTextArea(RSyntaxDocument doc) {
		super(doc);
	}

	public PhonScriptTextArea(String text) {
		super(text);
	}

	public PhonScriptTextArea(int rows, int cols) {
		super(rows, cols);
	}

	public PhonScriptTextArea(String text, int rows, int cols) {
		super(text, rows, cols);
	}

	public PhonScriptTextArea(RSyntaxDocument doc, String text, int rows, int cols) {
		super(doc, text, rows, cols);
	}

	public PhonScriptTextArea(int textMode) {
		super(textMode);
	}

	public boolean isAllowDiscardEdits() {
		return allowDiscardEdits;
	}

	public void setAllowDiscardEdits(boolean allowDiscardEdits) {
		this.allowDiscardEdits = allowDiscardEdits;
	}

	@Override
	public void discardAllEdits() {
		if(isAllowDiscardEdits()) {
			super.discardAllEdits();
		}
	}

}
