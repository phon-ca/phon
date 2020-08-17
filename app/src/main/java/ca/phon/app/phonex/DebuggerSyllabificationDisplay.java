package ca.phon.app.phonex;

import ca.phon.ui.ipa.SyllabificationDisplay;

public class DebuggerSyllabificationDisplay extends SyllabificationDisplay {

	private int debugIndex = -1;
	
	public DebuggerSyllabificationDisplay() {
		super();
		
		setUI(new DebuggerSyllabificationUI(this));
	}
	
	public int getDebugIndex() {
		return this.debugIndex;
	}
	
	public void setDebugIndex(int debugIndex) {
		int oldVal = this.debugIndex;
		this.debugIndex = debugIndex;
		firePropertyChange("debugIndex", oldVal, this.debugIndex);
	}
	
}
