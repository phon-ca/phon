package ca.phon.app.opgraph.nodes.query;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.phon.ui.text.PromptedTextField;

/**
 * Settings for inventory.
 */
public class InventorySettingsPanel extends JPanel {
	
	private static final long serialVersionUID = -3897702215563994515L;

	private PromptedTextField groupByField;
	
	private PromptedTextField columnsField;
	
	private JCheckBox caseSensitiveBox;
	
	private JCheckBox ignoreDiacriticsBox;
	
	public InventorySettingsPanel() {
		super();
		
		init();
	}
	
	private void init() {
		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 5, 2);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1.0;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		
		final JLabel groupingLabel = new JLabel("Group by column:");
		add(groupingLabel, gbc);
		
		groupByField = new PromptedTextField("Enter column name or number");
		gbc.gridy++;
		add(groupByField, gbc);
		
		final JLabel columnsLabel = new JLabel("Include columns:");
		gbc.gridy++;
		add(columnsLabel, gbc);
		
		columnsField = new PromptedTextField("Enter column names/numbers separated by ';'");
		gbc.gridy++;
		add(columnsField, gbc);
		
		caseSensitiveBox = new JCheckBox("Case sensitive");
		gbc.gridy++;
		add(caseSensitiveBox, gbc);
		
		ignoreDiacriticsBox = new JCheckBox("Ignore diacritics");
		gbc.gridy++;
		add(ignoreDiacriticsBox, gbc);
	}
	
	public boolean isCaseSensitive() {
		return this.caseSensitiveBox.isSelected();
	}
	
	public void setCaseSensitive(boolean cs) {
		this.caseSensitiveBox.setSelected(cs);
	}
	
	public boolean isIgnoreDiacritics() {
		return this.ignoreDiacriticsBox.isSelected();
	}
	
	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		this.ignoreDiacriticsBox.setSelected(ignoreDiacritics);
	}
	
	public String getGroupingBy() {
		return this.groupByField.getText();
	}
	
	public void setGroupingBy(String groupBy) {
		this.groupByField.setText(groupBy);
	}
	
	public String getColumns() {
		return this.columnsField.getText();
	}
	
	public void setColumns(String cols) {
		this.columnsField.setText(cols);
	}

}
