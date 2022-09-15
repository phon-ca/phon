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
package ca.phon.app.opgraph;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import org.jdesktop.swingx.*;

import ca.phon.ipa.*;
import ca.phon.query.script.params.*;
import ca.phon.query.script.params.DiacriticOptionsScriptParam.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.*;
import ca.phon.util.icons.*;

/**
 * UI for modifying OverrideParameter settings for a report/analysis.
 * 
 */
public final class OverrideParameterPanel extends JPanel {
	
	private boolean overrideCaseSensitive = false;
	private JCheckBox caseSensitiveBox;
	
	private boolean overrideGroupingColumn = false;
	private final String[] groupingColumnOptions = { "Age", "Session" };
	private JComboBox<String> groupingColumnBox;
	private JPanel groupingColumnPanel;
	
	private boolean overrideIgnoreDiacritics = false;
	private DiacriticOptionsPanel diacriticOptionsPanel;
	
	private JLabel warningLabel;

	public OverrideParameterPanel() {
		super();
		
		init();
		setBorder(BorderFactory.createTitledBorder("Overrides"));
		updateContentAndVisiblity();
	}
	
	private void init() {
		setLayout(new VerticalLayout());
		
		HidablePanel hidablePanel = new HidablePanel(OverrideParameterPanel.class.getName() + ".hidablePanel");
		hidablePanel.setLayout(new BorderLayout());
		ImageIcon warningIcon = IconManager.getInstance().getIcon("categories/info-black", IconSize.SMALL);
		warningLabel = new JLabel("<html><p>These settings override those configured in other forms.  They are not saved with the report/analysis.</p></html>");
		warningLabel.setIcon(warningIcon);
		hidablePanel.add(warningLabel, BorderLayout.CENTER);
		add(hidablePanel);
		
		groupingColumnBox = new JComboBox<String>(groupingColumnOptions);
		groupingColumnBox.setSelectedIndex(1);
		
		groupingColumnPanel = new JPanel(new HorizontalLayout());
		groupingColumnPanel.add(new JLabel("Inventory grouping column: "));
		groupingColumnPanel.add(groupingColumnBox);
		add(groupingColumnPanel);
		
		caseSensitiveBox = new JCheckBox("Case sensitive");
		caseSensitiveBox.setToolTipText("Override case sensitive setting where applicable");
		add(caseSensitiveBox);
		
		final DiacriticOptionsScriptParam diacriticOptions = new DiacriticOptionsScriptParam("", "", false, new ArrayList<>());
		diacriticOptionsPanel = new DiacriticOptionsPanel(diacriticOptions);
		add(diacriticOptionsPanel);
	}
	
	private void updateContentAndVisiblity() {
		boolean visible = (overrideCaseSensitive || overrideGroupingColumn || overrideIgnoreDiacritics);
		setVisible(visible);
		
		caseSensitiveBox.setVisible(overrideCaseSensitive);
		groupingColumnPanel.setVisible(overrideGroupingColumn);
		diacriticOptionsPanel.setVisible(overrideIgnoreDiacritics);
	}
	
	public boolean isOverrideCaseSensitive() {
		return this.overrideCaseSensitive;
	}
	
	public void setOverrideCaseSensitive(boolean overrideCaseSensitive) {
		setOverrideCaseSensitive(overrideCaseSensitive, isCaseSensitive());
	}
	
	public void setOverrideCaseSensitive(boolean overrideCaseSensitive, boolean caseSensitive) {
		this.overrideCaseSensitive = overrideCaseSensitive;
		setCaseSensitive(caseSensitive);
		updateContentAndVisiblity();
	}
	
	public boolean isCaseSensitive() {
		return caseSensitiveBox.isSelected();
	}
	
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitiveBox.setSelected(caseSensitive);
	}
	
	public boolean isOverrideInventoryGroupingColumn() {
		return this.overrideGroupingColumn;
	}
	
	public void setOverrideInventoryGroupingColumn(boolean overrideInventoryGrouping) {
		this.overrideGroupingColumn = overrideInventoryGrouping;
		updateContentAndVisiblity();
	}
	
	public String getInventoryGroupingColumn() {
		return groupingColumnBox.getSelectedItem().toString();
	}
	
	public void setInventoryGroupingColumn(String column) {
		groupingColumnBox.setSelectedItem(column);
	}
	
	public boolean isOverrideIgnoreDiacritics() {
		return this.overrideIgnoreDiacritics;
	}
	
	public void setOverrideIgnoreDiacritics(boolean overrideIgnoreDiacritics) {
		setOverrideIgnoreDiacritics(overrideIgnoreDiacritics, isIgnoreDiacritics());
	}
	
	public void setOverrideIgnoreDiacritics(boolean overrideIgnoreDiacritics, boolean ignoreDiacritics) {
		this.overrideIgnoreDiacritics = overrideIgnoreDiacritics;
		setIgnoreDiacritics(ignoreDiacritics);
		updateContentAndVisiblity();
	}
	
	public boolean isIgnoreDiacritics() {
		return diacriticOptionsPanel.getIgnoreDiacriticsBox().isSelected();
	}
	
	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		diacriticOptionsPanel.getIgnoreDiacriticsBox().setSelected(ignoreDiacritics);
	}
	
	public boolean isOnlyOrExcept() {
		return diacriticOptionsPanel.getSelectionModeBox().getSelectedItem() == SelectionMode.ONLY;
	}
	
	public void setOnlyOrExcept(boolean onlyOrExcept) {
		diacriticOptionsPanel.getSelectionModeBox().setSelectedItem(onlyOrExcept ? SelectionMode.ONLY : SelectionMode.EXCEPT);
	}
	
	public Collection<Diacritic> getSelectedDiacritics() {
		return diacriticOptionsPanel.getDiacriticSelector().getSelectedDiacritics();
	}
	
	public void setSelectedDiacritics(Collection<Diacritic> selectedDiacritics) {
		diacriticOptionsPanel.getDiacriticSelector().setSelectedDiacritics(selectedDiacritics);
	}
	
	/* Menu and actions */
	public void setupMenu(MenuBuilder menuBuilder) {
		JMenuItem overridesItem = new JMenuItem("-- Overrides --");
		overridesItem.setEnabled(false);
		menuBuilder.addItem(".", overridesItem);
		
		// inventory grouping
		PhonUIAction<String> defaultInventoryGroupingAct = PhonUIAction.eventConsumer(this::inventoryGroupingHandler, "default");
		defaultInventoryGroupingAct.putValue(PhonUIAction.NAME, "Don't override");
		defaultInventoryGroupingAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Don't override inventory grouping options for report");
		defaultInventoryGroupingAct.putValue(PhonUIAction.SELECTED_KEY, !isOverrideInventoryGroupingColumn());
		JCheckBoxMenuItem defaultInventoryGroupingItem = new JCheckBoxMenuItem(defaultInventoryGroupingAct);
		
		JMenu inventoryGroupingMenu = menuBuilder.addMenu(".", "Inventory Grouping");
		inventoryGroupingMenu.add(defaultInventoryGroupingItem);
		
		for(String inventoryGroupingOpt:groupingColumnOptions) {
			PhonUIAction<String> inventoryGroupingAct = PhonUIAction.eventConsumer(this::inventoryGroupingHandler, inventoryGroupingOpt);
			inventoryGroupingAct.putValue(PhonUIAction.NAME, inventoryGroupingOpt);
			inventoryGroupingAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override inventory grouping column options fro report");
			inventoryGroupingAct.putValue(PhonUIAction.SELECTED_KEY, isOverrideInventoryGroupingColumn() && getInventoryGroupingColumn().contentEquals(inventoryGroupingOpt));
			JCheckBoxMenuItem inventoryGroupingItem = new JCheckBoxMenuItem(inventoryGroupingAct);
			inventoryGroupingMenu.add(inventoryGroupingItem);
		}
		
		// case sensitive
		PhonUIAction<String> defaultCaseSensitiveAct = PhonUIAction.eventConsumer(this::caseSensitiveHandler, "default");
		defaultCaseSensitiveAct.putValue(PhonUIAction.NAME, "Don't override");
		defaultCaseSensitiveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Don't override case sensitive options for report");
		defaultCaseSensitiveAct.putValue(PhonUIAction.SELECTED_KEY, !isOverrideCaseSensitive());
		JCheckBoxMenuItem defaultCaseSensitiveItem = new JCheckBoxMenuItem(defaultCaseSensitiveAct);
		
		PhonUIAction<String> yesCaseSensitiveAct = PhonUIAction.eventConsumer(this::caseSensitiveHandler, "yes");
		yesCaseSensitiveAct.putValue(PhonUIAction.NAME, "yes");
		yesCaseSensitiveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override case sensitive options for report");
		yesCaseSensitiveAct.putValue(PhonUIAction.SELECTED_KEY, isOverrideCaseSensitive() && isIgnoreDiacritics());
		JCheckBoxMenuItem yesCaseSensitiveItem = new JCheckBoxMenuItem(yesCaseSensitiveAct);
		
		PhonUIAction<String> noCaseSensitiveAct = PhonUIAction.eventConsumer(this::caseSensitiveHandler, "no");
		noCaseSensitiveAct.putValue(PhonUIAction.NAME, "no");
		noCaseSensitiveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override case sensitive options for report");
		noCaseSensitiveAct.putValue(PhonUIAction.SELECTED_KEY, isOverrideCaseSensitive() && !isIgnoreDiacritics());
		JCheckBoxMenuItem noCaseSensitiveItem = new JCheckBoxMenuItem(noCaseSensitiveAct);
		
		JMenu caseSensitiveMenu = menuBuilder.addMenu(".", "Case sensitive");
		caseSensitiveMenu.add(defaultCaseSensitiveItem);
		caseSensitiveMenu.add(yesCaseSensitiveItem);
		caseSensitiveMenu.add(noCaseSensitiveItem);
		
		// ignore diacritics
		PhonUIAction<String> defaultIgnoreDiacriticsAct = PhonUIAction.eventConsumer(this::ignoreDiacriticsHandler, "default");
		defaultIgnoreDiacriticsAct.putValue(PhonUIAction.NAME, "Don't override");
		defaultIgnoreDiacriticsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Don't override ignore diacritics options for report");
		defaultIgnoreDiacriticsAct.putValue(PhonUIAction.SELECTED_KEY, !isOverrideIgnoreDiacritics());
		JCheckBoxMenuItem defaultIgnoreDiacriticsItem = new JCheckBoxMenuItem(defaultIgnoreDiacriticsAct);
		
		PhonUIAction<String> yesIgnoreDiacriticsAct = PhonUIAction.eventConsumer(this::ignoreDiacriticsHandler, "yes");
		yesIgnoreDiacriticsAct.putValue(PhonUIAction.NAME, "yes");
		yesIgnoreDiacriticsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override ignore diacritics options for report");
		yesIgnoreDiacriticsAct.putValue(PhonUIAction.SELECTED_KEY, isOverrideIgnoreDiacritics() && isIgnoreDiacritics());
		JCheckBoxMenuItem yesIgnoreDiacriticsItem = new JCheckBoxMenuItem(yesIgnoreDiacriticsAct);
		
		PhonUIAction<String> noIgnoreDiacriticsAct = PhonUIAction.eventConsumer(this::ignoreDiacriticsHandler, "no");
		noIgnoreDiacriticsAct.putValue(PhonUIAction.NAME, "no");
		noIgnoreDiacriticsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override ignore diacritics options for report");
		noIgnoreDiacriticsAct.putValue(PhonUIAction.SELECTED_KEY, isOverrideIgnoreDiacritics() && !isIgnoreDiacritics());
		JCheckBoxMenuItem noIgnoreDiacriticsItem = new JCheckBoxMenuItem(noIgnoreDiacriticsAct);
		
		JMenu ignoreDiacriticsMenu = menuBuilder.addMenu(".", "Ignore diacritics");
		ignoreDiacriticsMenu.add(defaultIgnoreDiacriticsItem);
		ignoreDiacriticsMenu.add(yesIgnoreDiacriticsItem);
		ignoreDiacriticsMenu.add(noIgnoreDiacriticsItem);
	}
	
	public void caseSensitiveHandler(PhonActionEvent<String> pae) {
		String v = pae.getData();
		
		if("yes".contentEquals(v)) {
			setOverrideCaseSensitive(true, true);
		} else if("no".contentEquals(v)) {
			setOverrideCaseSensitive(true, false);
		} else {
			setOverrideCaseSensitive(false);
		}
	}
	
	public void ignoreDiacriticsHandler(PhonActionEvent<String> pae) {
		String v = pae.getData();
		
		if("yes".contentEquals(v)) {
			setOverrideIgnoreDiacritics(true, true);
		} else if("no".contentEquals(v)) {
			setOverrideIgnoreDiacritics(true, false);
		} else {
			setOverrideIgnoreDiacritics(false);
		}
	}

	public void inventoryGroupingHandler(PhonActionEvent<String> pae) {
		String v = pae.getData();
		
		if("default".contentEquals(v)) {
			setOverrideInventoryGroupingColumn(false);
		} else {
			setOverrideInventoryGroupingColumn(true);
			setInventoryGroupingColumn(v);
		}
	}
	
}
