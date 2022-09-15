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
package ca.phon.app.opgraph.nodes.table;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import org.jdesktop.swingx.*;

import ca.phon.app.opgraph.nodes.table.SortNodeSettings.*;
import ca.phon.app.opgraph.nodes.table.SortNodeSettings.SortOrder;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.layout.*;
import ca.phon.ui.text.*;
import ca.phon.util.icons.*;

public class SortNodeSettingsPanel extends JPanel {

	private static final long serialVersionUID = 4289280424233502931L;
	
	private final SortNodeSettings settings;
	
	private TitledPanel autoConfigPanel;
	private TitledPanel manualConfigPanel;

	private ButtonGroup configTypeGroup;
	private JRadioButton autoConfigBtn;
	private JRadioButton manualConfigBtn;
	
	private JCheckBox autoConfigBox;
	private JComboBox<SortOrder> autoSortOrderBox;
	
	private JPanel sortByPanel;
	private JButton addSortButton;
	
	private JCheckBox likeOnTopBox;
	
	public SortNodeSettingsPanel(SortNodeSettings settings) {
		super();
		this.settings = settings;
		
		init();
	}
	
	private void init() {
		configTypeGroup = new ButtonGroup();
		
		likeOnTopBox = new JCheckBox("Keep like on top");
		likeOnTopBox.setSelected(settings.isLikeOnTop());
		likeOnTopBox.addActionListener( (e) -> settings.setLikeOnTop(likeOnTopBox.isSelected()) );
		
		// auto config options
		autoConfigBtn = new JRadioButton("Automatic Configuration");
		autoConfigBtn.setOpaque(false);
		configTypeGroup.add(autoConfigBtn);
		autoConfigPanel = new TitledPanel("");
		autoConfigPanel.setLeftDecoration(autoConfigBtn);
		autoConfigBtn.setSelected(settings.isConfigureAutomatically());
		
		autoSortOrderBox = new JComboBox<>(SortOrder.values());
		autoSortOrderBox.setSelectedItem(settings.getAutoSortOrder());
		autoSortOrderBox.addItemListener( (e) -> settings.setAutoSortOrder((SortOrder)autoSortOrderBox.getSelectedItem()) );
		autoConfigPanel.getContentContainer().setLayout(new BorderLayout());
		autoConfigPanel.getContentContainer().add(new JLabel("Sort order:"), BorderLayout.WEST);
		autoConfigPanel.getContentContainer().add(autoSortOrderBox, BorderLayout.CENTER);
		
		manualConfigBtn = new JRadioButton("Manual Configuration");
		manualConfigBtn.setOpaque(false);
		configTypeGroup.add(manualConfigBtn);
		manualConfigPanel = new TitledPanel("");
		manualConfigPanel.setLeftDecoration(manualConfigBtn);
		manualConfigBtn.setSelected(!settings.isConfigureAutomatically());
		
		final ActionListener configListener = (e) -> {
			autoConfigPanel.getContentContainer().setEnabled(settings.isConfigureAutomatically());
			manualConfigPanel.getContentContainer().setEnabled(!settings.isConfigureAutomatically());
			settings.setConfigureAutomatically(autoConfigBtn.isSelected());
		};
		autoConfigBtn.addActionListener(configListener);
		manualConfigBtn.addActionListener(configListener);
		
		sortByPanel = new JPanel(new VerticalLayout());
		
		final ImageIcon icon = IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final Action onAddAction = PhonUIAction.runnable(this::onAddColumn);
		onAddAction.putValue(Action.NAME, "Add");
		onAddAction.putValue(Action.SHORT_DESCRIPTION, "Add column to sort");
		onAddAction.putValue(Action.SMALL_ICON, icon);
		addSortButton = new JButton(onAddAction);
		
		manualConfigPanel.getContentContainer().setLayout(new BorderLayout());
		manualConfigPanel.getContentContainer().add(sortByPanel, BorderLayout.CENTER);
		manualConfigPanel.getContentContainer().add(ButtonBarBuilder.buildOkBar(addSortButton), BorderLayout.SOUTH);
		
		updateManualConfig();
		
		setLayout(new BorderLayout());
		add(autoConfigPanel, BorderLayout.NORTH);
		add(manualConfigPanel, BorderLayout.CENTER);
		add(likeOnTopBox, BorderLayout.SOUTH);
	}
	
	public void updateManualConfig() {
		sortByPanel.removeAll();
		
		int scIdx = 0;
		for(SortColumn sc:settings.getSorting()) {
			final SortColumnPanel scPanel = new SortColumnPanel(sc);
			if(scIdx > 0) {
				final JComponent sep = createSeparator(scPanel);
				sortByPanel.add(sep);
			}
			sortByPanel.add(scPanel);
			++scIdx;
		}
	}
	
	public void onAddColumn() {
		final SortColumn sc = new SortColumn();
		settings.getSorting().add(sc);
		final SortColumnPanel scPanel = new SortColumnPanel(sc);
		final JComponent sep = createSeparator(scPanel);
		sortByPanel.add(sep);
		sortByPanel.add(scPanel);
		revalidate();
	}
	
	public void onRemoveColumn(SortColumnPanel scPanel) {
		sortByPanel.remove(scPanel);
		if(scPanel.getSeparator() != null)
			sortByPanel.remove(scPanel.getSeparator());
		settings.getSorting().remove(scPanel.getSortColumn());
		sortByPanel.revalidate();
		repaint();
	}

	public SortNodeSettings getSettings() {
		return this.settings;
	}
	
	private JComponent createSeparator(SortColumnPanel scPanel) {
		final ImageIcon removeIcon =
				IconManager.getInstance().getDisabledIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction<SortColumnPanel> removeAct = PhonUIAction.consumer(this::onRemoveColumn, scPanel);
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
		
		scPanel.setSeparator(sep);
		
		return sep;
	}
	
	private PromptedTextField createColumnField() {
		final PromptedTextField retVal = new PromptedTextField();
		retVal.setPrompt("Enter column name or number");
		return retVal;
	}
	
	private JComboBox<SortType> createSortTypeBox() {
		final JComboBox<SortType> retVal = new JComboBox<>(SortType.values());
		retVal.setSelectedItem(null);
		return retVal;
	}
	
	private JComboBox<FeatureFamily> createFeatureBox() {
		final FeatureFamily[] boxVals = new FeatureFamily[FeatureFamily.values().length + 1];
		int idx = 0;
		boxVals[idx++] = null;
		for(FeatureFamily v:FeatureFamily.values()) boxVals[idx++] = v;
		
		final JComboBox<FeatureFamily> retVal = new JComboBox<>(boxVals);
		retVal.setSelectedItem(null);
		return retVal;
	}
	
	class SortColumnPanel extends JPanel {
		private PromptedTextField columnField = createColumnField();
		private JComboBox<SortType> typeBox = createSortTypeBox();
		
		// plain text options
		private JPanel orderOptions = new JPanel();
		private JRadioButton ascendingBox = new JRadioButton("Ascending");
		private JRadioButton descendingBox = new JRadioButton("Descending");
		
		private final SortColumn sortColumn;
		
		private JComponent separator;
		
		public SortColumnPanel(SortColumn sortColumn) {
			super();
			
			this.sortColumn = sortColumn;
			init();
		}
		
		private void init() {
			setLayout(new GridBagLayout());
			
			final GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.EAST;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.insets = new Insets(2, 2, 5, 2);
			
			columnField.setText(sortColumn.getColumn());
			columnField.getDocument().addDocumentListener(new DocumentListener() {
				
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
					
				}
			});
			gbc.gridy = 0;
			gbc.gridx = 0;
			gbc.weightx = 0.0;
			add(new JLabel("Column:"), gbc);
			
			gbc.gridx++;
			gbc.weightx = 1.0;
			add(columnField, gbc);
			
			gbc.weightx = 0.0;
			gbc.gridx = 0;
			gbc.gridy++;
			add(new JLabel("Sort type:"), gbc);
			
			typeBox.addItemListener( (e) -> {
				sortColumn.setType((SortType)typeBox.getSelectedItem());
			});
			typeBox.setSelectedItem(sortColumn.getType());
			gbc.gridx++;
			gbc.weightx = 1.0;
			add(typeBox, gbc);

			final ButtonGroup grp = new ButtonGroup();
			grp.add(ascendingBox);
			grp.add(descendingBox);
			ascendingBox.setSelected(sortColumn.getOrder() == SortOrder.ASCENDING);
			descendingBox.setSelected(sortColumn.getOrder() == SortOrder.DESCENDING);
			orderOptions.setLayout(new HorizontalLayout());
			
			final ChangeListener l = (e) -> {
				if(ascendingBox.isSelected())
					sortColumn.setOrder(SortOrder.ASCENDING);
				else
					sortColumn.setOrder(SortOrder.DESCENDING);
			};
			orderOptions.add(ascendingBox);
			orderOptions.add(descendingBox);
			ascendingBox.addChangeListener(l);
			descendingBox.addChangeListener(l);
			
			gbc.gridx = 1;
			gbc.gridy++;
			gbc.insets = new Insets(0, 0, 0, 0);
			add(orderOptions, gbc);
			
			add(new JSeparator(SwingConstants.HORIZONTAL));
		}
		
		void setSeparator(JComponent sep) {
			this.separator = sep;
		}
		
		JComponent getSeparator() {
			return this.separator;
		}
		
		public SortColumn getSortColumn() {
			return sortColumn;
		}
		
		public void updateColumn() {
			sortColumn.setColumn(columnField.getText().trim());
		}
	}
}
