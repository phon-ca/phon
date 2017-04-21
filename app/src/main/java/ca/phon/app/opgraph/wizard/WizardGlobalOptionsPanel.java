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
package ca.phon.app.opgraph.wizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;
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
	
	private JComboBox<String> caseSensitiveBox;
	
	private JComboBox<String> ignoreDiacriticsBox;
	
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
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.0;
		
		final String[] comboBoxItems = new String[] {
				"default",
				"yes",
				"no"
		};
		
		add(new JLabel("Case sensitive:"), gbc);
		++gbc.gridx;
		caseSensitiveBox = new JComboBox<>(comboBoxItems);
		caseSensitiveBox.setSelectedItem(PrefHelper.get(CASE_SENSITIVE_PROP, comboBoxItems[0]));
		add(caseSensitiveBox, gbc);

		++gbc.gridx;
		add(new JLabel("Ignore diacritics:"), gbc);
		++gbc.gridx;
		ignoreDiacriticsBox = new JComboBox<>(comboBoxItems);
		ignoreDiacriticsBox.setSelectedItem(PrefHelper.get(IGNORE_DIACRITICS_PROP, comboBoxItems[0]));
		add(ignoreDiacriticsBox, gbc);
		
		// add global options
		final List<IPluginExtensionPoint<WizardGlobalOption>> pluginOptions =
				PluginManager.getInstance().getExtensionPoints(WizardGlobalOption.class);
		for(IPluginExtensionPoint<WizardGlobalOption> extPt:pluginOptions) {
			final WizardGlobalOption globalOption = extPt.getFactory().createObject();
			
			++gbc.gridx;
			add(globalOption.getGlobalOptionsComponent(), gbc);
			this.pluginGlobalOptions.add(globalOption);
		}
		
		gbc.weightx = 1.0;
		++gbc.gridx;
		add(Box.createHorizontalGlue(), gbc);
	}
	
	public boolean isUseGlobalCaseSensitive() {
		return this.caseSensitiveBox.getSelectedIndex() > 0;
	}
	
	public boolean isCaseSensitive() {
		return this.caseSensitiveBox.getSelectedIndex() == 1;
	}

	public boolean isUseGlobalIgnoreDiacritics() {
		return this.ignoreDiacriticsBox.getSelectedIndex() > 0;
	}

	public boolean isIgnoreDiacritics() {
		return this.ignoreDiacriticsBox.getSelectedIndex() == 1;
	}
	
	public List<WizardGlobalOption> getPluginGlobalOptions() {
		return this.pluginGlobalOptions;
	}

}
