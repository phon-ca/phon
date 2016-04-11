package ca.phon.app.opgraph.nodes.query;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ca.phon.ui.text.PromptedTextField;

public class ColumnOptionsPanel extends JPanel {

	private static final long serialVersionUID = -2068888819093380149L;

	protected PromptedTextField nameField;
	protected JCheckBox caseSensitiveBox;
	protected JCheckBox ignoreDiacriticsBox;
	
	private boolean showOptions = true;
	
	public ColumnOptionsPanel() {
		this(true);
	}
	
	public ColumnOptionsPanel(boolean showOptions) {
		super();
		this.showOptions = showOptions;
		
		init();
	}
	
	protected void init() {
		final GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(2, 2, 5, 2);
		
		nameField = new PromptedTextField("Enter column names separated by ';'");
		caseSensitiveBox = new JCheckBox("Case sensitive");
		ignoreDiacriticsBox = new JCheckBox("Ignore diacritics");
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;
		add(nameField, gbc);
		
		++gbc.gridy;
		gbc.weighty = 1.0;
		gbc.weightx = 0.5;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		add(caseSensitiveBox, gbc);
		++gbc.gridx;
		add(ignoreDiacriticsBox, gbc);
	}

	public String getColumnNames() {
		return nameField.getText();
	}

	public void setColumnNames(String columnNames) {
		this.nameField.setText(columnNames);
	}

	public boolean isCaseSensitive() {
		return caseSensitiveBox.isSelected();
	}

	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitiveBox.setSelected(caseSensitive);
	}

	public boolean isIgnoreDiacritics() {
		return ignoreDiacriticsBox.isSelected();
	}

	public void setIgnoreDiacritics(boolean ignoreDiacritics) {
		this.ignoreDiacriticsBox.setSelected(ignoreDiacritics);
	}

	public boolean isShowOptions() {
		return showOptions;
	}

	public void setShowOptions(boolean showOptions) {
		this.showOptions = showOptions;
		this.caseSensitiveBox.setVisible(showOptions);
		this.ignoreDiacriticsBox.setVisible(showOptions);
	}
	
}
