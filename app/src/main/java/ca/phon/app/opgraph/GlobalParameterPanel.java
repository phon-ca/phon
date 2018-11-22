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
package ca.phon.app.opgraph;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
		gbc.insets = new Insets(0, 0, 0, 2);
		
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
	
	public void useDefaultCaseSensitive() {
		this.caseSensitiveBox.setSelectedIndex(0);
	}
	
	public boolean isCaseSensitive() {
		return this.caseSensitiveBox.getSelectedIndex() == 1;
	}
	
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitiveBox.setSelectedIndex( caseSensitive ? 1 : 2 );
	}

	public boolean isUseGlobalIgnoreDiacritics() {
		return this.ignoreDiacriticsBox.getSelectedIndex() > 0;
	}
	
	public void useDefaultIgnoreDiacritics() {
		this.ignoreDiacriticsBox.setSelectedIndex(0);
	}

	public boolean isIgnoreDiacritics() {
		return this.ignoreDiacriticsBox.getSelectedIndex() == 1;
	}
	
	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		this.ignoreDiacriticsBox.setSelectedIndex( ignoreDiacritics ? 1 : 2 );
	}
	
	public boolean isUseInventoryGrouping() {
		return this.inventoryGroupingBox.getSelectedIndex() > 0;
	}
	
	public void setInventoryGrouping(String grouping) {
		inventoryGroupingBox.setSelectedItem(grouping);
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