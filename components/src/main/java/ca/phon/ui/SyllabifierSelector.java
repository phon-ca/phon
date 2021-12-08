package ca.phon.ui;

import ca.phon.syllabifier.*;
import ca.phon.util.Language;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Liset selection component for {@link Syllabifier}s
 */
public class SyllabifierSelector extends JList<Syllabifier> {

	private List<Syllabifier> availableSyllabifiers;

	public SyllabifierSelector() {
		super();

		init();
	}

	private void init() {
		Iterator<Syllabifier> syllabifierItr = SyllabifierLibrary.getInstance().availableSyllabifiers();
		List<Syllabifier> syllabifierList = new ArrayList<>();
		while(syllabifierItr.hasNext()) {
			syllabifierList.add(syllabifierItr.next());
		}
		Collections.sort(syllabifierList, Comparator.comparing(Syllabifier::getName));
		availableSyllabifiers = syllabifierList;

		setCellRenderer(new SyllabifierCellRenderer());
		setModel(new SyllabifierSelectorListModel());
	}

	public int getLanguageIndex(Language lang) {
		List<Language> languages = availableSyllabifiers.stream().map(Syllabifier::getLanguage).collect(Collectors.toList());
		return languages.indexOf(lang);
	}

	public void setSelectedLanguage(Language lang) {
		setSelectedIndex(getLanguageIndex(lang));
	}

	public void setSelectedSyllabifier(Syllabifier syllabifier) {
		setSelectedLanguage(syllabifier.getLanguage());
	}

	public Syllabifier getSelectedSyllabifier() {
		return super.getSelectedValue();
	}

	private class SyllabifierSelectorListModel extends AbstractListModel<Syllabifier> {

		@Override
		public int getSize() {
			return availableSyllabifiers.size();
		}

		@Override
		public Syllabifier getElementAt(int index) {
			return availableSyllabifiers.get(index);
		}

	}

	private class SyllabifierCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if(value instanceof Syllabifier) {
				Syllabifier syllabifier = (Syllabifier) value;
				retVal.setText(syllabifier.getName() + " (" + syllabifier.getLanguage().toString() + ")");
			}

			return retVal;
		}
	}

}
