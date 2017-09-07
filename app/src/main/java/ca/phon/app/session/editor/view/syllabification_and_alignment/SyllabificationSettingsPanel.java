/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
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
package ca.phon.app.session.editor.view.syllabification_and_alignment;

import java.awt.Component;
import java.util.*;

import javax.swing.*;

import com.jgoodies.forms.layout.*;

import ca.phon.session.*;
import ca.phon.syllabifier.*;
import ca.phon.util.Language;

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
