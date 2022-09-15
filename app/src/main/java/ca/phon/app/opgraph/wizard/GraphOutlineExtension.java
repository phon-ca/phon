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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import ca.phon.app.opgraph.wizard.edits.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.components.*;
import ca.phon.ui.action.*;
import ca.phon.ui.menu.*;
import ca.phon.util.icons.*;

/**
 * Setup cell renderer and mouse handler on {@link GraphOutline} view
 * for {@link WizardExtension}.
 *
 */
public class GraphOutlineExtension {

	public static void install(GraphDocument document, GraphOutline outline, WizardExtension wizardExtension) {
		final GraphOutlineExtension ext = new GraphOutlineExtension(document, outline, wizardExtension);
		ext.installCellRenderer();
		ext.installContextListener();
	}

	private final GraphDocument document;

	private final GraphOutline outline;

	private final WizardExtension wizardExtension;

	public GraphOutlineExtension(GraphDocument document, GraphOutline outline, WizardExtension wizardExtension) {
		super();

		this.document = document;
		this.outline = outline;
		this.wizardExtension = wizardExtension;
	}

	public void installCellRenderer() {
		getGraphOutline().getTree().setCellRenderer(
				new WizardExtensionCellRenderer(getGraphOutline().getTree().getCellRenderer()));

		final OpGraphTreeModel model = getGraphOutline().getModel();
		getWizardExtension().addWizardExtensionListener( (e) -> {
			switch(e.getEventType()) {
			case NODE_MAKRED_AS_OPTIONAL:
			case NODE_MAKRED_AS_NONOPTIONAL:
			case NODE_ADDED_TO_SETTINGS:
			case NODE_REMOVED_FROM_SETTINGS:
			case NODE_MARKED_AS_REQUIRED:
			case NODE_MAKRED_AS_NOT_REQUIRED:
				model.nodeChanged(e.getNode());
				break;

			default:
				break;
			}
		});
	}

	public void installContextListener() {
		getGraphOutline().addContextMenuListener(contextListener);
	}

	public WizardExtension getWizardExtension() {
		return this.wizardExtension;
	}

	public GraphOutline getGraphOutline() {
		return this.outline;
	}

	public GraphDocument getDocument() {
		return this.document;
	}

	private final PopupMenuListener contextListener = new PopupMenuListener() {

		@Override
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			final WizardExtension ext = getWizardExtension();
			final MenuBuilder builder = new MenuBuilder((JPopupMenu)e.getSource());

			final TreeSelectionModel selectionModel = getGraphOutline().getTree().getSelectionModel();
			if(selectionModel.getSelectionCount() == 1 && selectionModel.getLeadSelectionRow() > 0) {
				builder.addSeparator(".", "analysis");

				final TreePath selectedPath = selectionModel.getSelectionPath();
				final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
				final OpNode node = (OpNode)treeNode.getUserObject();

				boolean isSettingButNotStep = ext.containsNode(node) && !ext.isNodeForced(node);
				boolean isStep = ext.containsNode(node) && ext.isNodeForced(node);

				if(!isSettingButNotStep) {
					final PhonUIAction<OpNode> toggleNodeAsStepAct = PhonUIAction.eventConsumer(GraphOutlineExtension.this::onToggleNodeAsStep, node);
					toggleNodeAsStepAct.putValue(PhonUIAction.NAME, "Show as step");
					toggleNodeAsStepAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show node settings as a wizard step (and in advanced settings)");
					toggleNodeAsStepAct.putValue(PhonUIAction.SELECTED_KEY, isStep);
					builder.addItem(".@analysis", new JCheckBoxMenuItem(toggleNodeAsStepAct));
				}

				if(!isStep) {
					final PhonUIAction<OpNode> toggleNodeSettingsAct = PhonUIAction.eventConsumer(GraphOutlineExtension.this::onToggleNodeSettings, node);
					toggleNodeSettingsAct.putValue(PhonUIAction.NAME, "Show in advanced settings");
					toggleNodeSettingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show node settings in advanced settings");
					toggleNodeSettingsAct.putValue(PhonUIAction.SELECTED_KEY, isSettingButNotStep);
					builder.addItem(".", new JCheckBoxMenuItem(toggleNodeSettingsAct));
				}

				final PhonUIAction<OpNode> toggleOptionalAct = PhonUIAction.eventConsumer(GraphOutlineExtension.this::onToggleNodeOptional, node);
				toggleOptionalAct.putValue(PhonUIAction.NAME, "Optional node");
				toggleOptionalAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Make execution of this node optional");
				toggleOptionalAct.putValue(PhonUIAction.SELECTED_KEY, ext.isNodeOptional(node));
				builder.addItem(".", new JCheckBoxMenuItem(toggleOptionalAct));
			}
		}

		@Override
		public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		}

		@Override
		public void popupMenuCanceled(PopupMenuEvent e) {
		}
	};

	public void onToggleNodeSettings(PhonActionEvent<OpNode> pae) {
		final OpNode node = pae.getData();
		final NodeWizardSettingsEdit edit = new NodeWizardSettingsEdit(
				getDocument().getGraph(), getWizardExtension(), node,
					!getWizardExtension().containsNode(node), false);
		getDocument().getUndoSupport().postEdit(edit);
	}

	public void onToggleNodeAsStep(PhonActionEvent<OpNode> pae) {
		final OpNode node = pae.getData();
		final NodeWizardSettingsEdit edit = new NodeWizardSettingsEdit(
				getDocument().getGraph(), getWizardExtension(), node,
					!getWizardExtension().containsNode(node), true);
		getDocument().getUndoSupport().postEdit(edit);
	}

	public void onToggleNodeOptional(PhonActionEvent<OpNode> pae) {
		final OpNode node = pae.getData();
		final NodeWizardOptionalsEdit edit = new NodeWizardOptionalsEdit(
				getDocument().getGraph(), getWizardExtension(), node,
					!getWizardExtension().isNodeOptional(node), true);
		getDocument().getUndoSupport().postEdit(edit);
	}

	private class WizardExtensionCellRenderer extends DefaultTreeCellRenderer {

		private TreeCellRenderer parentRenderer;

		private ImageIcon oIcon;

		private ImageIcon sIcon;

		private ImageIcon rIcon;

		public WizardExtensionCellRenderer(TreeCellRenderer parent) {
			super();

			this.parentRenderer = parent;

			final IconManager iconManager = IconManager.getInstance();
			oIcon = iconManager.createGlyphIcon(Character.valueOf('O'), UIManager.getFont("Label.font").deriveFont(Font.BOLD), Color.BLACK,
					new Color(255, 255, 255));
			sIcon = iconManager.createGlyphIcon(Character.valueOf('S'), UIManager.getFont("Label.font"), Color.black, Color.WHITE);
			rIcon =  iconManager.createGlyphIcon(Character.valueOf('R'), UIManager.getFont("Label.font"), Color.black, Color.WHITE);
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			final WizardExtension ext = getWizardExtension();
			JLabel retVal = (JLabel) parentRenderer.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			if(value instanceof DefaultMutableTreeNode) {
				final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)value;

				if(treeNode.getUserObject() != null && treeNode.getUserObject() instanceof OpNode) {
					final OpNode node = (OpNode)treeNode.getUserObject();

					final List<ImageIcon> icons = new ArrayList<>();
					icons.add((ImageIcon)retVal.getIcon());

					if(ext.isNodeOptional(node)) {
						icons.add(oIcon);
					}

					if(ext.containsNode(node)) {
						if(ext.isNodeForced(node)) {
							icons.add(rIcon);
						} else {
							icons.add(sIcon);
						}
					}

					final ImageIcon icn = IconManager.getInstance().createIconStrip(icons.toArray(new ImageIcon[0]));
					retVal.setIcon(icn);
				}
			}

			return retVal;
		}

	}

}
