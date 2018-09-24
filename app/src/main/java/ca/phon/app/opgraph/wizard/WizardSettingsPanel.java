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
package ca.phon.app.opgraph.wizard;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;
import ca.phon.ui.HidablePanel;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.text.PromptedTextField;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Displays all options for modifying the wizard.
 *
 */
public class WizardSettingsPanel extends JPanel {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(WizardSettingsPanel.class.getName());
	
	private static final long serialVersionUID = 865535897566978589L;

	private DialogHeader header;
	
	private JTabbedPane tabPane;
	
	private JPanel introPanel;
	private final static String INTRO_PANEL_PROP = WizardSettingsPanel.class.getName() + ".introMessage"; 
	private HidablePanel introMessagePanel;
	private SettingsPanel introSettingsPanel;
	
	private JPanel advancedSettingsPanel;
	private final static String ADV_SETTINGS_PROP = WizardSettingsPanel.class.getName() + ".advSettingsMessage";
	private HidablePanel advancedSettingsMessagePanel;
	private OpGraphCheckBoxTree advancedSettingsTree;
	private CardLayout advancedSettingsCardLayout;
	private Map<OpNode, SettingsPanel> advancedSettingsMap;
	private JPanel advancedSettingsContentPanel;
	
	private JPanel optionalsPanel;
	private final static String OPTIONALS_PROP = WizardSettingsPanel.class.getName() + ".optionalsMessage";
	private HidablePanel optionalsMessagePanel;
	private OpGraphCheckBoxTree optionalsTree;
	private CardLayout optionalsCardLayout;
	private Map<OpNode, SettingsPanel> optionalsMap;
	private JPanel optionalsContentPanel;

	private JPanel reportTemplatePanel;
	private final static String REPORT_PROP = WizardSettingsPanel.class.getName() + ".rptTemplateMessage";
	private JList<String> reportTemplateList;
	private DefaultListModel<String> reportTemplateListModel;
	private HidablePanel reportTemplateMessagePanel;
	private CardLayout reportTemplateCardLayout;
	private Map<String, SettingsPanel> reportTemplateMap;
	private JPanel reportContentPanel;
	
	private final OpGraph graph;
	
	private final WizardExtension wizardExtension;
	
	public WizardSettingsPanel(OpGraph graph, WizardExtension wizardExtension) {
		super();
		
		this.graph = graph;
		this.wizardExtension = wizardExtension;
		
		init();
	}
	
	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("Wizard Settings", "Modify settings for the wizard.");
		
		tabPane = new JTabbedPane();
		
		createIntroPanel();
		tabPane.add("Introduction", introPanel);
		
		createAdvancedSettingsPanel();
		tabPane.add("Advanced Settings", advancedSettingsPanel);
		
		createOptionalsPanel();
		tabPane.add("Optional Nodes", optionalsPanel);
		
		createReportTemplatePanel();
		tabPane.add("Reports", reportTemplatePanel);
		
		add(tabPane, BorderLayout.CENTER);
	}
	
	private void createIntroPanel() {
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(2, 2, 0, 2);
		
		introPanel = new JPanel(layout);
		
		introMessagePanel = new HidablePanel(INTRO_PANEL_PROP);
		introMessagePanel.setTopLabelText("Introduction");
		introMessagePanel.setBottomLabelText("Modify the message displayed as the first step of the wizard.");
		
		introSettingsPanel = new SettingsPanel();
		introSettingsPanel.checkBox.setVisible(false);
		introSettingsPanel.titleField.setText(wizardExtension.getWizardTitle());
		introSettingsPanel.infoArea.setText(wizardExtension.getWizardMessage());
		introSettingsPanel.formatBox.setSelectedItem(wizardExtension.getWizardMessageFormat());
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		introPanel.add(introMessagePanel, gbc);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		++gbc.gridy;
		introPanel.add(introSettingsPanel, gbc);
	}
	
	private void createAdvancedSettingsPanel() {
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(2, 2, 0, 2);
		
		advancedSettingsPanel = new JPanel(layout);
		
		advancedSettingsMessagePanel = new HidablePanel(ADV_SETTINGS_PROP);
		advancedSettingsMessagePanel.setTopLabelText("Advanced Settings");
		advancedSettingsMessagePanel.setBottomLabelText(
				"<html>Select nodes to display as optional settings in the wizard. "
				+ "If 'required' is selected, the settings will appear as a step in the wizard.<br/>"
				+ "An optional message may also be added to the settings panel.</html>");
		
		advancedSettingsMap = new HashMap<>();
		
		advancedSettingsTree = new OpGraphCheckBoxTree(getGraph());
		advancedSettingsTree.setPreferredSize(new Dimension(250, 0));
		advancedSettingsTree.setRootVisible(false);
		advancedSettingsTree.setBorder(BorderFactory.createTitledBorder("Outline"));
		final JScrollPane scroller = new JScrollPane(advancedSettingsTree);
		advancedSettingsTree.addTreeSelectionListener( (e) -> {
			final TreePath selectedPath = e.getPath();
			if(selectedPath != null) {
				final Object lastNode = selectedPath.getLastPathComponent();
				if(lastNode instanceof TristateCheckBoxTreeNode) {
					final TristateCheckBoxTreeNode treeNode = (TristateCheckBoxTreeNode)lastNode;
					if(treeNode.isLeaf()) {
						final OpNode node = (OpNode)treeNode.getUserObject();
					
						// update panel
						SettingsPanel nodePanel = advancedSettingsMap.get(node);
						if(nodePanel == null) {
							nodePanel = new SettingsPanel();
							nodePanel.setTitle(wizardExtension.getNodeTitle(node));
							nodePanel.setInfo(wizardExtension.getNodeMessage(node));
							nodePanel.setFormat(wizardExtension.getNodeMessageFormat(node));
							nodePanel.checkBox.setText("Required (show node settings as a wizard step)");
							nodePanel.checkBox.setSelected(wizardExtension.isNodeForced(node));
							
							final String id = Integer.toHexString(nodePanel.hashCode());
							advancedSettingsMap.put(node, nodePanel);
							advancedSettingsContentPanel.add(nodePanel, id);
						}
						final String id = Integer.toHexString(nodePanel.hashCode());
						advancedSettingsCardLayout.show(advancedSettingsContentPanel, id);
						advancedSettingsPanel.revalidate();
					}
				}
			}
		});
		
		for(OpNode settingsNode:getWizardExtension()) {
			final TreePath nodePath = advancedSettingsTree.treePathForNode(settingsNode);
			advancedSettingsTree.setCheckingStateForPath(nodePath, TristateCheckBoxState.CHECKED);
			
			final TreeNode node = (TreeNode)nodePath.getLastPathComponent();
			if(node.isLeaf())
				advancedSettingsTree.expandPath(nodePath.getParentPath());
			else
				advancedSettingsTree.expandPath(nodePath);
		}
		
		advancedSettingsCardLayout = new CardLayout();
		advancedSettingsContentPanel = new JPanel(advancedSettingsCardLayout);
		
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		advancedSettingsPanel.add(advancedSettingsMessagePanel, gbc);
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.2;
		gbc.weighty = 1.0;
		++gbc.gridy;
		advancedSettingsPanel.add(scroller, gbc);
		gbc.weightx = 1.0;
		++gbc.gridx;
		advancedSettingsPanel.add(advancedSettingsContentPanel, gbc);
	}
	
	private void createOptionalsPanel() {
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(2, 2, 0, 2);
		
		optionalsPanel = new JPanel(layout);
		
		optionalsMessagePanel = new HidablePanel(OPTIONALS_PROP);
		optionalsMessagePanel.setTopLabelText("Optionals");
		optionalsMessagePanel.setBottomLabelText(
				"Select optional paths through the execution tree.");
		
		optionalsMap = new HashMap<>();
		optionalsTree = new OpGraphCheckBoxTree(getGraph(), false);
		optionalsTree.setRootVisible(false);
		optionalsTree.setPreferredSize(new Dimension(250, 0));
		optionalsTree.setRootVisible(false);
		optionalsTree.setBorder(BorderFactory.createTitledBorder("Outline"));
		final JScrollPane scroller = new JScrollPane(optionalsTree);
		optionalsTree.addTreeSelectionListener( (e) -> {
			final TreePath selectedPath = e.getPath();
			if(selectedPath != null) {
				final Object lastNode = selectedPath.getLastPathComponent();
				if(lastNode instanceof TristateCheckBoxTreeNode) {
					final TristateCheckBoxTreeNode treeNode = (TristateCheckBoxTreeNode)lastNode;
					final OpNode node = (OpNode)treeNode.getUserObject();
				
					// update panel
					SettingsPanel nodePanel = optionalsMap.get(node);
					if(nodePanel == null) {
						nodePanel = new SettingsPanel();
						nodePanel.formatBox.setVisible(false);
						nodePanel.infoArea.setVisible(false);
						nodePanel.titleField.setVisible(false);
						nodePanel.checkBox.setText("Default value");
						nodePanel.checkBox.setSelected(wizardExtension.getOptionalNodeDefault(node));
						
						final String id = Integer.toHexString(nodePanel.hashCode());
						optionalsMap.put(node, nodePanel);
						optionalsContentPanel.add(nodePanel, id);
					}
					final String id = Integer.toHexString(nodePanel.hashCode());
					optionalsCardLayout.show(optionalsContentPanel, id);
					optionalsPanel.revalidate();
				}
			}
		});
		
		for(OpNode settingsNode:getWizardExtension().getOptionalNodes()) {
			final TreePath nodePath = optionalsTree.treePathForNode(settingsNode);
			optionalsTree.setCheckingStateForPath(nodePath, TristateCheckBoxState.CHECKED);
			
			final TreeNode node = (TreeNode)nodePath.getLastPathComponent();
			if(node.isLeaf())
				optionalsTree.expandPath(nodePath.getParentPath());
			else
				optionalsTree.expandPath(nodePath);
		}
		
		optionalsCardLayout = new CardLayout();
		optionalsContentPanel = new JPanel(optionalsCardLayout);
		
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		optionalsPanel.add(optionalsMessagePanel, gbc);
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.2;
		gbc.weighty = 1.0;
		++gbc.gridy;
		optionalsPanel.add(scroller, gbc);
		gbc.weightx = 1.0;
		++gbc.gridx;
		optionalsPanel.add(optionalsContentPanel, gbc);
	}
	
	private void createReportTemplatePanel() {
		final GridBagLayout layout = new GridBagLayout();
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(2, 2, 0, 2);
		
		reportTemplatePanel = new JPanel(layout);
		
		reportTemplateMessagePanel = new HidablePanel(REPORT_PROP);
		reportTemplateMessagePanel.setTopLabelText("Report Templates");
		reportTemplateMessagePanel.setBottomLabelText(
				"Setup report templates for the analysis.");
		
		reportTemplateMap = new HashMap<>();
		
		reportTemplateListModel = new DefaultListModel<>();
		wizardExtension.getReportTemplateNames().forEach( (r) -> reportTemplateListModel.addElement(r) );
		reportTemplateList = new JList<>(reportTemplateListModel);
		reportTemplateList.setPreferredSize(new Dimension(250, 0));
		final JScrollPane scroller = new JScrollPane(reportTemplateList);
		reportTemplateList.addListSelectionListener( (e) -> {
			final String selectedReport = reportTemplateList.getSelectedValue();
			if(selectedReport != null) {
				NodeWizardReportTemplate template = wizardExtension.getReportTemplate(selectedReport);
				if(template == null) {
					template = wizardExtension.putReportTemplate(selectedReport, "");
				}
				
				SettingsPanel settingsPanel = reportTemplateMap.get(selectedReport);
				if(settingsPanel == null) {
					settingsPanel = new SettingsPanel();
					settingsPanel.formatBox.setVisible(false);
					settingsPanel.checkBox.setVisible(false);
					settingsPanel.infoArea.setText(template.getTemplate());
					settingsPanel.titleField.setText(selectedReport);
					
					reportTemplateMap.put(selectedReport, settingsPanel);
					reportContentPanel.add(settingsPanel, selectedReport);
				}
				reportTemplateCardLayout.show(reportContentPanel, selectedReport);
				reportContentPanel.revalidate();
			}
		});
		
		final ImageIcon addIcn = IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final PhonUIAction addAct = new PhonUIAction(this, "onAddReport");
		addAct.putValue(PhonUIAction.SMALL_ICON, addIcn);
		addAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add report");
		final JButton addBtn = new JButton(addAct);
		
		final ImageIcon removeIcn = IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction removeAct = new PhonUIAction(this, "onRemoveReport");
		removeAct.putValue(PhonUIAction.SMALL_ICON, removeIcn);
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected reports");
		final JButton removeBtn = new JButton(removeAct);
		
		final JComponent btnPanel = ButtonBarBuilder.buildOkCancelBar(addBtn, removeBtn);
		
		final JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(btnPanel, BorderLayout.NORTH);
		leftPanel.add(scroller, BorderLayout.CENTER);
		
		reportTemplateCardLayout = new CardLayout();
		reportContentPanel = new JPanel(reportTemplateCardLayout);
		
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		reportTemplatePanel.add(reportTemplateMessagePanel, gbc);
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 0.2;
		gbc.weighty = 1.0;
		++gbc.gridy;
		reportTemplatePanel.add(leftPanel, gbc);
		gbc.weightx = 1.0;
		++gbc.gridx;
		reportTemplatePanel.add(reportContentPanel, gbc);
	}
	
	public void onAddReport() {
		String reportName = "Report";
		int i = 0;
		while(reportTemplateListModel.contains(reportName)) {
			reportName = "Report " + (++i);
		}
		reportTemplateListModel.addElement(reportName);
	}
	
	public void onRemoveReport() {
		final int[] selectedReports = reportTemplateList.getSelectedIndices();
		for(int i = selectedReports.length-1; i >= 0; i--) {
			reportTemplateListModel.remove(i);
		}
	}
	
	public OpGraph getGraph() {
		return this.graph;
	}
	
	/**
	 * Returns the original wizard extension object. 
	 * @return
	 */
	public WizardExtension getWizardExtension() {
		return this.wizardExtension;
	}
	
	public WizardExtension getUpdatedWizardExtension() {
		final WizardExtension origExtension = getWizardExtension();
		final Class<? extends WizardExtension> extClass = getWizardExtension().getClass();
		
		Constructor<? extends WizardExtension> constructor;
		WizardExtension retVal = null;
		try {
			constructor = extClass.getConstructor(OpGraph.class);
			retVal = constructor.newInstance(getGraph());
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
			return getWizardExtension();
		}
		
		retVal.setWizardTitle(introSettingsPanel.getTitle());
		retVal.setWizardMessage(introSettingsPanel.getInfo(), introSettingsPanel.getFormat());
		
		for(TreePath path:advancedSettingsTree.getCheckedPaths()) {
			final TristateCheckBoxTreeNode treeNode = (TristateCheckBoxTreeNode)path.getLastPathComponent();
			if(treeNode.isLeaf()) {
				final OpNode node = (OpNode)treeNode.getUserObject();
				retVal.addNode(node);
				
				final SettingsPanel nodeSettings = advancedSettingsMap.get(node);
				if(nodeSettings != null) {
					retVal.setNodeTitle(node, nodeSettings.getTitle());
					retVal.setNodeMessage(node, nodeSettings.getInfo(), nodeSettings.getFormat());
					retVal.setNodeForced(node, nodeSettings.isSelected());
				} else {
					retVal.setNodeTitle(node, origExtension.getNodeTitle(node));
					retVal.setNodeMessage(node, origExtension.getNodeMessage(node));
					retVal.setNodeForced(node, origExtension.isNodeForced(node));
				}
			}
		}
		
		for(TreePath path:optionalsTree.getCheckedPaths()) {
			final TristateCheckBoxTreeNode treeNode = (TristateCheckBoxTreeNode)path.getLastPathComponent();
			final OpNode node = (OpNode)treeNode.getUserObject();
			
			retVal.addOptionalNode(node);
			final SettingsPanel nodeSettings = optionalsMap.get(node);
			if(nodeSettings != null) {
				retVal.setOptionalNodeDefault(node, nodeSettings.isSelected());
			} else {
				retVal.setOptionalNodeDefault(node, origExtension.getOptionalNodeDefault(node));
			}
		}
		
		for(int i = 0; i < reportTemplateListModel.size(); i++) {
			final String name = reportTemplateListModel.getElementAt(i);
			final SettingsPanel settings = reportTemplateMap.get(name);
			if(settings != null) {
				retVal.putReportTemplate(name, settings.getInfo());
			} else {
				retVal.putReportTemplate(name, origExtension.getReportTemplate(name).getTemplate());
			}
		}
		
		return retVal;
	}
	
	private class SettingsPanel extends JPanel {
		
		private PromptedTextField titleField;
		
		private JComboBox<WizardInfoMessageFormat> formatBox;
		
		private RSyntaxTextArea infoArea;
		
		private JCheckBox checkBox;
		
		SettingsPanel() {
			super();
			
			init();
		}
		
		private void init() {
			setLayout(new BorderLayout());
			
			titleField = new PromptedTextField("Enter title");
			final JPanel topPanel = new JPanel(new BorderLayout());
			topPanel.add(titleField, BorderLayout.CENTER);
			topPanel.setBorder(BorderFactory.createTitledBorder("Title"));
			add(topPanel, BorderLayout.NORTH);
			
			checkBox = new JCheckBox();
			
			formatBox = new JComboBox<>(WizardInfoMessageFormat.values());
			infoArea = new RSyntaxTextArea();
			infoArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
			formatBox.addItemListener( (e) -> {
				WizardInfoMessageFormat format = (WizardInfoMessageFormat)formatBox.getSelectedItem();
				switch(format) {
				case HTML:
					infoArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
					break;
					
				case MARKDOWN:
					infoArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
				}
			});
			
			infoArea = new RSyntaxTextArea();
			infoArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
			final JPanel centerPanel = new JPanel(new BorderLayout());
			final JPanel formatPanel = new JPanel(new BorderLayout());
			
			formatPanel.add(checkBox, BorderLayout.WEST);
			formatPanel.add(formatBox, BorderLayout.EAST);
			centerPanel.add(formatPanel, BorderLayout.NORTH);
			centerPanel.add(new RTextScrollPane(infoArea, true), BorderLayout.CENTER);
			centerPanel.setBorder(BorderFactory.createTitledBorder("Info"));
			add(centerPanel, BorderLayout.CENTER);
		}
		
		public void setTitle(String title) {
			titleField.setText(title);
		}
		
		public String getTitle() {
			return titleField.getText();
		}
		
		public void setInfo(String info) {
			infoArea.setText(info);
		}
		
		public String getInfo() {
			return infoArea.getText();
		}
		
		public void setFormat(WizardInfoMessageFormat format) {
			formatBox.setSelectedItem(format);
		}
		
		public boolean isSelected() {
			return checkBox.isSelected();
		}
		
		public WizardInfoMessageFormat getFormat() {
			return (WizardInfoMessageFormat)formatBox.getSelectedItem();
		}
		
	}
}
