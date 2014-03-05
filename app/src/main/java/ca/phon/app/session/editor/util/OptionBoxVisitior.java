package ca.phon.app.session.editor.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import ca.phon.app.session.editor.tier.layout.TierDataConstraint;
import ca.phon.app.session.editor.tier.layout.TierDataLayoutPanel;
import ca.phon.orthography.OrthoElement;
import ca.phon.orthography.OrthoWord;
import ca.phon.orthography.OrthoWordnet;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

public class OptionBoxVisitior extends VisitorAdapter<OrthoElement> {
	
	private final RecordLookupPanel recordLookupPanel;
	
	private final TierDataLayoutPanel groupPanel;

	int row;
	
	int col;
	
	int wordIdx = 1;
	
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
		groupPanel.add(orthoLabel, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+col, row));
		
		final JSeparator wordSep = new JSeparator(SwingConstants.VERTICAL);
		groupPanel.add(wordSep, new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+col+1, row));
		
		final OrthoWordIPAOptions opts = word.getExtension(OrthoWordIPAOptions.class);
		final String[] options = (opts != null ? opts.getOptions().toArray(new String[0]) : new String[0]);
		
		final ButtonGroup bg = new ButtonGroup();
		int myCol = col;
		for(int i = 0; i < options.length; i++) {
			final String opt = options[i];
			final JRadioButton btn = new JRadioButton(opt);
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
//		final JLabel lbl = new JLabel("Word #" + wordIdx++);
//		lbl.setHorizontalAlignment(SwingConstants.RIGHT);
//		this.recordLookupPanel.add(lbl, new TierDataConstraint(TierDataConstraint.TIER_LABEL_COLUMN, row));
//		
//		final OrthoWord w1 = word.getWord1();
//		final OrthoWordIPAOptions opts1 = w1.getExtension(OrthoWordIPAOptions.class);
//		final OrthoWord w2 = word.getWord2();
//		final OrthoWordIPAOptions opts2 = w2.getExtension(OrthoWordIPAOptions.class);
//		
//		final String[] opts = new String[opts1.getOptions().size() * opts2.getOptions().size()];
//		int i = 0;
//		for(String o1:opts1.getOptions()) {
//			for(String o2:opts2.getOptions()) {
//				final String opt = o1 + word.getMarker().toString() + o2;
//				opts[i++] = opt;
//			}
//		}
//		
//		final JComboBox box = new JComboBox(opts);
//		final TierDataConstraint tierDataConstraint = new TierDataConstraint(TierDataConstraint.GROUP_START_COLUMN+col, row++);
//		this.recordLookupPanel.add(box, tierDataConstraint);
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
