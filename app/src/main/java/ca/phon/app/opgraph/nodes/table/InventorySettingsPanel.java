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
package ca.phon.app.opgraph.nodes.table;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.VerticalLayout;

import ca.phon.app.opgraph.nodes.table.InventorySettings.ColumnInfo;
import ca.phon.query.script.params.DiacriticOptionsPanel;
import ca.phon.query.script.params.DiacriticOptionsScriptParam;
import ca.phon.query.script.params.DiacriticOptionsScriptParam.SelectionMode;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.text.PromptedTextField;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Settings for inventory.
 */
public class InventorySettingsPanel extends JPanel {

	private static final long serialVersionUID = -3897702215563994515L;
	
	TitledPanel autoConfigPanel;
	TitledPanel manualConfigPanel;
	
	private ButtonGroup configTypeGroup;
	private JRadioButton autoConfigBtn;
	private JRadioButton manualConfigBtn;
	private JCheckBox autoGroupBox;
	
	private ButtonGroup groupByGroup;
	private JRadioButton groupByAgeBtn;
	private JRadioButton groupBySessionBtn;
	
//	private JCheckBox ignoreDiacriticsBox;
	private DiacriticOptionsPanel diacriticOptionsPanel;
	private JCheckBox caseSensitiveBox;
	private JCheckBox includeGroupDataBox;
	private JCheckBox includeWordDataBox;
	private JCheckBox includeMetadataBox;

	private ColumnPanel groupByPanel;
	private JButton addColumnButton;
	private JPanel columnPanel;

	// model
	private InventorySettings settings;

	public InventorySettingsPanel(InventorySettings settings) {
		super();

		this.settings = settings;
		init();
	}
	
	private void init() {
		configTypeGroup = new ButtonGroup();
		
		// auto config options
		autoConfigBtn = new JRadioButton("Automatic Configuration");
		autoConfigBtn.setOpaque(false);
		configTypeGroup.add(autoConfigBtn);
		autoConfigPanel = new TitledPanel("");
		autoConfigPanel.setLeftDecoration(autoConfigBtn);
		autoConfigBtn.setSelected(settings.isConfigureAutomatically());
		
		groupByGroup = new ButtonGroup();
		groupByAgeBtn = new JRadioButton("Age");
		groupByAgeBtn.setSelected(settings.getAutoGroupingColumn().equals("Age"));
		groupByAgeBtn.setEnabled(settings.isAutoGrouping());
		groupByGroup.add(groupByAgeBtn);
		
		groupBySessionBtn = new JRadioButton("Session");
		groupBySessionBtn.setSelected(settings.getAutoGroupingColumn().equals("Session"));
		groupBySessionBtn.setEnabled(settings.isAutoGrouping());
		groupByGroup.add(groupBySessionBtn);
		
		autoGroupBox = new JCheckBox("Group by:");
		autoGroupBox.addActionListener( (e) -> {
			groupByAgeBtn.setEnabled(autoGroupBox.isSelected());
			groupBySessionBtn.setEnabled(autoGroupBox.isSelected());
			settings.setAutoGrouping(autoGroupBox.isSelected());
		});
		autoGroupBox.setSelected(settings.isAutoGrouping());

		final ActionListener groupByListener = (e) -> {
			if(groupByAgeBtn.isSelected())
				settings.setAutoGroupingColumn("Age");
			else if(groupBySessionBtn.isSelected())
				settings.setAutoGroupingColumn("Session");
		};
		groupByAgeBtn.addActionListener(groupByListener);
		groupBySessionBtn.addActionListener(groupByListener);
		
		final JPanel autoGroupingPanel = new JPanel(new HorizontalLayout());
		autoGroupingPanel.add(autoGroupBox);
		autoGroupingPanel.add(groupByAgeBtn);
		autoGroupingPanel.add(groupBySessionBtn);
		
		includeMetadataBox = new JCheckBox("Include metadata");
		includeMetadataBox.setSelected(settings.isIncludeMetadata());
		includeMetadataBox.addActionListener( (e) -> settings.setIncludeMetadata(includeMetadataBox.isSelected()) );
		
		includeGroupDataBox = new JCheckBox("Include additional group data");
		includeGroupDataBox.setSelected(settings.isIncludeAdditionalGroupData());
		includeGroupDataBox.addActionListener( (e) -> settings.setIncludeAdditionalGroupData(includeGroupDataBox.isSelected()) );
		
		includeWordDataBox = new JCheckBox("Include additional word data");
		includeWordDataBox.setSelected(settings.isIncludeAdditionalWordData());
		includeWordDataBox.addActionListener( (e) -> settings.setIncludeAdditionalWordData(includeWordDataBox.isSelected()) );
		
		final DiacriticOptionsScriptParam diacriticOptions = new DiacriticOptionsScriptParam("", "", false, new ArrayList<>());
		diacriticOptions.addPropertyChangeListener( (e) -> {
			if(("." + DiacriticOptionsScriptParam.IGNORE_DIACRITICS_PARAM).contentEquals(e.getPropertyName())) {
				settings.setIgnoreDiacritics(diacriticOptions.isIgnoreDiacritics());
			} else if(("." + DiacriticOptionsScriptParam.SELECTION_MODE_PARAM).contentEquals(e.getPropertyName())) {
				settings.setOnlyOrExcept(diacriticOptions.getSelectionMode() == SelectionMode.ONLY);
			} else if(("." + DiacriticOptionsScriptParam.SELECTED_DIACRITICS_PARAM).contentEquals(e.getPropertyName())) {
				settings.setSelectedDiacritics(diacriticOptions.getSelectedDiacritics());
			}
		});
		diacriticOptionsPanel = new DiacriticOptionsPanel(diacriticOptions);
		diacriticOptionsPanel.getIgnoreDiacriticsBox().setSelected(settings.isIgnoreDiacritics());
		diacriticOptionsPanel.getSelectionModeBox().setSelectedItem(settings.isOnlyOrExcept() ? SelectionMode.ONLY : SelectionMode.EXCEPT);
		diacriticOptionsPanel.getDiacriticSelector().setSelectedDiacritics(settings.getSelectedDiacritics());

		caseSensitiveBox = new JCheckBox("Case sensititve");
		caseSensitiveBox.setSelected(settings.isCaseSensitive());
		caseSensitiveBox.addActionListener( (e) -> settings.setCaseSensitive(caseSensitiveBox.isSelected()) );

		autoConfigPanel.getContentContainer().setLayout(new VerticalLayout());
		autoConfigPanel.getContentContainer().add(autoGroupingPanel);
		autoConfigPanel.getContentContainer().add(includeMetadataBox);
		autoConfigPanel.getContentContainer().add(includeGroupDataBox);
		autoConfigPanel.getContentContainer().add(includeWordDataBox);
		autoConfigPanel.getContentContainer().add(diacriticOptionsPanel);
		autoConfigPanel.getContentContainer().add(caseSensitiveBox);
		
		// manual config options
		manualConfigBtn = new JRadioButton("Manual Configuration");
		manualConfigBtn.setOpaque(false);
		configTypeGroup.add(manualConfigBtn);
		manualConfigPanel = new TitledPanel("");
		manualConfigPanel.setLeftDecoration(manualConfigBtn);
		manualConfigBtn.setSelected(!settings.isConfigureAutomatically());
		
		autoConfigBtn.addActionListener( (e) -> settings.setConfigureAutomatically(true) );
		manualConfigBtn.addActionListener( (e) -> settings.setConfigureAutomatically(false) );
		
		groupByPanel = new ColumnPanel(new ColumnInfo());
		columnPanel = new JPanel(new VerticalLayout());
		
		final ImageIcon icon = IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final Action onAddAction = new PhonUIAction(this, "onAddColumn");
		onAddAction.putValue(Action.NAME, "Add");
		onAddAction.putValue(Action.SHORT_DESCRIPTION, "Add column to sort");
		onAddAction.putValue(Action.SMALL_ICON, icon);
		addColumnButton = new JButton(onAddAction);
		
		updateManualConfig();
		
		setLayout(new BorderLayout());
		add(autoConfigPanel, BorderLayout.NORTH);
		add(manualConfigPanel, BorderLayout.CENTER);
	}

	public void updateManualConfig() {
		ColumnInfo groupBy = settings.getGroupBy();
		if(groupBy == null) {
			groupBy = new ColumnInfo();
			settings.setGroupBy(groupBy);
		}
		groupByPanel.setColumnInfo(settings.getGroupBy());
		groupByPanel.setBorder(BorderFactory.createTitledBorder("Group by"));

		columnPanel.removeAll();
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

		final JPanel btmPanel = new JPanel(new BorderLayout());
		btmPanel.setBorder(BorderFactory.createTitledBorder("Columns"));
		btmPanel.add(columnPanel, BorderLayout.CENTER);
		btmPanel.add(ButtonBarBuilder.buildOkBar(addColumnButton), BorderLayout.SOUTH);

		manualConfigPanel.getContentContainer().removeAll();
		manualConfigPanel.getContentContainer().setLayout(new BorderLayout());
		manualConfigPanel.getContentContainer().add(groupByPanel, BorderLayout.NORTH);
		manualConfigPanel.getContentContainer().add(btmPanel, BorderLayout.CENTER);
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
		manualConfigPanel.getContentContainer().revalidate();
		repaint();
	}

	private class ColumnPanel extends JPanel {

		private PromptedTextField nameField;

		private JCheckBox caseSensitiveBox;

		private DiacriticOptionsPanel diacriticOptionsPanel;

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
		
		public void setColumnInfo(ColumnInfo info) {
			this.info = info;
			update();
		}

		private void init() {
			setLayout(new GridBagLayout());
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
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
					updateColumnInfo();
				}

				@Override
				public void insertUpdate(DocumentEvent e) {
					updateColumnInfo();
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
			gbc.weightx = 1.0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			final DiacriticOptionsScriptParam diacriticOptions = new DiacriticOptionsScriptParam("", "", false, new ArrayList<>());
			diacriticOptions.addPropertyChangeListener( (e) -> {
				if(("." + DiacriticOptionsScriptParam.IGNORE_DIACRITICS_PARAM).contentEquals(e.getPropertyName())) {
					info.setIgnoreDiacritics(diacriticOptions.isIgnoreDiacritics());
				} else if(("." + DiacriticOptionsScriptParam.SELECTION_MODE_PARAM).contentEquals(e.getPropertyName())) {
					info.setOnlyOrExcept(diacriticOptions.getSelectionMode() == SelectionMode.ONLY);
				} else if(("." + DiacriticOptionsScriptParam.SELECTED_DIACRITICS_PARAM).contentEquals(e.getPropertyName())) {
					info.setSelectedDiacritics(diacriticOptions.getSelectedDiacritics());
				}
			});
			diacriticOptionsPanel = new DiacriticOptionsPanel(diacriticOptions);
			diacriticOptionsPanel.getIgnoreDiacriticsBox().setSelected(info.isIgnoreDiacritics());
			diacriticOptionsPanel.getSelectionModeBox().setSelectedItem(info.isOnlyOrExcept() ? SelectionMode.ONLY : SelectionMode.EXCEPT);
			diacriticOptionsPanel.getDiacriticSelector().setSelectedDiacritics(info.getSelectedDiacritics());
			
			add(diacriticOptionsPanel, gbc);
		}
		
		private void update() {
			nameField.setText(info.getName());
			caseSensitiveBox.setSelected(info.caseSensitive);
			diacriticOptionsPanel.getDiacriticOptions().setIgnoreDiacritics(info.ignoreDiacritics);
			diacriticOptionsPanel.getDiacriticOptions().setSelectionMode(info.onlyOrExcept ? SelectionMode.ONLY : SelectionMode.EXCEPT);
			diacriticOptionsPanel.getDiacriticOptions().setSelectedDiacritics(info.selectedDiacritics);
		}

		private void updateColumnInfo() {
			info.setName(nameField.getText());
		}
	}

}
