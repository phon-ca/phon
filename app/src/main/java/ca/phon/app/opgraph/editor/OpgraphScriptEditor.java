/*
 * Copyright (C) 2005-2021 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.editor;

import ca.phon.app.opgraph.nodes.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.components.OpGraphTreeCellRenderer;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.util.icons.*;
import org.apache.commons.lang3.*;
import org.jdesktop.swingx.*;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class OpgraphScriptEditor extends JPanel {

	private final GraphDocument graphDocument;

	private OpNode currentEditorNode = null;
	private final Map<OpNode, ScriptNodeEditor> editorMap = new HashMap<>();

	private JPanel topPanel;
	private JButton updateButton;
	private DropDownButton scriptTreeButton;
	private JXTree scriptTree;
	private JXLabel currentNodeLabel;

	private JPanel cardPanel;
	private CardLayout cardLayout;

	private final static String NO_NODE_PANEL = "no_node_panel";
	private JPanel noNodePanel;

	public OpgraphScriptEditor(GraphDocument graphDocument) {
		super();

		this.graphDocument = graphDocument;
		init();
		this.graphDocument.getRootGraph().addGraphListener(graphListener);
	}

	private void init() {
		setLayout(new BorderLayout());

		cardLayout = new CardLayout();
		cardPanel = new JPanel(cardLayout);

		GridBagConstraints gbc = new GridBagConstraints();
		noNodePanel = new JPanel(new GridBagLayout());
		noNodePanel.setPreferredSize(new Dimension(400, 0));
		gbc.gridy = 0;
		gbc.gridx = 0;

		JLabel noNodeLbl = new JLabel("No script node open");
		noNodeLbl.setFont(noNodeLbl.getFont().deriveFont(Font.ITALIC));
		noNodePanel.add(noNodeLbl, gbc);

		cardPanel.add(noNodePanel, NO_NODE_PANEL);

		scriptTree = new JXTree();
		ScriptEditorTreeModel treeModel = new ScriptEditorTreeModel(scriptTree, this.graphDocument.getRootGraph());
		scriptTree.setModel(treeModel);
		scriptTree.setCellRenderer(new ScriptTreeRenderer());
		scriptTree.setVisibleRowCount(20);
		scriptTree.setRootVisible(true);
		scriptTree.addMouseListener(scriptTreeMouseHandler);
		scriptTree.setScrollsOnExpand(true);
		scriptTree.expandAll();

		JScrollPane scriptTreeScroller = new JScrollPane(scriptTree);
		scriptTreeScroller.setPreferredSize(new Dimension(400, scriptTreeScroller.getPreferredSize().height));

		final PhonUIAction dropDownAct = new PhonUIAction(this, "noop");
		dropDownAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		dropDownAct.putValue(DropDownButton.ARROW_ICON_GAP, 0);
		dropDownAct.putValue(DropDownButton.BUTTON_POPUP, scriptTreeScroller);
		dropDownAct.putValue(PhonUIAction.SMALL_ICON,
				IconManager.getInstance().getIcon("opgraph/graph", IconSize.SMALL));
		dropDownAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show graph script editor tree");
		scriptTreeButton = new DropDownButton(dropDownAct);
		scriptTreeButton.setOnlyPopup(true);

		ImageIcon saveIcn = IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);

		final PhonUIAction updateAct = new PhonUIAction(this, "updateCurrentEditor");
		updateAct.putValue(PhonUIAction.NAME, "");
		updateAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Update script node with current text (F5)");
		updateAct.putValue(PhonUIAction.SMALL_ICON, saveIcn);
		updateButton = new JButton(updateAct);
		updateButton.setEnabled(false);

		currentNodeLabel = new JXLabel();
		DropDownIcon nodeIcn = new DropDownIcon(IconManager.getInstance().getIcon("mimetypes/text-x-script", IconSize.SMALL), SwingConstants.BOTTOM);
		currentNodeLabel.setIcon(nodeIcn);
		currentNodeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		currentNodeLabel.addMouseListener(scriptLabelMouseHandler);

		topPanel = new JPanel(new GridBagLayout());
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.weightx = 0.0;
		gbc.weighty = 0.0;
		topPanel.add(updateButton, gbc);
		gbc.insets = new Insets(0, 5, 0, 0);
		++gbc.gridx;
		topPanel.add(scriptTreeButton, gbc);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		++gbc.gridx;
		topPanel.add(currentNodeLabel, gbc);

		add(topPanel, BorderLayout.NORTH);
		add(cardPanel, BorderLayout.CENTER);

		updateButtonStates();
	}

	private void updateButtonStates() {
		ScriptNodeEditor editor = currentEditor();
		if(editor != null && editor.hasChanges()) {
			updateButton.setEnabled(true);
		} else {
			updateButton.setEnabled(false);
		}
	}

	private void updateLabel() {
		ScriptNodeEditor currentEditor = currentEditor();
		if(currentEditor != null) {
			List<OpNode> nodePath = this.graphDocument.getRootGraph().getNodePath(this.currentEditorNode.getId());
			String nodeAddr = nodePath.stream().map(OpNode::getName).collect(Collectors.joining("/"));
			if(nodeAddr.length() > 256) {
				nodeAddr = StringUtils.abbreviate(nodeAddr, nodeAddr.length() - 256, 256);
			}
			currentNodeLabel.setText(this.currentEditorNode.getName() + (currentEditor.hasChanges() ? " *" : ""));
			currentNodeLabel.setToolTipText(nodeAddr);
		} else {
			currentNodeLabel.setText("");
		}
	}

	private ScriptNodeEditor currentEditor() {
		if(currentEditorNode == null) return null;
		return editorMap.get(currentEditorNode);
	}

	public void showScriptPopupMenu() {
		JPopupMenu menu = new JPopupMenu();
		MenuBuilder builder = new MenuBuilder(menu);
		setupScriptPopupMenu(builder);
		menu.show(currentNodeLabel, 0, currentNodeLabel.getHeight());
	}

	public void setupScriptPopupMenu(MenuBuilder builder) {
		ScriptNodeEditor currentEditor = currentEditor();
		for(OpNode node:editorMap.keySet()) {
			ScriptNodeEditor editor = editorMap.get(node);

			List<OpNode> nodePath = this.graphDocument.getRootGraph().getNodePath(node.getId());
			String nodeAddr = nodePath.stream().map(OpNode::getName).collect(Collectors.joining("/"));

			PhonUIAction act = new PhonUIAction(this, "onOpenScriptNode", node);
			act.putValue(PhonUIAction.NAME, nodeAddr + (editor.hasChanges() ? " *" : ""));
			act.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open script for node " + node.getName());
			act.putValue(PhonUIAction.SELECTED_KEY, editor == currentEditor);
			builder.addItem(".", new JCheckBoxMenuItem(act));
		}
	}

	public void updateCurrentEditor() {
		ScriptNodeEditor editor = currentEditor();
		if(editor != null && editor.hasChanges()) {
			ScriptNodeEdit edit = new ScriptNodeEdit(editor);
			this.graphDocument.getUndoSupport().postEdit(edit);
		}
	}

	public void onOpenScriptNode(PhonActionEvent pae) {
		openScriptNode((ScriptNode) pae.getData());
	}

	public void openScriptNode(ScriptNode scriptNode) {
		ScriptNodeEditor currentEditor = currentEditor();
		if(currentEditor != null) {
			currentEditor.removePropertyChangeListener("hasChanges", changeListener);
			currentEditor.getScriptNode().toOpNode().removeNodeListener(nodeListener);
		}

		if (!editorMap.containsKey(scriptNode)) {
			ScriptNodeEditor editor = new ScriptNodeEditor();
			editor.setScriptNode(scriptNode);

			final PhonUIAction updateScriptAct = new PhonUIAction(this, "updateCurrentEditor");
			editor.getActionMap().put("update_script", updateScriptAct);
			editor.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
					.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), "update_script");

			editorMap.put(scriptNode.toOpNode(), editor);
			cardPanel.add(editor, scriptNode.toOpNode().getId());
		}
		cardLayout.show(cardPanel, scriptNode.toOpNode().getId());
		this.currentEditorNode = scriptNode.toOpNode();
		scriptNode.toOpNode().addNodeListener(nodeListener);
		editorMap.get(scriptNode.toOpNode()).addPropertyChangeListener("hasChanges", changeListener);
		updateButtonStates();
		updateLabel();
	}

	private class ScriptTreeRenderer extends OpGraphTreeCellRenderer {
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			JLabel retVal = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			if(value != null && value instanceof DefaultMutableTreeNode) {
				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;
				if(treeNode.getUserObject() != null && treeNode.getUserObject() instanceof OpNode) {
					OpNode node = (OpNode) treeNode.getUserObject();
					if (leaf && editorMap.containsKey(node)) {
						ScriptNodeEditor editor = editorMap.get(node);
						if (editor.hasChanges())
							retVal.setText(retVal.getText() + " *");
					}
				}
			}

			return retVal;
		}
	}

	private final MouseInputAdapter scriptTreeMouseHandler = new MouseInputAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getClickCount() == 2) {
				int row = scriptTree.getRowForLocation(e.getX(), e.getY());
				if(row >= 0 && row < scriptTree.getRowCount()) {
					TreePath treePath = scriptTree.getPathForRow(row);
					if(treePath.getLastPathComponent() instanceof DefaultMutableTreeNode) {
						DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) treePath.getLastPathComponent();
						if(treeNode.getUserObject() instanceof ScriptNode) {
							openScriptNode((ScriptNode) treeNode.getUserObject());
						}
					}
				}
			}
		}
	};

	private final MouseInputAdapter scriptLabelMouseHandler = new MouseInputAdapter() {
		@Override
		public void mousePressed(MouseEvent e) {
			showScriptPopupMenu();
		}
	};

	private final PropertyChangeListener changeListener = (e) -> {
		if(!"hasChanges".equals(e.getPropertyName())) return;
		updateButtonStates();
		updateLabel();
	};

	private final OpGraphListener graphListener = new OpGraphListener() {
		@Override
		public void nodeAdded(OpGraph opGraph, OpNode opNode) {
		}

		@Override
		public void nodeRemoved(OpGraph opGraph, OpNode opNode) {
			ScriptNodeEditor currentEditor = currentEditor();
			if(editorMap.containsKey(opNode)) {
				ScriptNodeEditor removedEditor = editorMap.remove(opNode);
				if(removedEditor == currentEditor) {
					if(editorMap.keySet().size() > 0) {
						openScriptNode((ScriptNode)editorMap.keySet().iterator().next());
					} else {
						cardLayout.show(cardPanel, NO_NODE_PANEL);
						updateLabel();
						updateButtonStates();
					}
				}
			}
		}

		@Override
		public void nodeSwapped(OpGraph opGraph, OpNode opNode, OpNode opNode1) {

		}

		@Override
		public void linkAdded(OpGraph opGraph, OpLink opLink) {

		}

		@Override
		public void linkRemoved(OpGraph opGraph, OpLink opLink) {

		}

	};

	private final OpNodeListener nodeListener = new OpNodeAdapter() {
		@Override
		public void nodePropertyChanged(OpNode node, String propertyName, Object oldValue, Object newValue) {
			if(propertyName.equals("name")) {
				updateLabel();
			}
		}

		@Override
		public void fieldAdded(OpNode node, InputField field) {
		}

		@Override
		public void fieldRemoved(OpNode node, InputField field) {
		}

		@Override
		public void fieldAdded(OpNode node, OutputField field) {
		}

		@Override
		public void fieldRemoved(OpNode node, OutputField field) {
		}

		@Override
		public void fieldRenamed(OpNode node, ContextualItem field) {
		}
	};

}
