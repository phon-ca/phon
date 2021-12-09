package ca.phon.ipadictionary.ui;

import ca.phon.ipadictionary.IPADictionary;

import javax.swing.*;
import java.awt.*;

public class IPADictionaryCellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		if (value instanceof IPADictionary) {
			IPADictionary dict = (IPADictionary) value;
			retVal.setText(dict.getName() + " (" + dict.getLanguage().toString() + ")");
		}

		return retVal;
	}

}
