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
package ca.phon.app.opgraph.wizard;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.swing.*;
import javax.swing.tree.*;

import org.jdesktop.swingx.*;

import ca.phon.opgraph.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.util.icons.*;

/**
 * Display settings for wizard nodes identified in the
 * given extension.
 */
public class AdvancedSettingsPanel extends JPanel {

	private final static String NO_SELECTION = "_no_selection_";
	
	private JXTree nodeTree;
	
	private JPanel settingsPanel;
	private CardLayout cardLayout;
	
	private WizardExtension wizardExtension;
	
	public AdvancedSettingsPanel(WizardExtension ext) {
		super();
		this.wizardExtension = ext;
		
		init();
	}
	
	private void init() {
		final GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;

		nodeTree = new JXTree(new NodeTreeModel());
		nodeTree.addTreeSelectionListener( (e) -> {
			TreePath tp = nodeTree.getSelectionPath();
			List<OpNode> nodePath = new ArrayList<>();
			for(Object obj:tp.getPath()) {
				final DefaultMutableTreeNode node = 
						(DefaultMutableTreeNode)obj;
				if(!(node instanceof RootNode)) {
					nodePath.add((OpNode)node.getUserObject());
				}
			}
			cardLayout.show(settingsPanel, nodePathToString(nodePath));
		});
		nodeTree.setCellRenderer(new OpNodeTreeCellRenderer());
		final JScrollPane nodeTreeScroller = new JScrollPane(nodeTree);
		gbc.weighty = 1.0;
		gbc.weightx = 0.4;
		add(nodeTreeScroller, gbc);
		
		++gbc.gridx;
		gbc.weightx = 0.6;
		cardLayout = new CardLayout();
		settingsPanel = new JPanel(cardLayout);
		settingsPanel.add(createNoSelectionComponent(), NO_SELECTION);
		add(settingsPanel, gbc);
		
		addWizardSettings();
	}
	
	private void addWizardSettings() {
		final OpGraph graph = getWizardExtension().getGraph();
		for(OpNode node:getWizardExtension()) {
			if(getWizardExtension().isNodeForced(node)) continue;
			final NodeSettings settings = node.getExtension(NodeSettings.class);
			if(settings != null) {
				try {
					final Component comp = settings.getComponent(null);
					
					final String nodeMessage = getWizardExtension().getNodeMessage(node);
					
					final JPanel panel = new JPanel(new BorderLayout());
					panel.add(comp, BorderLayout.CENTER);
					
					if(nodeMessage != null && nodeMessage.length() > 0) {
						final JLabel nodeMessageLbl = new JLabel(nodeMessage);
						panel.add(nodeMessageLbl, BorderLayout.NORTH);
					}
					settingsPanel.add(panel, 
							nodePathToString(graph.getNodePath(node.getId())));
				} catch (NullPointerException e) {
					// using 'null' for document may cause this depending
					// on 'age' of node settings implementation
				}
			}
		}
	}
	
	private String nodePathToString(List<OpNode> nodes) {
		return nodes.stream().map( (node) -> node.getName() ).collect(Collectors.joining("/"));
	}
	
	private JComponent createNoSelectionComponent() {
		final GridBagLayout layout = new GridBagLayout();
		final JPanel retVal = new JPanel(layout);
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		final JLabel lbl = new JLabel("<html><body><p>To edit advanced settings, select an item from the tree on the left.</p></body></html>");
		retVal.add(lbl, gbc);
		
		return retVal;
	}

	public WizardExtension getWizardExtension() {
		return this.wizardExtension;
	}
	
	private class RootNode extends DefaultMutableTreeNode {
	
		public RootNode(String name) {
			super(name);
		}
		
	}
	
	private class NodeTreeModel extends DefaultTreeModel {
		
		public NodeTreeModel() {
			super(new RootNode(getWizardExtension().getWizardTitle()));
			
			createTree();
		}
		
		private void createTree() {
			final OpGraph graph = getWizardExtension().getGraph();
			for(OpNode node:getWizardExtension()) {
				// don't add nodes that are already steps
				if(getWizardExtension().isNodeForced(node)) continue;
				final List<OpNode> nodePath = graph.getNodePath(node.getId());
				
				DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode)getRoot();
				for(OpNode currentNode:nodePath) {
					DefaultMutableTreeNode childNode = null;
					
					for(int i = 0; i < parentNode.getChildCount(); i++) {
						DefaultMutableTreeNode currentChild = 
								(DefaultMutableTreeNode)parentNode.getChildAt(i);
						if(currentChild.getUserObject() == currentNode) {
							childNode = currentChild;
							break;
						}
					}
					if(childNode == null) {
						childNode = new DefaultMutableTreeNode(currentNode);
						parentNode.add(childNode);
					}
					parentNode = childNode;
				}
			}
		}
		
	}
	
	private class OpNodeTreeCellRenderer extends DefaultTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			JLabel retVal = (JLabel)
					super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
			
			if(node.getUserObject() instanceof OpNode) {
				String name = getWizardExtension().getNodeTitle((OpNode)node.getUserObject());
				if(name == null)
					name = (((OpNode)node.getUserObject()).getName());
				retVal.setText(name);
			}
			
			if(leaf) {
				retVal.setIcon(IconManager.getInstance().getIcon("actions/settings-black", IconSize.XSMALL));
			}
			
			return retVal;
		}
		
	}
	
}
