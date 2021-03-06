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
package ca.phon.app.session.editor.view.syllabification_and_alignment;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.jgoodies.forms.layout.*;

import ca.phon.session.*;
import ca.phon.syllabifier.*;
import ca.phon.util.*;

public class SyllabificationSettingsPanel extends JPanel {
	
	private static final long serialVersionUID = -2762436673337886614L;

	private JComboBox<Syllabifier> targetSyllabifierBox;
	
	private JComboBox<Syllabifier> actualSyllabifierBox;
	
	private final SyllabifierInfo syllabifierInfo;

	public SyllabificationSettingsPanel(SyllabifierInfo info) {
		super();
		this.syllabifierInfo = info;
		
		init();
	}
	
	private void init() {
		final FormLayout layout = new FormLayout("right:pref, fill:pref:grow", "5dlu, pref, 3dlu, pref, 5dlu");
		setLayout(layout);
		final CellConstraints cc = new CellConstraints();
		
		final SyllabifierLibrary library = SyllabifierLibrary.getInstance();
		final Iterator<Syllabifier> syllabifiers = library.availableSyllabifiers();
		final List<Syllabifier> syllabifierList = new ArrayList<Syllabifier>();
		while(syllabifiers.hasNext()) syllabifierList.add(syllabifiers.next());
		Collections.sort(syllabifierList, new SyllabifierComparator());
		
		this.targetSyllabifierBox = new JComboBox<>(syllabifierList.toArray(new Syllabifier[0]));
		final Syllabifier selectedSyllabifier = library.getSyllabifierForLanguage(
				syllabifierInfo.getSyllabifierLanguageForTier(SystemTierType.IPATarget.getName()));
		this.targetSyllabifierBox.setSelectedItem(selectedSyllabifier);
		this.targetSyllabifierBox.setRenderer(new SyllabifierCellRenderer());
		
		this.actualSyllabifierBox = new JComboBox<>(syllabifierList.toArray(new Syllabifier[0]));
		this.actualSyllabifierBox.setSelectedItem(library.getSyllabifierForLanguage(
				syllabifierInfo.getSyllabifierLanguageForTier(SystemTierType.IPAActual.getName())));
		this.actualSyllabifierBox.setRenderer(new SyllabifierCellRenderer());
		
		add(new JLabel("Target syllabifier:"), cc.xy(1,2));
		add(targetSyllabifierBox, cc.xy(2,2));
		
		add(new JLabel("Actual syllabifier:"), cc.xy(1,4));
		add(actualSyllabifierBox, cc.xy(2,4));
	}

	public Language getSelectedTargetSyllabifier() {
		final Syllabifier syllabifier = (Syllabifier)targetSyllabifierBox.getSelectedItem();
		if(syllabifier != null) {
			return syllabifier.getLanguage();
		}
		return null;
	}
	
	public Language getSelectedActualSyllabifier() {
		final Syllabifier syllabifier = (Syllabifier)actualSyllabifierBox.getSelectedItem();
		if(syllabifier != null) {
			return syllabifier.getLanguage();
		}
		return null;
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
