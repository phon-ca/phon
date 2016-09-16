/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXLabel;

import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
import ca.phon.session.ParticipantRole;
import ca.phon.util.PrefHelper;

/**
 * Edit global options add provide a button for
 * displaying advanced settings in the wizard 
 * content area.
 * 
 */
public class WizardGlobalOptionsPanel extends JPanel {
	
	private final static String CASE_SENSITIVE_PROP = 
			WizardGlobalOptionsPanel.class.getName() + ".caseSensitive";
	
	private final static String IGNORE_DIACRITICS_PROP =
			WizardGlobalOptionsPanel.class.getName() + ".ignoreDiacritics";
	
	private final static String PARTICIPANT_ROLE_PROP =
			WizardGlobalOptionsPanel.class.getName() + ".participantRole";
	
	private JCheckBox caseSensitiveBox;
	
	private JCheckBox ignoreDiacriticsBox;
	
	private JComboBox<ParticipantRole> participantRoleBox;
	
	private List<WizardGlobalOption> pluginGlobalOptions = new ArrayList<>();
	
	public WizardGlobalOptionsPanel() {
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
		
		// add global options
		final List<IPluginExtensionPoint<WizardGlobalOption>> pluginOptions =
				PluginManager.getInstance().getExtensionPoints(WizardGlobalOption.class);
		for(IPluginExtensionPoint<WizardGlobalOption> extPt:pluginOptions) {
			final WizardGlobalOption globalOption = extPt.getFactory().createObject();
			
			++gbc.gridy;
			gbc.gridx = 0;
			gbc.weightx = 1.0;
			gbc.gridwidth = 2;
			add(globalOption.getGlobalOptionsComponent(), gbc);
			this.pluginGlobalOptions.add(globalOption);
		}
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
	
	public List<WizardGlobalOption> getPluginGlobalOptions() {
		return this.pluginGlobalOptions;
	}
	
	private class ParticipantCellRenderer extends DefaultListCellRenderer {
		
	}

}
