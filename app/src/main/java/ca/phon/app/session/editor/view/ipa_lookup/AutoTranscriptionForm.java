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

package ca.phon.app.session.editor.view.ipa_lookup;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import ca.phon.app.session.RecordFilterPanel;
import ca.phon.project.Project;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.syllabifier.Syllabifier;
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

	private JCheckBox overwriteBox;
	
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
		
		overwriteBox = new JCheckBox("Overwrite");
		overwriteBox.setSelected(true);

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
	
		syllabifierBox = new JComboBox(sortedSyllabifiers.toArray(new Syllabifier[0]));
		syllabifierBox.setRenderer(new SyllabifierCellRenderer());
		if(defSyllabifier != null)
			syllabifierBox.setSelectedItem(defSyllabifier);

		FormLayout topLayout = new FormLayout(
				"right:pref, fill:pref:grow",
				"pref, pref, pref, pref, pref");
		JPanel topPanel = new JPanel();
		topPanel.setLayout(topLayout);

		topPanel.setBorder(BorderFactory.createTitledBorder("Tier Options"));

		topPanel.add(new JLabel("Transcribe:"), cc.xy(1,2));
		topPanel.add(overwriteBox, cc.xy(2,1));
		topPanel.add(setIPATargetBox, cc.xy(2,2));
		topPanel.add(setIPAActualBox, cc.xy(2,3));
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
			return o1.getLanguage().toString().compareTo(o2.getLanguage().toString());
		}
		
	}
	
	private class SyllabifierCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list,
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
