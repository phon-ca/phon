package ca.phon.app.opgraph;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.ipa.Diacritic;
import ca.phon.query.script.params.DiacriticOptionsPanel;
import ca.phon.query.script.params.DiacriticOptionsScriptParam;
import ca.phon.query.script.params.DiacriticOptionsScriptParam.SelectionMode;
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
		updateContentAndVisiblity();
	}
	
	private void init() {
		setLayout(new VerticalLayout());
		
		ImageIcon warningIcon = IconManager.getInstance().getIcon("categories/info-black", IconSize.SMALL);
		warningLabel = new JLabel("These settings override those configured the report.  They are not saved.");
		warningLabel.setIcon(warningIcon);
		add(warningLabel);
		
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
		this.overrideIgnoreDiacritics = overrideIgnoreDiacritics;
		updateContentAndVisiblity();
	}
	
	public boolean isIgnoreDiacritics() {
		return diacriticOptionsPanel.getDiacriticOptions().isIgnoreDiacritics();
	}
	
	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		diacriticOptionsPanel.getDiacriticOptions().setIgnoreDiacritics(ignoreDiacritics);
	}
	
	public boolean isOnlyOrExcept() {
		return diacriticOptionsPanel.getDiacriticOptions().getSelectionMode() == SelectionMode.ONLY;
	}
	
	public void setOnlyOrExcept(boolean onlyOrExcept) {
		diacriticOptionsPanel.getDiacriticOptions().setSelectionMode(onlyOrExcept ? SelectionMode.ONLY : SelectionMode.EXCEPT);
	}
	
	public Collection<Diacritic> getSelectedDiacritics() {
		return diacriticOptionsPanel.getDiacriticOptions().getSelectedDiacritics();
	}
	
	public void setSelectedDiacritics(Collection<Diacritic> selectedDiacritics) {
		diacriticOptionsPanel.getDiacriticOptions().setSelectedDiacritics(selectedDiacritics);
	}
	
}
