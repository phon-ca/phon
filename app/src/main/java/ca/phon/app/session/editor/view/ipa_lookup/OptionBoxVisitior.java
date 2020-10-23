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

import java.awt.event.*;

import javax.swing.*;

import ca.phon.app.ipalookup.*;
import ca.phon.app.session.editor.view.common.*;
import ca.phon.orthography.*;
import ca.phon.ui.fonts.*;
import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

public class OptionBoxVisitior extends VisitorAdapter<OrthoElement> {
	
	private final RecordLookupPanel recordLookupPanel;
	
	private final TierDataLayoutPanel groupPanel;

	int row;
	
	int col;
	
	int wordIdx = 1;
	
	private boolean lblAdded = false;
	
	public OptionBoxVisitior(RecordLookupPanel lookupPanel, TierDataLayoutPanel groupPanel, int row) {
		super();
		this.recordLookupPanel = lookupPanel;
		this.groupPanel = groupPanel;
		this.row = row;
		col = 0;
	}

	@Override
	public void fallbackVisit(OrthoElement obj) {
	}
	
	@Visits
	public void visitWord(OrthoWord word) {
		final JLabel orthoLabel = new JLabel(word.getWord());
		orthoLabel.setFont(FontPreferences.getTierFont());
		groupPanel.add(orthoLabel, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+col, row));
		
		final JSeparator wordSep = new JSeparator(SwingConstants.VERTICAL);
		groupPanel.add(wordSep, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+col+1, row));
		
		final OrthoWordIPAOptions opts = word.getExtension(OrthoWordIPAOptions.class);
		final String[] options = (opts != null ? opts.getOptions().toArray(new String[0]) : new String[0]);
		
		if(options.length > 0) addLabel();
		
		final ButtonGroup bg = new ButtonGroup();
		int myCol = col;
		for(int i = 0; i < options.length; i++) {
			final String opt = options[i];
			final JRadioButton btn = new JRadioButton(opt);
			btn.setFont(FontPreferences.getTierFont());
			btn.setOpaque(false);
			btn.setBorderPainted(false);
			if(opts.getSelectedOption() == i) 
				btn.setSelected(true);
			
			btn.addActionListener(new OptionListener(opts, i));
			
			groupPanel.add(btn, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+myCol, row+i+1));
			
			final JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
			groupPanel.add(sep, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+myCol+1, row+i+1));
			
			
			bg.add(btn);
		}
		col += 2;
		wordIdx++;
	}
	
	@Visits
	public void visitCompoundWord(OrthoWordnet word) {
		final JLabel orthoLabel = new JLabel(word.text());
		groupPanel.add(orthoLabel, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+col, row));
		
		final JSeparator wordSep = new JSeparator(SwingConstants.VERTICAL);
		groupPanel.add(wordSep, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+col+1, row));
		
		final OrthoWordIPAOptions opts1 = word.getWord1().getExtension(OrthoWordIPAOptions.class);
		final OrthoWordIPAOptions opts2 = word.getWord2().getExtension(OrthoWordIPAOptions.class);
		
		final String[] options1 = (opts1 != null ? opts1.getOptions().toArray(new String[0]) : new String[0]);
		final String[] options2 = (opts2 != null ? opts2.getOptions().toArray(new String[0]) : new String[0]);
		
		if(options1.length * options2.length > 0) addLabel();
		
		final ButtonGroup bg = new ButtonGroup();
		int myCol = col;
		int idx = 0;
		for(int i = 0; i < options1.length; i++) {
			final String opt1 = options1[i];
			for(int j = 0; j < options2.length; j++) {
				final String opt2 = options2[j];
				final String opt = opt1 + word.getMarker().toString() + opt2;
				
				final JRadioButton btn = new JRadioButton(opt);
				btn.setOpaque(false);
				btn.setBorderPainted(false);
				
				if( (opts1 != null && opts1.getSelectedOption() == i)
						&& (opts2 != null && opts2.getSelectedOption() == j) ) {
					btn.setSelected(true);
				}
				
				btn.addActionListener(new CompoundOptionsListener(opts1, i, opts2, j));
				
				groupPanel.add(btn, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+myCol, row+idx+1));
				
				final JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
				groupPanel.add(sep, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+myCol+1, row+idx+1));
				
				bg.add(btn);
				++idx;
			}
		}
		col += 2;
		wordIdx++;
	}
	
	private void addLabel() {
		if(lblAdded) return;
		
		final JLabel orthoLbl = new JLabel("Orthography");
		orthoLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		groupPanel.add(orthoLbl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, row));
		
		final JLabel ipaLbl = new JLabel("IPA");
		ipaLbl.setHorizontalAlignment(SwingConstants.RIGHT);
		groupPanel.add(ipaLbl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, row+1));
		
		lblAdded = true;
	}
	
	private class OptionListener implements ActionListener {
		
		private final OrthoWordIPAOptions opts;
		
		private final int option;

		public OptionListener(OrthoWordIPAOptions opts, int option) {
			super();
			this.opts = opts;
			this.option = option;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final JRadioButton btn = (JRadioButton)e.getSource();
			if(btn.isSelected()) {
				opts.setSelectedOption(option);
				recordLookupPanel.updateLookupTier();
			}
		}
		
	}
	
	private class CompoundOptionsListener implements ActionListener {
		
		private final OrthoWordIPAOptions opts1;
		private final int selectedOpt1;
		
		private final OrthoWordIPAOptions opts2;
		private final int selectedOpt2;
		
		public CompoundOptionsListener(OrthoWordIPAOptions opts1, int o1,
				OrthoWordIPAOptions opts2, int o2) {
			super();
			this.opts1 = opts1;
			this.selectedOpt1 = o1;
			this.opts2 = opts2;
			this.selectedOpt2 = o2;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			final JRadioButton btn = (JRadioButton)e.getSource();
			if(btn.isSelected()) {
				opts1.setSelectedOption(selectedOpt1);
				opts2.setSelectedOption(selectedOpt2);
				recordLookupPanel.updateLookupTier();
			}
		}
		
	}
	
	
//	private class OrthoWordItemListener implements ItemListener {
//		
//		private final OrthoWordIPAOptions opts;
//		
//		public OrthoWordItemListener(OrthoWordIPAOptions opts) {
//			super();
//			this.opts = opts;
//		}
//
//		@Override
//		public void itemStateChanged(ItemEvent e) {
//			if(e.getStateChange() == ItemEvent.SELECTED) {
//				final JComboBox box = (JComboBox)e.getSource();
//				opts.setSelectedOption(box.getSelectedIndex());
//				recordLookupPanel.updateLookupTier();
//			}
//		}
//		
//	}
}
