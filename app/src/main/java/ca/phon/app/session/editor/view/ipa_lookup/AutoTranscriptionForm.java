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

package ca.phon.app.session.editor.view.ipa_lookup;

import java.awt.Component;
import java.util.ArrayList;
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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import ca.phon.app.session.RecordFilterPanel;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.filter.RecordFilter;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.util.Language;

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

	private JComboBox<Syllabifier> syllabifierBox;

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
				"pref, pref, pref, pref, pref");
		JPanel topPanel = new JPanel();
		topPanel.setLayout(topLayout);

		topPanel.setBorder(BorderFactory.createTitledBorder("Tier Options"));

		topPanel.add(new JLabel("Transcribe:"), cc.xy(1,2));
		topPanel.add(overwriteBox, cc.xy(2,1));
		topPanel.add(setIPATargetBox, cc.xy(2,2));
		topPanel.add(setIPAActualBox, cc.xy(2,3));
		topPanel.add(new JLabel("Syllabifier:"), cc.xy(1, 4));
		topPanel.add(syllabifierBox, cc.xy(2, 4));

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
