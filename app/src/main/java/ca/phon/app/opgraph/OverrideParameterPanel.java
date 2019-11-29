package ca.phon.app.opgraph;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.ipa.Diacritic;
import ca.phon.query.script.params.DiacriticOptionsPanel;
import ca.phon.query.script.params.DiacriticOptionsScriptParam;
import ca.phon.query.script.params.DiacriticOptionsScriptParam.SelectionMode;
import ca.phon.ui.HidablePanel;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * UI for modifying OverrideParamter settings for a report/analysis.
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
		PhonUIAction defaultInventoryGroupingAct = new PhonUIAction(this, "inventoryGroupingHandler", "default");
		defaultInventoryGroupingAct.putValue(PhonUIAction.NAME, "Don't override");
		defaultInventoryGroupingAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Don't override inventory grouping options for report");
		defaultInventoryGroupingAct.putValue(PhonUIAction.SELECTED_KEY, !isOverrideInventoryGroupingColumn());
		JCheckBoxMenuItem defaultInventoryGroupingItem = new JCheckBoxMenuItem(defaultInventoryGroupingAct);
		
		JMenu inventoryGroupingMenu = menuBuilder.addMenu(".", "Inventory Grouping");
		inventoryGroupingMenu.add(defaultInventoryGroupingItem);
		
		for(String inventoryGroupingOpt:groupingColumnOptions) {
			PhonUIAction inventoryGroupingAct = new PhonUIAction(this, "inventoryGroupingHandler", inventoryGroupingOpt);
			inventoryGroupingAct.putValue(PhonUIAction.NAME, inventoryGroupingOpt);
			inventoryGroupingAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override inventory grouping column options fro report");
			inventoryGroupingAct.putValue(PhonUIAction.SELECTED_KEY, isOverrideInventoryGroupingColumn() && getInventoryGroupingColumn().contentEquals(inventoryGroupingOpt));
			JCheckBoxMenuItem inventoryGroupingItem = new JCheckBoxMenuItem(inventoryGroupingAct);
			inventoryGroupingMenu.add(inventoryGroupingItem);
		}
		
		// case sensitive
		PhonUIAction defaultCaseSensitiveAct = new PhonUIAction(this, "caseSensitiveHandler", "default");
		defaultCaseSensitiveAct.putValue(PhonUIAction.NAME, "Don't override");
		defaultCaseSensitiveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Don't override case sensitive options for report");
		defaultCaseSensitiveAct.putValue(PhonUIAction.SELECTED_KEY, !isOverrideCaseSensitive());
		JCheckBoxMenuItem defaultCaseSensitiveItem = new JCheckBoxMenuItem(defaultCaseSensitiveAct);
		
		PhonUIAction yesCaseSensitiveAct = new PhonUIAction(this, "caseSensitiveHandler", "yes");
		yesCaseSensitiveAct.putValue(PhonUIAction.NAME, "yes");
		yesCaseSensitiveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override case sensitive options for report");
		yesCaseSensitiveAct.putValue(PhonUIAction.SELECTED_KEY, isOverrideCaseSensitive() && isIgnoreDiacritics());
		JCheckBoxMenuItem yesCaseSensitiveItem = new JCheckBoxMenuItem(yesCaseSensitiveAct);
		
		PhonUIAction noCaseSensitiveAct = new PhonUIAction(this, "caseSensitiveHandler", "no");
		noCaseSensitiveAct.putValue(PhonUIAction.NAME, "no");
		noCaseSensitiveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override case sensitive options for report");
		noCaseSensitiveAct.putValue(PhonUIAction.SELECTED_KEY, isOverrideCaseSensitive() && !isIgnoreDiacritics());
		JCheckBoxMenuItem noCaseSensitiveItem = new JCheckBoxMenuItem(noCaseSensitiveAct);
		
		JMenu caseSensitiveMenu = menuBuilder.addMenu(".", "Case sensitive");
		caseSensitiveMenu.add(defaultCaseSensitiveItem);
		caseSensitiveMenu.add(yesCaseSensitiveItem);
		caseSensitiveMenu.add(noCaseSensitiveItem);
		
		// ignore diacritics
		PhonUIAction defaultIgnoreDiacriticsAct = new PhonUIAction(this, "ignoreDiacriticsHandler", "default");
		defaultIgnoreDiacriticsAct.putValue(PhonUIAction.NAME, "Don't override");
		defaultIgnoreDiacriticsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Don't override ignore diacritics options for report");
		defaultIgnoreDiacriticsAct.putValue(PhonUIAction.SELECTED_KEY, !isOverrideIgnoreDiacritics());
		JCheckBoxMenuItem defaultIgnoreDiacriticsItem = new JCheckBoxMenuItem(defaultIgnoreDiacriticsAct);
		
		PhonUIAction yesIgnoreDiacriticsAct = new PhonUIAction(this, "ignoreDiacriticsHandler", "yes");
		yesIgnoreDiacriticsAct.putValue(PhonUIAction.NAME, "yes");
		yesIgnoreDiacriticsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override ignore diacritics options for report");
		yesIgnoreDiacriticsAct.putValue(PhonUIAction.SELECTED_KEY, isOverrideIgnoreDiacritics() && isIgnoreDiacritics());
		JCheckBoxMenuItem yesIgnoreDiacriticsItem = new JCheckBoxMenuItem(yesIgnoreDiacriticsAct);
		
		PhonUIAction noIgnoreDiacriticsAct = new PhonUIAction(this, "ignoreDiacriticsHandler", "no");
		noIgnoreDiacriticsAct.putValue(PhonUIAction.NAME, "no");
		noIgnoreDiacriticsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override ignore diacritics options for report");
		noIgnoreDiacriticsAct.putValue(PhonUIAction.SELECTED_KEY, isOverrideIgnoreDiacritics() && !isIgnoreDiacritics());
		JCheckBoxMenuItem noIgnoreDiacriticsItem = new JCheckBoxMenuItem(noIgnoreDiacriticsAct);
		
		JMenu ignoreDiacriticsMenu = menuBuilder.addMenu(".", "Ignore diacritics");
		ignoreDiacriticsMenu.add(defaultIgnoreDiacriticsItem);
		ignoreDiacriticsMenu.add(yesIgnoreDiacriticsItem);
		ignoreDiacriticsMenu.add(noIgnoreDiacriticsItem);
	}
	
	public void caseSensitiveHandler(PhonActionEvent pae) {
		String v = pae.getData().toString();
		
		if("yes".contentEquals(v)) {
			setOverrideCaseSensitive(true, true);
		} else if("no".contentEquals(v)) {
			setOverrideCaseSensitive(true, false);
		} else {
			setOverrideCaseSensitive(false);
		}
	}
	
	public void ignoreDiacriticsHandler(PhonActionEvent pae) {
		String v = pae.getData().toString();
		
		if("yes".contentEquals(v)) {
			setOverrideIgnoreDiacritics(true, true);
		} else if("no".contentEquals(v)) {
			setOverrideIgnoreDiacritics(true, false);
		} else {
			setOverrideIgnoreDiacritics(false);
		}
	}

	public void inventoryGroupingHandler(PhonActionEvent pae) {
		String v = pae.getData().toString();
		
		if("default".contentEquals(v)) {
			setOverrideInventoryGroupingColumn(false);
		} else {
			setOverrideInventoryGroupingColumn(true);
			setInventoryGroupingColumn(v);
		}
	}
	
}
