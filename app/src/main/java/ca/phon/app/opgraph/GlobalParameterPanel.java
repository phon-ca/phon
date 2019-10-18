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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingContainer;

import ca.phon.ipa.Diacritic;
import ca.phon.ipa.IPAElementFactory;
import ca.phon.ipamap.IpaMap;
import ca.phon.ipamap2.DiacriticSelector;
import ca.phon.ipamap2.IPAMapGrid;
import ca.phon.ipamap2.IPAMapGridContainer;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.util.PrefHelper;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Edit global options add provide a button for
 * displaying advanced settings in the wizard 
 * content area.
 * 
 */
public class GlobalParameterPanel extends JPanel {
	
	private final static String USE_CASE_SENSITIVE_PROP =
			GlobalParameterPanel.class.getName() + ".useCaseSensitive";
	
	private final static String CASE_SENSITIVE_PROP = 
			GlobalParameterPanel.class.getName() + ".caseSensitive";
	
	private final static String USE_IGNORE_DIACRITICS_PROP =
			GlobalParameterPanel.class.getName() + ".useIgnoreDiacritics";
	
	private final static String IGNORE_DIACRITICS_PROP =
			GlobalParameterPanel.class.getName() + ".ignoreDiacritics";
	
	private final static String RETAIN_DIACRITICS_PROP =
			GlobalParameterPanel.class.getName() + ".retainDiacritics";
	
	private final static String USE_INVENTORY_GROUPING_PROP =
			GlobalParameterPanel.class.getName() + ".useInventoryGrouping";
	
	private final static String INVENTORY_GROUPING_PROP = 
			GlobalParameterPanel.class.getName() + ".inventoryGrouping";
	
	private JComboBox<String> caseSensitiveBox;
	
	private DropDownButton ignoreDiacriticsBtn;
	private JPopupMenu ignoreDiacriticsMenu;
	private boolean useIgnoreDiacritics = false;
	private boolean ignoreDiacritics = false;
	
	private DiacriticSelector diacriticSelector;

	private JComboBox<String> inventoryGroupingBox;
	
	public GlobalParameterPanel() {
		super();
		
		init();
		loadPreferences();
		updateButtons();
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
		caseSensitiveBox.setToolTipText("Override case sensitive setting for report.  Use 'default' to accept setting as configured in report.");
		add(caseSensitiveBox, gbc);

		
		++gbc.gridx;
		JLabel diaLbl = new JLabel("Ignore diacritics:");
		diaLbl.putClientProperty("JComponent.sizeVariant", "small");
		add(diaLbl, gbc);
		
		++gbc.gridx;
		
		ignoreDiacriticsMenu = new JPopupMenu();
		for(String itemTxt:comboBoxItems) {
			final PhonUIAction idAct = new PhonUIAction(this, "onIgnoreDiacriticsMenu", itemTxt);
			idAct.putValue(PhonUIAction.NAME, itemTxt);
			ignoreDiacriticsMenu.add(idAct);
		}
		ignoreDiacriticsMenu.addSeparator();
		
		diacriticSelector = new DiacriticSelector();
		ignoreDiacriticsMenu.add(diacriticSelector);
				
		PhonUIAction ignoreDiacriticsAct = new PhonUIAction(this, "noOp");
		ignoreDiacriticsAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		ignoreDiacriticsAct.putValue(PhonUIAction.NAME, "default");
		ignoreDiacriticsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "");
		ignoreDiacriticsAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("blank", IconSize.XSMALL));
		ignoreDiacriticsAct.putValue(DropDownButton.BUTTON_POPUP, ignoreDiacriticsMenu);
		
		ignoreDiacriticsBtn = new DropDownButton(ignoreDiacriticsAct);
		ignoreDiacriticsBtn.setOnlyPopup(true);
		ignoreDiacriticsBtn.setPreferredSize(ignoreDiacriticsBtn.getPreferredSize());
		ignoreDiacriticsBtn.setHorizontalTextPosition(SwingConstants.LEFT);
		ignoreDiacriticsBtn.setHorizontalAlignment(SwingConstants.LEFT);
		
		add(ignoreDiacriticsBtn, gbc);
		
		final PhonUIAction dropDownAct = new PhonUIAction(this, "noOp");
		dropDownAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("apps/preferences-desktop-font", IconSize.SMALL));
		dropDownAct.putValue(PhonUIAction.NAME, "Retain diacritics");
		dropDownAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select diacritics which will be retained when using the ignore diacritics setting");
		dropDownAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
		dropDownAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		
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
	
	private void updateButtons() {
		String ignoreDiacriticsVal = 
				(isUseGlobalIgnoreDiacritics() ? 
						(isIgnoreDiacritics() ? "yes" : "no") : "default");
		ignoreDiacriticsBtn.setText(ignoreDiacriticsVal);
	}
	
	/*
	 * Event handlers
	 */
	
	// ignore diacritics menu
	public void onIgnoreDiacriticsMenu(PhonActionEvent pae) {
		String value = pae.getData().toString();
		if("default".equals(value)) {
			useDefaultIgnoreDiacritics();
		} else if("yes".equals(value)) {
			this.useIgnoreDiacritics = true;
			this.ignoreDiacritics = true;
		} else if("no".equals(value)) {
			this.useIgnoreDiacritics = true;
			this.ignoreDiacritics = false;
		}
		updateButtons();
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
		return this.useIgnoreDiacritics;
	}
	
	public void useDefaultIgnoreDiacritics() {
		this.useIgnoreDiacritics = false;
		updateButtons();
	}

	public boolean isIgnoreDiacritics() {
		return this.ignoreDiacritics;
	}
	
	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		this.ignoreDiacritics = ignoreDiacritics;
		updateButtons();
	}
	
	public Set<Diacritic> getGlobalRetainDiacritics() {
		return diacriticSelector.getSelectedDiacritics();
	}
	
	public boolean isUseInventoryGrouping() {
		return this.inventoryGroupingBox.getSelectedIndex() > 0;
	}
	
	public void useDefaultInventoryGrouping() {
		inventoryGroupingBox.setSelectedIndex(0);
		updateButtons();
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
			
		case RETAIN_DIACRITICS_SET:
			return getGlobalRetainDiacritics();
	
		default:
			return null;
		}
	}
	
	private String getIPAMapSelectionString() {
		return diacriticSelector.getMapGrids().stream()
			.map( (grid) -> {
				String retVal = grid.getGrid().getName() + "(";
				
				for(int i = 0; i < grid.getSelectionModel().getSelectedItemsCount(); i++) {
					if(i > 0) retVal += ",";
					retVal += grid.getSelectionModel().getSelectedIndices()[i];
				}
				
				retVal += ")";
				return retVal;
			}).collect(Collectors.joining(";"));
	}
	
	private void loadIPAMapSelection(String selectionString) {
		String regex = "(\\w+)\\(([0-9,]+)\\)";
		Pattern p = Pattern.compile(regex);
		
		diacriticSelector.clearSelection();
		
		try(Scanner s1 = new Scanner(selectionString)) {
			String gridData = null;
			while((gridData = s1.findInLine(p)) != null) {
				Matcher m = p.matcher(gridData);
				if(m.matches()) {
					String gridName = m.group(1);
					String selectedIndices = m.group(2);
					
					var mapGridOpt = diacriticSelector.getMapGrids().stream()
							.filter( (grid) -> grid.getGrid().getName().contentEquals(gridName) )
							.findAny();
					
					if(mapGridOpt.isPresent()) {
						IPAMapGrid mapGrid = mapGridOpt.get();
						try(Scanner s2 = new Scanner(selectedIndices)) {
							s2.useDelimiter(",");
							while(s2.hasNextInt()) {
								int selectedIdx = s2.nextInt();
								
								mapGrid.getSelectionModel().addSelectionInterval(selectedIdx, selectedIdx);
							}
						}
					}
				}
			}
		}
	}
	
	public void savePreferences() {
		var userPrefs = PrefHelper.getUserPreferences();
		userPrefs.putBoolean(USE_CASE_SENSITIVE_PROP, isUseGlobalCaseSensitive());
		userPrefs.putBoolean(CASE_SENSITIVE_PROP, isCaseSensitive());
		
		userPrefs.putBoolean(USE_IGNORE_DIACRITICS_PROP, isUseGlobalIgnoreDiacritics());
		userPrefs.putBoolean(IGNORE_DIACRITICS_PROP, isIgnoreDiacritics());
		userPrefs.put(RETAIN_DIACRITICS_PROP, getIPAMapSelectionString());
		
		userPrefs.putBoolean(USE_INVENTORY_GROUPING_PROP, isUseInventoryGrouping());
		userPrefs.put(INVENTORY_GROUPING_PROP, getInventoryGrouping());
	}
	
	public void loadPreferences() {
		var userPrefs = PrefHelper.getUserPreferences();
		
		boolean useCaseSensitive = userPrefs.getBoolean(USE_CASE_SENSITIVE_PROP, false);
		if(useCaseSensitive) {
			setCaseSensitive(userPrefs.getBoolean(CASE_SENSITIVE_PROP, true));
		} else {
			useDefaultCaseSensitive();
		}
		
		boolean useIgnoreDiacritics = userPrefs.getBoolean(USE_IGNORE_DIACRITICS_PROP, false);
		if(useIgnoreDiacritics) {
			setIgnoreDiacritics(userPrefs.getBoolean(IGNORE_DIACRITICS_PROP, false));
		} else {
			useDefaultIgnoreDiacritics();
		}
		
		String retainDiacriticsSelection = userPrefs.get(RETAIN_DIACRITICS_PROP, "");
		loadIPAMapSelection(retainDiacriticsSelection);
		
		boolean useInventoryGrouping = userPrefs.getBoolean(USE_INVENTORY_GROUPING_PROP, false);
		if(useInventoryGrouping) {
			setInventoryGrouping(userPrefs.get(INVENTORY_GROUPING_PROP, "Session"));
		} else {
			useDefaultInventoryGrouping();
		}
	}
	
}
