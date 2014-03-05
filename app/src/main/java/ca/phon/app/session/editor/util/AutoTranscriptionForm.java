/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2008 The Phon Project, Memorial University <http://phon.ling.mun.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.phon.app.session.editor.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.phon.app.session.RecordFilterPanel;
import ca.phon.project.Project;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.util.Language;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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

//	private JComboBox dictBox;

	private JComboBox syllabifierBox;

	public AutoTranscriptionForm(Project project, Session session) {
		super();

		init(project, session);
	}

	private void init(Project project, Session t) {
		FormLayout layout = new FormLayout(
				"fill:pref:grow",
				"pref, pref");
		setLayout(layout);
		CellConstraints cc = new CellConstraints();

		setIPATargetBox = new JCheckBox("IPA Target");
		setIPATargetBox.setSelected(true);
//		PhonUIAction toggleSetIPATargetAct =
//				new PhonUIAction(this, "toggleIPABox", setIPATargetBox);
//		toggleSetIPATargetAct.putValue(PhonUIAction.NAME, "Auto transcribe IPA Target");
//		setIPATargetBox.setAction(toggleSetIPATargetAct);

		setIPAActualBox = new JCheckBox("IPA Actual");
//		PhonUIAction toggleSetIPAActualAct =
//				new PhonUIAction(this, "toggleIPABox", setIPAActualBox);
//		toggleSetIPAActualAct.putValue(PhonUIAction.NAME, "Auto transcribe IPA Actual");
//		setIPAActualBox.setAction(toggleSetIPAActualAct);

//		dictBox = new JComboBox(IPADictionary.getAvailableLanguages());
//		dictBox.setSelectedItem(IPADictionary.getDefaultLanguage());
//		PhonUIAction selectDictionaryAct =
//				new PhonUIAction(this, "selectDictionary");
//		dictBox.setAction(selectDictionaryAct);

		Set<Language> syllabifiers = SyllabifierLibrary.getInstance().availableSyllabifierLanguages();
//		Collections.sort(syllabifiers);
		syllabifierBox = new JComboBox(syllabifiers.toArray(new String[0]));
		syllabifierBox.setSelectedItem(SyllabifierLibrary.getInstance().defaultSyllabifierLanguage());
//		syllabifierBox.setSelectedItem(SyllabifierLibrary.getInstance().
//		PhonUIAction selectSyllabifierAct =
//				new PhonUIAction(this, "selectSyllabifier");
//		syllabifierBox.setAction(selectSyllabifierAct);

		FormLayout topLayout = new FormLayout(
				"right:pref, fill:pref:grow",
				"pref, pref, pref, pref");
		JPanel topPanel = new JPanel();
		topPanel.setLayout(topLayout);

		topPanel.setBorder(BorderFactory.createTitledBorder("Tier Options"));

		topPanel.add(new JLabel("Transcribe:"), cc.xy(1,1));
		topPanel.add(setIPATargetBox, cc.xy(2,1));
		topPanel.add(setIPAActualBox, cc.xy(2,2));
//		topPanel.add(new JLabel("IPA Dicitonary:"), cc.xy(1,3));
//		topPanel.add(dictBox, cc.xy(2, 3));
		topPanel.add(new JLabel("Syllabifier:"), cc.xy(1, 3));
		topPanel.add(syllabifierBox, cc.xy(2, 3));

		filterPanel = new RecordFilterPanel(project, t);
		filterPanel.setBorder(BorderFactory.createTitledBorder("Record Selection"));

		add(topPanel, cc.xy(1, 1));
		add(filterPanel, cc.xy(1, 2));
	}

	public boolean isSetIPATarget() {
		return setIPATargetBox.isSelected();
	}

	public boolean isSetIPAActual() {
		return setIPAActualBox.isSelected();
	}

	public String getSyllabifier() {
		return syllabifierBox.getSelectedItem().toString();
	}

	public RecordFilter getRecordFilter() {
		return filterPanel.getRecordFilter();
	}
}
