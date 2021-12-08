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
package ca.phon.app.prefs;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.SessionMediaModel;
import ca.phon.app.syllabifier.SyllabifierComboBox;
import ca.phon.ipadictionary.*;
import ca.phon.ipadictionary.ui.IPADictionarySelector;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.Language;
import ca.phon.util.PrefHelper;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Panel for editing session editor prefs.
 *
 */
public class EditorPrefsPanel extends PrefsPanel {

	private static final long serialVersionUID = 5933816135903983293L;

	/*
	 * UI
	 */
	private IPADictionarySelector dictionarySelector;
	private SyllabifierComboBox cmbSyllabifierLanguage;

	private JComboBox<Integer> autosaveBox;
	private final Integer[] autosaveTimes = { 0, 5, 10, 15, 20, 30 }; // minutes

	private JCheckBox backupWhenSaveBox;
	
	private JCheckBox performMediaCheckBox;

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

		dictionarySelector = new IPADictionarySelector();
		dictionarySelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		dictionarySelector.setSelectedLang(dictLang, false);
		dictionarySelector.addListSelectionListener( (e) -> {
			IPADictionary dictLanguage = (IPADictionary) dictionarySelector.getSelectedValue();
			if(dictLanguage != null)
				PrefHelper.getUserPreferences().put(PhonProperties.IPADICTIONARY_LANGUAGE, dictLanguage.getLanguage().toString());
		});
		dictionarySelector.setVisibleRowCount(5);

		JPanel jpanel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jpanel1.setBorder(new TitledBorder("Dictionary Language"));
		jpanel1.add(new JScrollPane(dictionarySelector));
		SwingUtilities.invokeLater(() -> {
			dictionarySelector.ensureIndexIsVisible(dictionarySelector.getLanguageIndex(dictLang));
		});

		cmbSyllabifierLanguage = new SyllabifierComboBox();
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
		
		final PhonUIAction mediaCheckAct = new PhonUIAction(this, "toggleMediaCheck");
		mediaCheckAct.putValue(PhonUIAction.NAME, "Check file when loading session audio");
		mediaCheckAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Check audio file before loading to avoid crashes/freezing.  Turn off if problematic.");
		mediaCheckAct.putValue(PhonUIAction.SELECTED_KEY, PrefHelper.getBoolean(SessionMediaModel.PERFORM_MEDIA_CHECK_PROP, SessionMediaModel.DEFAULT_PERFORM_MEDIA_CHECK));
		performMediaCheckBox = new JCheckBox(mediaCheckAct);
		
		JPanel jpanel5 = new JPanel(new FlowLayout(FlowLayout.LEFT));

		jpanel5.add(backupWhenSaveBox);
		jpanel5.setBorder(BorderFactory.createTitledBorder("Backup Sessions"));

		JPanel jpanel6 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		jpanel6.add(performMediaCheckBox);
		jpanel6.setBorder(BorderFactory.createTitledBorder("Media"));
		
		JPanel innerPanel = new JPanel();
		FormLayout layout = new FormLayout(
				"fill:pref:grow",
				"pref, pref, pref, pref, pref, pref");
		innerPanel.setLayout(layout);

		innerPanel.add(jpanel1, cc.xy(1,1));
		innerPanel.add(jpanel2, cc.xy(1,2));
		innerPanel.add(jpanel4, cc.xy(1, 4));
		innerPanel.add(jpanel5, cc.xy(1, 5));
		innerPanel.add(jpanel6, cc.xy(1, 6));

		setLayout(new BorderLayout());
		JScrollPane innerScroller = new JScrollPane(innerPanel);
		add(innerScroller, BorderLayout.CENTER);
	}

	public void toggleBackupWhenSave() {
		PrefHelper.getUserPreferences().putBoolean(SessionEditor.BACKUP_WHEN_SAVING, backupWhenSaveBox.isSelected());
	}
	
	public void toggleMediaCheck() {
		PrefHelper.getUserPreferences().putBoolean(SessionMediaModel.PERFORM_MEDIA_CHECK_PROP, performMediaCheckBox.isSelected());
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

}
