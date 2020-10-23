package ca.phon.app.phonex;

import ca.phon.ui.ipa.*;

public class PhonexDebuggerIPADisplay extends SyllabificationDisplay {

	private int currentIndex = 0;
	
	public PhonexDebuggerIPADisplay() {
		super();
	}
	
	public int getCurrentIndex() {
		return this.currentIndex;
	}
	
	public void setCurrentIndex(int index) {
		this.currentIndex = index;
	}
	
}
