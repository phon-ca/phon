package ca.phon.app.syllabifier;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.util.Language;

public class SyllabifierComboBox extends JComboBox<Syllabifier> {
	
	public SyllabifierComboBox() {
		super();
		
		init();
	}
	
	private void init() {
		final SyllabifierLibrary syllabifierLibrary = SyllabifierLibrary.getInstance();

		final Language syllLangPref = syllabifierLibrary.defaultSyllabifierLanguage();

		Syllabifier defSyllabifier = null;
		final Iterator<Syllabifier> syllabifiers = syllabifierLibrary.availableSyllabifiers();
		List<Syllabifier> sortedSyllabifiers = new ArrayList<Syllabifier>();
		while(syllabifiers.hasNext()) {
			final Syllabifier syllabifier = syllabifiers.next();
			if(syllabifier.getLanguage().equals(syllLangPref))
				defSyllabifier = syllabifier;
			sortedSyllabifiers.add(syllabifier);
		}
		Collections.sort(sortedSyllabifiers, new SyllabifierComparator());
		sortedSyllabifiers.forEach(this::addItem);
		
		setRenderer(new SyllabifierCellRenderer());
		
		if(defSyllabifier != null)
			setSelectedItem(defSyllabifier);
	}

	private class SyllabifierComparator implements Comparator<Syllabifier> {

		@Override
		public int compare(Syllabifier o1, Syllabifier o2) {
			return o1.toString().toLowerCase().compareTo(o2.toString().toLowerCase());
		}

	}

	private class SyllabifierCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList list,
				Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			if(value != null) {
				final Syllabifier syllabifier = (Syllabifier)value;
				final String text = syllabifier.getName() + " (" + syllabifier.getLanguage().toString() + ")";
				retVal.setText(text);
			}

			return retVal;
		}

	}
	
}
