package ca.phon.app.session.check;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import ca.phon.session.check.CheckTranscripts;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.SyllabifierLibrary;
import ca.phon.util.Language;

public class CheckTranscriptsUI extends JPanel implements SessionCheckUI {
	
	private final CheckTranscripts check;
	
	private JCheckBox resetSyllabificationBox;
	private JComboBox<Syllabifier> syllabifierBox;
	
	public CheckTranscriptsUI(CheckTranscripts check) {
		super();
		
		this.check = check;
		
		init();
	}
	
	private void init() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		
		resetSyllabificationBox = new JCheckBox("Reset syllabification");
		resetSyllabificationBox.setSelected(check.isResetSyllabification());
		resetSyllabificationBox.addActionListener( (e) -> {
			check.setResetSyllabification(resetSyllabificationBox.isSelected());
			syllabifierBox.setEnabled(resetSyllabificationBox.isSelected());
		});
		add(resetSyllabificationBox, gbc);
		
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
		syllabifierBox.addItemListener( (e) -> check.setSyllabifierLang(
				((Syllabifier)syllabifierBox.getSelectedItem()).getLanguage().toString()) );
		syllabifierBox.setEnabled(resetSyllabificationBox.isSelected());
		
		gbc.gridx++;
		gbc.insets = new Insets(0, 5, 0, 0);
		add(new JLabel("Language:"), gbc);
		gbc.gridx++;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 0, 0);
		add(syllabifierBox, gbc);
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
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		resetSyllabificationBox.setEnabled(enabled);
		syllabifierBox.setEnabled(enabled && resetSyllabificationBox.isSelected());
	}

	@Override
	public Component getComponent() {
		return this;
	}

}