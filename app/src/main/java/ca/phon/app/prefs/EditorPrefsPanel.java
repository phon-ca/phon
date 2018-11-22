/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.prefs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.ipadictionary.IPADictionaryLibrary;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.Language;
import ca.phon.util.PrefHelper;

/**
 * Panel for editing session editor prefs.
 *
 */
public class EditorPrefsPanel extends PrefsPanel {

	private static final long serialVersionUID = 5933816135903983293L;

	/*
	 * UI
	 */
	private JComboBox<Language> cmbDictionaryLanguage;
	private JComboBox<Syllabifier> cmbSyllabifierLanguage;

	private JComboBox<Integer> autosaveBox;
	private final Integer[] autosaveTimes = { 0, 5, 10, 15, 20, 30 }; // minutes

	private JCheckBox backupWhenSaveBox;

	public EditorPrefsPanel() {
		super("Session Editor");
		init();
	}

	private void init() {

		CellConstraints cc = new CellConstraints();

		final IPADictionaryLibrary dictLibrary = IPADictionaryLibrary.getInstance();

		final String dictLangPref = PrefHelper.get(PhonProperties.IPADICTIONARY_LANGUAGE,
				PhonProperties.DEFAULT_IPADICTIONARY_LANGUAGE);
		final Language dictLang = Language.parseLanguage(dictLangPref);
		
		Language langs[] = dictLibrary.availableLanguages().toArray(new Language[0]);
		Arrays.sort(langs, new LanguageComparator());
		
		cmbDictionaryLanguage = new JComboBox<>(langs);
		cmbDictionaryLanguage.setSelectedItem(dictLang);
		cmbDictionaryLanguage.addItemListener(new DictionaryLanguageListener());
		cmbDictionaryLanguage.setRenderer(new LanguageCellRenderer());

		JPanel jpanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jpanel1.setBorder(new TitledBorder("Dictionary Language"));
		jpanel1.add(cmbDictionaryLanguage);

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

		cmbSyllabifierLanguage = new JComboBox<>(sortedSyllabifiers.toArray(new Syllabifier[0]));
		cmbSyllabifierLanguage.setRenderer(new SyllabifierCellRenderer());
		if(defSyllabifier != null)
			cmbSyllabifierLanguage.setSelectedItem(defSyllabifier);
		cmbSyllabifierLanguage.addItemListener(new SyllabifierLanguageListener());

		JPanel jpanel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jpanel2.setBorder(new TitledBorder("Syllabifier Language"));
		jpanel2.add(cmbSyllabifierLanguage);

		autosaveBox = new JComboBox<>(autosaveTimes);

		final Integer autosavePref = PrefHelper.getInt(PhonProperties.AUTOSAVE_INTERVAL, PhonProperties.DEFAULT_AUTOSAVE_INTERVAL);
		autosaveBox.setSelectedItem(autosavePref);

		autosaveBox.addItemListener(new AutosaveTimeListener());
		autosaveBox.setRenderer(new AutosaveTimeRenderer());

		JPanel jpanel4 = new JPanel(new FlowLayout(FlowLayout.LEFT));

		jpanel4.add(autosaveBox);
		jpanel4.setBorder(BorderFactory.createTitledBorder("Autosave Sessions"));

		final PhonUIAction backupAct = new PhonUIAction(this, "toggleBackupWhenSave");
		backupAct.putValue(PhonUIAction.NAME, "Backup session file to <project>" + File.separator +
				"backups.zip when saving sessions.");
		backupAct.putValue(PhonUIAction.SELECTED_KEY, PrefHelper.getBoolean(SessionEditor.BACKUP_WHEN_SAVING, true));
		backupWhenSaveBox = new JCheckBox(backupAct);

		JPanel jpanel5 = new JPanel(new FlowLayout(FlowLayout.LEFT));

		jpanel5.add(backupWhenSaveBox);
		jpanel5.setBorder(BorderFactory.createTitledBorder("Backup Sessions"));

		JPanel innerPanel = new JPanel();
		FormLayout layout = new FormLayout(
				"fill:pref:grow",
				"pref, pref, pref, pref, pref");
		innerPanel.setLayout(layout);

		innerPanel.add(jpanel1, cc.xy(1,1));
		innerPanel.add(jpanel2, cc.xy(1,2));
		innerPanel.add(jpanel4, cc.xy(1, 4));
		innerPanel.add(jpanel5, cc.xy(1, 5));

		setLayout(new BorderLayout());
		JScrollPane innerScroller = new JScrollPane(innerPanel);
		add(innerScroller, BorderLayout.CENTER);
	}

	public void toggleBackupWhenSave() {
		PrefHelper.getUserPreferences().putBoolean(SessionEditor.BACKUP_WHEN_SAVING, backupWhenSaveBox.isSelected());
	}

	private class AutosaveTimeRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList arg0,
				Object arg1, int arg2, boolean arg3, boolean arg4) {
			JLabel retVal =
				(JLabel)super.getListCellRendererComponent(arg0, arg1, arg2, arg3, arg4);

			Integer val = (Integer)arg1;
			if(val == 0) {
				retVal.setText("Never");
			} else {
				retVal.setText("Every " + val + " minutes");
			}

			return retVal;
		}
	}

	private class AutosaveTimeListener implements ItemListener {

		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				Integer val = (Integer)autosaveBox.getSelectedItem();
				val = (val == null ? 0 : val);

				PrefHelper.getUserPreferences().putInt(PhonProperties.AUTOSAVE_INTERVAL, val);
			}
		}

	}

	private class DictionaryLanguageListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() != ItemEvent.SELECTED) return;

			Language dictLanguage = (Language)e.getItem();
			PrefHelper.getUserPreferences().put(PhonProperties.IPADICTIONARY_LANGUAGE, dictLanguage.toString());
		}
	}

	private class SyllabifierLanguageListener implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if(e.getStateChange() != ItemEvent.SELECTED) return;

			Syllabifier syllabifier = (Syllabifier)e.getItem();
			PrefHelper.getUserPreferences().put(PhonProperties.SYLLABIFIER_LANGUAGE, syllabifier.getLanguage().toString());
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

	private class SyllabifierComparator implements Comparator<Syllabifier> {

		@Override
		public int compare(Syllabifier o1, Syllabifier o2) {
			return o1.toString().compareTo(o2.toString());
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