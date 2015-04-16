/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.actions;

import it.cnr.imaa.essi.lablib.gui.checkboxtree.CheckboxTree;
import it.cnr.imaa.essi.lablib.gui.checkboxtree.DefaultCheckboxTreeCellRenderer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

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

	// the list of editors with changes
	private List<CommonModuleFrame> editors;
	
	// UI
	private DialogHeader header;
	private CheckboxTree checkboxTree;
	private JButton discardAllButton;
	private JButton saveSelectedButton;
	private JButton cancelButton;
	
	// close status
	private QuitOption closeStatus = QuitOption.Cancel;
	
	public SaveOnExitDialog(CommonModuleFrame owner, List<CommonModuleFrame> editors) {
		super(owner, "Phon : Quit", true);
		this.editors = editors;
		init();
	}
	
	private void init() {
		super.setLayout(new BorderLayout());
		
		header = new DialogHeader("Quit Phon", "Save changes before exit?");
		
		checkboxTree = new CheckboxTree();
		checkboxTree.setRootVisible(false);
		checkboxTree.setModel(new EditorTreeModel());
		checkboxTree.setCellRenderer(new EditorTreeRenderer());
		checkboxTree.expandAll();
		// check all
		checkboxTree.getCheckingModel().toggleCheckingPath(
				new TreePath(checkboxTree.getModel().getRoot()));
		
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
	
	private class EditorTreeRenderer extends DefaultCheckboxTreeCellRenderer {
		
		public EditorTreeRenderer() {
			ImageIcon icn = IconManager.getInstance().getIcon(
					"blank", IconSize.SMALL);
			
			setLeafIcon(icn);
			setOpenIcon(icn);
			setClosedIcon(icn);
		}

		@Override
		public Component getTreeCellRendererComponent(JTree arg0, Object arg1,
				boolean arg2, boolean arg3, boolean arg4, int arg5, boolean arg6) {
			Component retVal = super.getTreeCellRendererComponent(arg0, arg1, arg2, arg3, arg4, arg5,
					arg6);
			
			String labelText = arg1.toString();
			
			if(arg1 instanceof CommonModuleFrame) {
				// get the corpus.id of the session
				CommonModuleFrame editor = (CommonModuleFrame)arg1;
				labelText = editor.getTitle();
			} else if(arg1 instanceof Project) {
				labelText = ((Project)arg1).getName();
			}
			super.label.setText(labelText);
			
			return retVal;
		}
		
	}
	
	private class EditorTreeModel implements TreeModel {

		private final String _root = "Unsaved Data";
		
		@Override
		public void addTreeModelListener(TreeModelListener l) {
		}

		@Override
		public Object getChild(Object parent, int index) {
			Object retVal = null;
			
			if(parent == _root) {
				List<Project> projects = getProjects();
				retVal = projects.get(index);
			} else if (parent instanceof Project) {
				List<CommonModuleFrame> editors = getEditors((Project)parent);
				retVal = editors.get(index);
			}
			
			return retVal;
		}

		@Override
		public int getChildCount(Object parent) {
			int retVal = 0;
			
			if(parent == _root) {
				List<Project> projects = getProjects();
				retVal = projects.size();
			} else if (parent instanceof Project) {
				List<CommonModuleFrame> editors = getEditors((Project)parent);
				retVal = editors.size();
			}
			
			return retVal;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			int retVal = -1;
			
			if(parent == _root) {
				List<Project> projects = getProjects();
				
				for(int i = 0; i < projects.size(); i++) {
					Project p = projects.get(i);
					if(p == child) {
						retVal = i;
						break;
					}
				}
			} else if (parent instanceof Project) {
				List<CommonModuleFrame> editors = getEditors((Project)parent);
				
				for(int i = 0; i < editors.size(); i++) {
					CommonModuleFrame editor = editors.get(i);
					if(editor == child) {
						retVal = i;
						break;
					}
				}
			}
			
			return retVal;
		}

		@Override
		public Object getRoot() {
			return _root;
		}

		@Override
		public boolean isLeaf(Object node) {
			return (node instanceof CommonModuleFrame);
		}

		@Override
		public void removeTreeModelListener(TreeModelListener l) {
		}

		@Override
		public void valueForPathChanged(TreePath path, Object newValue) {
		}
		
		private List<Project> getProjects() {
			List<Project> retVal = new ArrayList<Project>();
			
			for(CommonModuleFrame editor:editors) {
				final Project pfe = editor.getExtension(Project.class);
				if(pfe != null && !retVal.contains(pfe)) {
					retVal.add(pfe);
				}
			}
			
			return retVal;
		}
		
		private List<CommonModuleFrame> getEditors(Project p) {
			List<CommonModuleFrame> retVal = new ArrayList<CommonModuleFrame>();
			
			for(CommonModuleFrame editor:editors) {
				final Project pfe = editor.getExtension(Project.class);
				if(pfe != null && pfe == p) {
					retVal.add(editor);
				}
			}
			
			return retVal;
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
		
		TreePath[] selectedPaths = 
			checkboxTree.getCheckingPaths();
		for(TreePath p:selectedPaths) {
			if(checkboxTree.getModel().isLeaf(p.getLastPathComponent())) {
				CommonModuleFrame editor = 
					(CommonModuleFrame)p.getLastPathComponent();
				retVal.add(editor);
			}
		}
		
		return retVal;
	}
	
	public QuitOption showDialog() {
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