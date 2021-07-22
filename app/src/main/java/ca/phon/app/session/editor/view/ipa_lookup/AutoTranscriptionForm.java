/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.phon.app.session.editor.view.ipa_lookup;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import ca.phon.ipadictionary.IPADictionaryLibrary;
import com.jgoodies.forms.layout.*;

import ca.phon.app.session.*;
import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.session.filter.*;
import ca.phon.syllabifier.*;
import ca.phon.util.*;
import org.jdesktop.swingx.VerticalLayout;

/**
 * Form for selection options when performing automatic
 * transcription.
 *
 * Options include:
 *  * whether to set IPA Target/Actual ( isSetIPATarget() and isSetIPAActual() )
 *  * dictionary to use for lookups
 *  * syllabifier to use
 *  * record filter to use for selecting which records to process
 */
public class AutoTranscriptionForm extends JPanel {

	/* UI Components */
	private RecordFilterPanel filterPanel;

	private JCheckBox setIPATargetBox;

	private JCheckBox setIPAActualBox;

	private JCheckBox overwriteBox;

	private JComboBox<Language> dictionaryLanguageBox;

	private JComboBox<Syllabifier> syllabifierBox;

	public AutoTranscriptionForm() {
		this(null, null);
	}

	public AutoTranscriptionForm(Project project) {
		this(project, null);
	}

	public AutoTranscriptionForm(Project project, Session session) {
		super();

		init(project, session);
	}

	private void init(Project project, Session session) {
		setLayout(new VerticalLayout());

		Set<Language> langs = IPADictionaryLibrary.getInstance().availableLanguages();
		Language langArray[] = langs.toArray(new Language[0]);
		Arrays.sort(langArray, new LanguageComparator());
		final Language defLang = IPADictionaryLibrary.getInstance().getDefaultLanguage();
		dictionaryLanguageBox = new JComboBox<>(langArray);
		dictionaryLanguageBox.setRenderer(new LanguageCellRenderer());
		dictionaryLanguageBox.setSelectedItem(defLang);

		overwriteBox = new JCheckBox("Overwrite existing data");
		overwriteBox.setSelected(false);

		setIPATargetBox = new JCheckBox("IPA Target");
		setIPATargetBox.setSelected(true);

		setIPAActualBox = new JCheckBox("IPA Actual");
		setIPAActualBox.setSelected(true);

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

		syllabifierBox = new JComboBox<>(sortedSyllabifiers.toArray(new Syllabifier[0]));
		syllabifierBox.setRenderer(new SyllabifierCellRenderer());
		if(defSyllabifier != null)
			syllabifierBox.setSelectedItem(defSyllabifier);

		FormLayout topLayout = new FormLayout(
				"right:pref, fill:pref:grow",
				"pref, pref, pref, pref, pref, pref");
		CellConstraints cc = new CellConstraints();
		JPanel topPanel = new JPanel();
		topPanel.setLayout(topLayout);

		topPanel.setBorder(BorderFactory.createTitledBorder("Tier Options"));

		topPanel.add(new JLabel("IPA Dictionary:"), cc.xy(1, 1));
		topPanel.add(this.dictionaryLanguageBox, cc.xy(2, 1));
		topPanel.add(new JLabel("Transcribe:"), cc.xy(1,2));
		topPanel.add(setIPATargetBox, cc.xy(2,2));
		topPanel.add(setIPAActualBox, cc.xy(2,3));
		topPanel.add(overwriteBox, cc.xy(2,4));
		topPanel.add(new JLabel("Syllabifier:"), cc.xy(1, 5));
		topPanel.add(syllabifierBox, cc.xy(2, 5));

		if(project != null && session != null) {
			filterPanel = new RecordFilterPanel(project, session);
			filterPanel.setBorder(BorderFactory.createTitledBorder("Record Selection"));
		}

		add(topPanel);
		if(project != null && session != null) {
			add(filterPanel);
		}
	}

	public Language getDictionaryLanguage() {
		return (Language) this.dictionaryLanguageBox.getSelectedItem();
	}

	public void setDictionaryLanguage(Language lang) {
		this.dictionaryLanguageBox.setSelectedItem(lang);
	}

	public boolean isSetIPATarget() {
		return setIPATargetBox.isSelected();
	}

	public boolean isSetIPAActual() {
		return setIPAActualBox.isSelected();
	}

	public Syllabifier getSyllabifier() {
		return (Syllabifier)syllabifierBox.getSelectedItem();
	}

	public RecordFilter getRecordFilter() {
		return filterPanel.getRecordFilter();
	}

	public boolean isOverwrite() {
		return overwriteBox.isSelected();
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

	private class LanguageComparator implements Comparator<Language> {

		@Override
		public int compare(Language o1, Language o2) {
			String l1 = o1.getPrimaryLanguage().getName() + " (" + o1.toString() + ")";
			String l2 = o2.getPrimaryLanguage().getName() + " (" + o2.toString() + ")";
			return l1.compareTo(l2);
		}

	}

	private class LanguageCellRenderer extends DefaultListCellRenderer {

		private static final long serialVersionUID = -5753923740573333306L;

		@Override
		public Component getListCellRendererComponent(JList list,
		                                              Object value, int index, boolean isSelected,
		                                              boolean cellHasFocus) {
			final JLabel retVal = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
					cellHasFocus);

			if(value != null) {
				final Language lang = (Language)value;
				final String text = lang.getPrimaryLanguage().getName() + " (" + lang.toString() + ")";
				retVal.setText(text);
			}

			return retVal;
		}

	}
}
