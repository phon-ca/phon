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
package ca.phon.app.opgraph.nodes.query;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
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
