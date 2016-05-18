package ca.phon.app.opgraph.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXLabel;

import ca.phon.session.ParticipantRole;
import ca.phon.util.PrefHelper;

/**
 * Edit global options add provide a button for
 * displaying advanced settings in the wizard 
 * content area.
 * 
 */
public class WizardOptionsPanel extends JPanel {
	
	private final static String CASE_SENSITIVE_PROP = 
			WizardOptionsPanel.class.getName() + ".caseSensitive";
	
	private final static String IGNORE_DIACRITICS_PROP =
			WizardOptionsPanel.class.getName() + ".ignoreDiacritics";
	
	private final static String PARTICIPANT_ROLE_PROP =
			WizardOptionsPanel.class.getName() + ".participantRole";
	
	private JCheckBox caseSensitiveBox;
	
	private JCheckBox ignoreDiacriticsBox;
	
	private JComboBox<ParticipantRole> participantRoleBox;
	
	public WizardOptionsPanel() {
		super();
		
		init();
	}
	
	private void init() {
		final GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		
		caseSensitiveBox = new JCheckBox("Case sensitive");
		caseSensitiveBox.setSelected(PrefHelper.getBoolean(CASE_SENSITIVE_PROP, false));
		add(caseSensitiveBox, gbc);
		
		++gbc.gridy;
		ignoreDiacriticsBox = new JCheckBox("Ignore diacritics");
		ignoreDiacriticsBox.setSelected(PrefHelper.getBoolean(IGNORE_DIACRITICS_PROP, false));
		add(ignoreDiacriticsBox, gbc);
		
		++gbc.gridy;
		gbc.weightx = 0.0;
		gbc.gridwidth = 1;
		add(new JLabel("Participant Role:"), gbc);
		++gbc.gridx;
		gbc.weightx = 1.0;
		
		ParticipantRole selectedRole = 
				ParticipantRole.fromString(PrefHelper.get(PARTICIPANT_ROLE_PROP, "null"));
		participantRoleBox = new JComboBox<>(ParticipantRole.values());
		((DefaultComboBoxModel<ParticipantRole>)participantRoleBox.getModel()).insertElementAt(null, 0);
		participantRoleBox.setSelectedItem(selectedRole);
		participantRoleBox.setRenderer(new ParticipantCellRenderer());
		add(participantRoleBox, gbc);
	}
	
	public ParticipantRole getSelectedParticipantRole() {
		return (ParticipantRole)this.participantRoleBox.getSelectedItem();
	}
	
	public boolean isCaseSensitive() {
		return this.caseSensitiveBox.isSelected();
	}
	
	public boolean isIgnoreDiacritics() {
		return this.ignoreDiacriticsBox.isSelected();
	}
	
	private class ParticipantCellRenderer extends DefaultListCellRenderer {
		
	}

}
