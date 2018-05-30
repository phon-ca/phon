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
package ca.phon.app.opgraph;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import ca.phon.plugin.*;
import ca.phon.util.PrefHelper;

/**
 * Edit global options add provide a button for
 * displaying advanced settings in the wizard 
 * content area.
 * 
 */
public class GlobalParameterPanel extends JPanel {
	
	private final static String CASE_SENSITIVE_PROP = 
			GlobalParameterPanel.class.getName() + ".caseSensitive";
	
	private final static String IGNORE_DIACRITICS_PROP =
			GlobalParameterPanel.class.getName() + ".ignoreDiacritics";
	
	private final static String INVENTORY_GROUPING_PROP = 
			GlobalParameterPanel.class.getName() + ".inventoryGrouping";
	
	private JComboBox<String> caseSensitiveBox;
	
	private JComboBox<String> ignoreDiacriticsBox;
	
	private JComboBox<String> inventoryGroupingBox;
	
	public GlobalParameterPanel() {
		super();
		
		init();
	}
	
	private void init() {
		final GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		setOpaque(false);
		
		final GridBagConstraints gbc = new GridBagConstraints();
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
		
		JLabel csLbl = new JLabel("Case sensitive:");
		csLbl.putClientProperty("JComponent.sizeVariant", "small");
		add(csLbl, gbc);
		
		++gbc.gridx;
		caseSensitiveBox = new JComboBox<>(comboBoxItems);
		caseSensitiveBox.putClientProperty("JComboBox.isSquare", Boolean.TRUE);
		caseSensitiveBox.putClientProperty("JComponent.sizeVariant", "small");
		caseSensitiveBox.setSelectedItem(PrefHelper.get(CASE_SENSITIVE_PROP, comboBoxItems[0]));
		add(caseSensitiveBox, gbc);

		
		++gbc.gridx;
		JLabel diaLbl = new JLabel("Ignore diacritics:");
		diaLbl.putClientProperty("JComponent.sizeVariant", "small");
		add(diaLbl, gbc);
		
		++gbc.gridx;
		ignoreDiacriticsBox = new JComboBox<>(comboBoxItems);
		ignoreDiacriticsBox.putClientProperty("JComboBox.isSquare", Boolean.TRUE);
		ignoreDiacriticsBox.putClientProperty("JComponent.sizeVariant", "small");
		ignoreDiacriticsBox.setSelectedItem(PrefHelper.get(IGNORE_DIACRITICS_PROP, comboBoxItems[0]));
		add(ignoreDiacriticsBox, gbc);
		
		++gbc.gridx;
		JLabel grpingLbl = new JLabel("Inventory grouping:");
		grpingLbl.putClientProperty("JComponent.sizeVariant", "small");
		add(grpingLbl, gbc);
		
		final String groupingOptions[] = new String[] { "default", "Session", "Age" };
		++gbc.gridx;
		inventoryGroupingBox = new JComboBox<>(groupingOptions);
		inventoryGroupingBox.putClientProperty("JComboBox.isSquare", Boolean.TRUE);
		inventoryGroupingBox.putClientProperty("JComponent.sizeVariant", "small");
		inventoryGroupingBox.setSelectedItem(PrefHelper.get(INVENTORY_GROUPING_PROP, groupingOptions[0]));
		add(inventoryGroupingBox, gbc);
		
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
	
	public boolean isUseInventoryGrouping() {
		return this.inventoryGroupingBox.getSelectedIndex() > 0;
	}
	
	public String getInventoryGrouping() {
		return inventoryGroupingBox.getSelectedItem().toString();
	}
	
	public Object getValue(GlobalParameter param) {
		switch(param) {
		case CASE_SENSITIVE:
			return isCaseSensitive();
		
		case IGNORE_DIACRITICS:
			return isIgnoreDiacritics();
			
		case INVENTORY_GROUPING_COLUMN:
			return getInventoryGrouping();
	
		default:
			return null;
		}
	}

}
