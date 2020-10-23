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
package ca.phon.app.opgraph.nodes.query;

import java.awt.*;

import javax.swing.*;

import ca.phon.ui.text.*;

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
