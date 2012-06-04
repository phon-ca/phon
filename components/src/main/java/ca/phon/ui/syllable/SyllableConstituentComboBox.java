package ca.phon.ui.syllable;

import javax.swing.JComboBox;

import ca.phon.syllable.SyllableConstituentType;

/**
 * Simple combo box for selecting a syllable constituen type.
 * 
 */
public class SyllableConstituentComboBox extends JComboBox {

	/**
	 * Constructor
	 */
	public SyllableConstituentComboBox() {
		super(SyllableConstituentType.values());
	}
	
}
