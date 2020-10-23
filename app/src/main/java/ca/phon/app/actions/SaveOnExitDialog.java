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
package ca.phon.app.actions;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

import javax.swing.*;
import javax.swing.tree.*;

import ca.phon.project.*;
import ca.phon.ui.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.layout.*;
import ca.phon.ui.tristatecheckbox.*;
import ca.phon.util.icons.*;

/**
 * Save changes on exit dialog.
 * 
 * 
 */
public class SaveOnExitDialog extends JDialog {
	
	private static final long serialVersionUID = -5407557304432524751L;

	public static enum QuitOption {
		SaveSelected,
		DiscardAll,
		Cancel
	};

	// UI
	private DialogHeader header;
	private TristateCheckBoxTree checkboxTree;
	private JButton discardAllButton;
	private JButton saveSelectedButton;
	private JButton cancelButton;
	
	// close status
	private QuitOption closeStatus = QuitOption.Cancel;
	
	public SaveOnExitDialog(CommonModuleFrame owner) {
		super(owner, "Phon : Quit", true);
		init();
	}
	
	private void init() {
		super.setLayout(new BorderLayout());
		
		header = new DialogHeader("Quit Phon", "Save changes before exit?");
		
		checkboxTree = new TristateCheckBoxTree(createTree());
		checkboxTree.setCellRenderer(new EditorTreeRenderer());
		checkboxTree.setCellEditor(new EditorTreeEditor(checkboxTree));
		checkboxTree.setRootVisible(false);
		checkboxTree.expandAll();
		
		saveSelectedButton = new JButton("Save selected");
		saveSelectedButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				closeStatus = QuitOption.SaveSelected;
				setVisible(false);
			}
			
		});
		
		discardAllButton = new JButton("Discard all");
		discardAllButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				closeStatus = QuitOption.DiscardAll;
				setVisible(false);
			}
			
		});
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				closeStatus = QuitOption.Cancel;
				setVisible(false);
			}
			
		});
		
		final JComponent btnBar = 
				ButtonBarBuilder.buildOkCancelBar(saveSelectedButton, cancelButton, discardAllButton);
		
		add(header, BorderLayout.NORTH);
		add(new JScrollPane(checkboxTree), BorderLayout.CENTER);
		add(btnBar, BorderLayout.SOUTH);
		
		getRootPane().setDefaultButton(saveSelectedButton);
		saveSelectedButton.requestFocusInWindow();
	}
	
	private TristateCheckBoxTreeNode createTree() {
		final TristateCheckBoxTreeNode root = new TristateCheckBoxTreeNode("Unsaved Data");
		root.setEnablePartialCheck(false);
		
		final Set<CommonModuleFrame> editorsWithChanges = 
				new TreeSet<>( (e1, e2) -> e1.getTitle().compareTo(e2.getTitle()) );
		final Set<Project> openProjects = new TreeSet<>( (p1, p2) -> p1.getName().compareTo(p2.toString()) );
		
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			if(cmf.hasUnsavedChanges()) {
				editorsWithChanges.add(cmf);
				
				final Project project = cmf.getExtension(Project.class);
				if(project != null) {
					openProjects.add(project);
				}
			}
		}
		
		// setup tree
		for(final Project project:openProjects) {
			final TristateCheckBoxTreeNode projectNode = new TristateCheckBoxTreeNode(project);
			projectNode.setEnablePartialCheck(false);
			
			final List<CommonModuleFrame> projectEditors =
					editorsWithChanges.stream()
						.filter( (e) -> e.getExtension(Project.class) == project )
						.collect(Collectors.toList());
			for(CommonModuleFrame cmf:projectEditors) {
				final TristateCheckBoxTreeNode editorNode = new TristateCheckBoxTreeNode(cmf);
				editorNode.setEnablePartialCheck(false);
				
				projectNode.add(editorNode);
			}
			
			root.add(projectNode);
		}
		
		final List<CommonModuleFrame> noProjectEditors = 
				editorsWithChanges.stream()
					.filter( (e) -> e.getExtension(Project.class) == null )
					.collect(Collectors.toList());
		final TristateCheckBoxTreeNode otherNode = new TristateCheckBoxTreeNode("Other");
		for(CommonModuleFrame cmf:noProjectEditors) {
			final TristateCheckBoxTreeNode editorNode = new TristateCheckBoxTreeNode(cmf);
			editorNode.setEnablePartialCheck(false);
			
			otherNode.add(editorNode);
		}
		if(otherNode.getChildCount() > 0) {
			root.add(otherNode);
		}
		
		// check all
		root.setCheckingState(TristateCheckBoxState.CHECKED);
		return root;
	}
	
	private class EditorTreeRenderer extends TristateCheckBoxTreeCellRenderer {
		
		public EditorTreeRenderer() {
			ImageIcon icn = IconManager.getInstance().getIcon(
					"blank", IconSize.SMALL);
			
			setLeafIcon(icn);
			setOpenIcon(icn);
			setClosedIcon(icn);
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean arg2, boolean arg3, boolean arg4, int arg5, boolean arg6) {
			Component retVal = super.getTreeCellRendererComponent(tree, value, arg2, arg3, arg4, arg5,
					arg6);
			
			if(value instanceof DefaultMutableTreeNode && retVal instanceof
					TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel) {
				final TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel panel = 
						(TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel)retVal;
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
				final Object userObj = node.getUserObject();
				if(userObj instanceof CommonModuleFrame) {
					panel.getLabel().setText(((CommonModuleFrame)userObj).getTitle());
				} else if(userObj instanceof Project) {
					panel.getLabel().setText(((Project)userObj).getName());
				}
			}
			
			return retVal;
		}
		
	}
	
	private class EditorTreeEditor extends TristateCheckBoxTreeCellEditor {
		
		public EditorTreeEditor(JTree tree) {
			super(tree, new EditorTreeRenderer());
		}
		
	}

	public QuitOption getCloseStatus() {
		return closeStatus;
	}

	public void setCloseStatus(QuitOption closeStatus) {
		this.closeStatus = closeStatus;
	}

	public List<CommonModuleFrame> getSelectedEditors() {
		List<CommonModuleFrame> retVal = new ArrayList<CommonModuleFrame>();
		
		final List<TreePath> selectedPaths = checkboxTree.getCheckedPaths();
		for(TreePath path:selectedPaths) {
			if(path.getLastPathComponent() instanceof TristateCheckBoxTreeNode) {
				final TristateCheckBoxTreeNode node = (TristateCheckBoxTreeNode)path.getLastPathComponent();
				if(node.getUserObject() instanceof CommonModuleFrame) {
					final CommonModuleFrame cmf = (CommonModuleFrame)node.getUserObject();
					retVal.add(cmf);
				}
			}
		}
		
		return retVal;
	}
	
	public QuitOption showDialog() {
		pack();
		// center dialog on screen
		Dimension size = new Dimension(450, 400);
		Dimension ss = 
			Toolkit.getDefaultToolkit().getScreenSize();
		
		int xPos = ss.width / 2 - (size.width/2);
		int yPos = ss.height / 2 - (size.height/2);
		
		setBounds(xPos, yPos, size.width, size.height);
		
		setVisible(true);
		
		// wait...
		
		return closeStatus;
	}
	
}