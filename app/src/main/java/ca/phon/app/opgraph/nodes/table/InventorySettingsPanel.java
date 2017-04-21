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
package ca.phon.app.opgraph.nodes.table;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.opgraph.nodes.table.InventorySettings.ColumnInfo;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.text.PromptedTextField;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Settings for inventory.
 */
public class InventorySettingsPanel extends JPanel {

	private static final long serialVersionUID = -3897702215563994515L;

	private JButton addColumnButton;

	private ColumnPanel groupByPanel;

	private JPanel columnPanel;

	private InventorySettings settings;

	public InventorySettingsPanel(InventorySettings settings) {
		super();

		this.settings = settings;
		init();
	}

	private void init() {
		setLayout(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		final ImageIcon icon = IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final Action onAddAction = new PhonUIAction(this, "onAddColumn");
		onAddAction.putValue(Action.NAME, "Add");
		onAddAction.putValue(Action.SHORT_DESCRIPTION, "Add column to sort");
		onAddAction.putValue(Action.SMALL_ICON, icon);
		addColumnButton = new JButton(onAddAction);

		ColumnInfo groupBy = settings.getGroupBy();
		if(groupBy == null) {
			groupBy = new ColumnInfo();
			settings.setGroupBy(groupBy);
		}
		groupByPanel = new ColumnPanel(groupBy);
		groupByPanel.setBorder(BorderFactory.createTitledBorder("Group by"));

		columnPanel = new JPanel(new VerticalLayout());
		if(settings.getColumns().size() == 0) {
			settings.addColumn(new ColumnInfo());
		}
		int idx = 0;
		for(ColumnInfo info:settings.getColumns()) {
			final ColumnPanel panel = new ColumnPanel(info);
			if(idx++ > 0) {
				final JComponent sep = createSeparator(panel);
				columnPanel.add(sep);
			}
			columnPanel.add(panel);
		}

		final JPanel btmPanel = new JPanel(new VerticalLayout());
		btmPanel.setBorder(BorderFactory.createTitledBorder("Columns"));
		btmPanel.add(columnPanel);
		btmPanel.add(ButtonBarBuilder.buildOkBar(addColumnButton));

		add(groupByPanel, gbc);
		++gbc.gridy;
		add(btmPanel, gbc);
		++gbc.gridy;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		add(Box.createVerticalGlue(), gbc);
	}

	private JComponent createSeparator(ColumnPanel colPanel) {
		final ImageIcon removeIcon =
				IconManager.getInstance().getDisabledIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction removeAct = new PhonUIAction(this, "onRemoveColumn", colPanel);
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove sort column");
		removeAct.putValue(PhonUIAction.SMALL_ICON, removeIcon);
		final JButton removeButton = new JButton(removeAct);
		removeButton.setBorderPainted(false);

		final JPanel sep = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(0, 0, 0, 0);

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		sep.add(new JSeparator(SwingConstants.HORIZONTAL), gbc);

		gbc.weightx = 0.0;
		++gbc.gridx;
		sep.add(removeButton, gbc);

		colPanel.setSeparator(sep);

		return sep;
	}

	public void onAddColumn() {
		final ColumnInfo sc = new ColumnInfo();
		settings.getColumns().add(sc);
		final ColumnPanel scPanel = new ColumnPanel(sc);
		final JComponent sep = createSeparator(scPanel);
		columnPanel.add(sep);
		columnPanel.add(scPanel);
		revalidate();
	}

	public void onRemoveColumn(ColumnPanel scPanel) {
		columnPanel.remove(scPanel);
		if(scPanel.getSeparator() != null)
			columnPanel.remove(scPanel.getSeparator());
		settings.getColumns().remove(scPanel.getColumnInfo());
		revalidate();
	}

	private class ColumnPanel extends JPanel {

		private PromptedTextField nameField;

		private JCheckBox caseSensitiveBox;

		private JCheckBox ignoreDiacriticsBox;

		private InventorySettings.ColumnInfo info;

		private JComponent separator;

		public ColumnPanel(ColumnInfo info) {
			super();

			this.info = info;
			init();
		}

		public void setSeparator(JComponent sep) {
			this.separator = sep;
		}

		public JComponent getSeparator() {
			return this.separator;
		}

		public ColumnInfo getColumnInfo() {
			return this.info;
		}

		private void init() {
			setLayout(new GridBagLayout());
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.EAST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(2, 2, 5, 2);

			gbc.gridheight = 1;
			gbc.gridwidth = 2;
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.weightx = 1.0;
			nameField = new PromptedTextField("Enter column name or number");
			if(info.getName().trim().length() > 0) {
				nameField.setText(info.getName());
			}
			nameField.getDocument().addDocumentListener(new DocumentListener() {

				@Override
				public void removeUpdate(DocumentEvent e) {
					updateColumn();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					updateColumn();
				}

				@Override
				public void changedUpdate(DocumentEvent e) {
					// TODO Auto-generated method stub

				}
			});

			add(nameField, gbc);

			gbc.gridx = 0;
			gbc.gridy++;
			gbc.gridwidth = 1;
			gbc.weightx = 0.0;
			caseSensitiveBox = new JCheckBox("Case sensitive");
			caseSensitiveBox.setSelected(info.caseSensitive);
			caseSensitiveBox.addChangeListener( (e) -> info.setCaseSensitive(caseSensitiveBox.isSelected()) );
			add(caseSensitiveBox, gbc);

			gbc.gridx++;
			ignoreDiacriticsBox = new JCheckBox("Ignore diacritics");
			ignoreDiacriticsBox.setSelected(info.ignoreDiacritics);
			ignoreDiacriticsBox.addChangeListener( (e) -> info.setIgnoreDiacritics(ignoreDiacriticsBox.isSelected()) );
			add(ignoreDiacriticsBox, gbc);
		}

		private void updateColumn() {
			info.setName(nameField.getText());
		}
	}

}
