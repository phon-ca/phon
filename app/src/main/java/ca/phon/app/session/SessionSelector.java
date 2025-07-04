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
package ca.phon.app.session;

import ca.hedlund.desktopicons.*;
import ca.hedlund.tst.TernaryTree;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.ui.tristatecheckbox.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.text.Collator;
import java.util.List;
import java.util.*;

/**
 * Displays an interface for selection one or more
 * sessions in a given project.
 *
 */
public class SessionSelector extends TristateCheckBoxTree {

	public static TristateCheckBoxTreeModel createModel(Project project, boolean hideEmptyCorpora) {
		if(project == null)
			return new TristateCheckBoxTreeModel(new DefaultMutableTreeNode("No project"));

		final ProjectTreeNode root = new ProjectTreeNode(project);
		root.setEnablePartialCheck(false);

		return new TristateCheckBoxTreeModel(root);
	}

	/** The project */
	private Project project;

	private boolean hideEmptyCorpora;

	public SessionSelector() {
		this(null, true);
	}

	public SessionSelector(Project project) {
		this(project, true);
	}

	/** Constructor */
	public SessionSelector(Project project, boolean hideEmptyCorpora) {
		super(createModel(project, hideEmptyCorpora));

		this.project = project;
		this.hideEmptyCorpora = hideEmptyCorpora;

		init();
		new ProjectTreeWorker().execute();
	}

	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		final Project oldProject = this.project;
		this.project = project;
		super.firePropertyChange("project", oldProject, project);

		setModel(createModel(project, isHideEmptyCorpora()));
		new ProjectTreeWorker().execute();
	}

	public boolean isHideEmptyCorpora() {
		return this.hideEmptyCorpora;
	}

	public void setHideEmptyCorpora(boolean hideEmptyCorpora) {
		this.hideEmptyCorpora = hideEmptyCorpora;
		setModel(createModel(project, hideEmptyCorpora));
	}

	private void init() {
		final SessionSelectorCellRenderer renderer = new SessionSelectorCellRenderer();

		final SessionSelectorCellRenderer editorRenderer = new SessionSelectorCellRenderer();
		final TristateCheckBoxTreeCellEditor editor = new TristateCheckBoxTreeCellEditor(this, editorRenderer);

		setCellRenderer(renderer);
		setCellEditor(editor);

	}

	public TreePath sessionPathToTreePath(SessionPath sessionPath) {
		final TristateCheckBoxTreeNode root = (TristateCheckBoxTreeNode)getModel().getRoot();

		if(sessionPath.getFolder().equals(".")) {
			// root node
			for(int j = 0; j < root.getChildCount(); j++) {
				final TristateCheckBoxTreeNode sessionNode = (TristateCheckBoxTreeNode)root.getChildAt(j);
				if(sessionNode.isLeaf() && sessionNode.getUserObject().equals(sessionPath)) {
					final TreePath checkPath = new TreePath(
							new Object[]{ root, sessionNode });
					return checkPath;
				}
			}
		} else {
			for (int i = 0; i < root.getChildCount(); i++) {
				final TristateCheckBoxTreeNode corpusNode = (TristateCheckBoxTreeNode) root.getChildAt(i);
				if (corpusNode.getUserObject().equals(sessionPath.getFolder())) {
					for (int j = 0; j < corpusNode.getChildCount(); j++) {
						final TristateCheckBoxTreeNode sessionNode = (TristateCheckBoxTreeNode) corpusNode.getChildAt(j);
						if (sessionNode.getUserObject().equals(sessionPath)) {
							final TreePath checkPath = new TreePath(
									new Object[]{root, corpusNode, sessionNode});
							return checkPath;
						}
					}
				}
			}
		}
		return null;
	}

	public List<SessionPath> getSelectedSessions() {
		List<SessionPath> retVal =
			new ArrayList<SessionPath>();

		List<TreePath> checkPaths = super.getCheckedPaths();

		for(TreePath checkPath:checkPaths) {
			final TristateCheckBoxTreeNode checkNode = (TristateCheckBoxTreeNode)checkPath.getLastPathComponent();
			if(checkNode.getUserObject() instanceof SessionPath loc) {
				retVal.add(loc);
			}
		}

		Collections.sort(retVal, (sp1, sp2) -> sp1.toString().compareTo(sp2.toString()) );

		return retVal;
	}

	public void setSelectedSessions(List<SessionPath> selectedSessions) {
		super.clearSelection();

		for(SessionPath sessionPath:selectedSessions) {
			final TreePath path = sessionPathToTreePath(sessionPath);
			super.setCheckingStateForPath(path, TristateCheckBoxState.CHECKED);
			expandPath(path.getParentPath());
		}
	}
	
	public TristateCheckBoxTreeModel getCheckboxTreeModel() {
		return (TristateCheckBoxTreeModel)getModel();
	}
	
	public static class ProjectTreeNode extends TristateCheckBoxTreeNode {
		
		public ProjectTreeNode(Project project) {
			super(project);
		}
		
	}
	
	public static class CorpusTreeNode extends TristateCheckBoxTreeNode {
		
		public CorpusTreeNode(String corpusName) {
			super(corpusName);
		}
		
	}
	
	public static class SessionTreeNode extends TristateCheckBoxTreeNode {
		
		public SessionTreeNode(SessionPath sessionPath) {
			super(sessionPath);
		}
		
	}
	
	
	public class SessionSelectorCellRenderer extends TristateCheckBoxTreeCellRenderer {

		final ImageIcon folderIcon = (OSInfo.isMacOs() ? IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.GenericFolderIcon, IconSize.SMALL)
				: OSInfo.isWindows() ? IconManager.getInstance().getSystemStockIcon(WindowsStockIcon.FOLDER, IconSize.SMALL)
						: IconManager.getInstance().getIcon("places/folder", IconSize.SMALL));
		
		final ImageIcon sessionIcon = IconManager.getInstance().getSystemIconForFileType("xml", IconSize.SMALL);
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			Component retVal = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			if(value instanceof TristateCheckBoxTreeNode) {
				TristateCheckBoxTreeNode node = (TristateCheckBoxTreeNode)value;
				TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel panel = 
						(TristateCheckBoxTreeCellRenderer.TristateCheckBoxTreeNodePanel)retVal;
				
				if(node instanceof SessionTreeNode) {
					SessionPath sp = (SessionPath)node.getUserObject();
					SessionEditor editor = (SessionEditor)sp.getExtension(SessionEditor.class);
					panel.getLabel().setText(sp.getSessionFile());
					if(editor != null) {
						StringBuffer lblTxt = new StringBuffer();
						lblTxt.append("(O) ").append(sp.getSessionFile());
						if(editor.isModified()) {
							lblTxt.append("*");
						}
						panel.getLabel().setText(lblTxt.toString());
					}
					panel.getLabel().setIcon(sessionIcon);
				} else if(node instanceof ProjectTreeNode || node instanceof CorpusTreeNode) {
					panel.getLabel().setIcon(folderIcon);
				}
			}
			
			return retVal;
		}
		
	}


	private record ProjectTreeInsertionData(TristateCheckBoxTreeNode parent, TristateCheckBoxTreeNode child, int index) {
		public ProjectTreeInsertionData(TristateCheckBoxTreeNode parent, TristateCheckBoxTreeNode child) {
			this(parent, child, -1);
		}
	}

	/**
	 * Worker for building the tree structure of the project
	 *
	 */
	public class ProjectTreeWorker extends SwingWorker<Void, ProjectTreeInsertionData> {

		@Override
		protected Void doInBackground() throws Exception {
			if(getModel().getRoot() instanceof ProjectTreeNode root) {
				// create new tree structure
				final Iterator<String> corpusNames = project.getCorpusIterator();
				while (corpusNames.hasNext()) {
					String corpus = corpusNames.next();
					TristateCheckBoxTreeNode corpusNode = new CorpusTreeNode(corpus);
					corpusNode.setEnablePartialCheck(false);

					final Iterator<String> sessionNames = project.getSessionIterator(corpus);
					if (!sessionNames.hasNext() && hideEmptyCorpora) continue;
					if (".".equals(corpus)) {
						corpusNode = root;
					} else {
						publish(new ProjectTreeInsertionData(root, corpusNode));
					}

					while (sessionNames.hasNext()) {
						final String session = sessionNames.next();
						SessionPath sp = new SessionPath(corpus, session);

						SessionTreeNode sessionNode = new SessionTreeNode(sp);
						sessionNode.setEnablePartialCheck(false);
						publish(new ProjectTreeInsertionData(corpusNode, sessionNode));
					}
				}
			}
			return null;
		}

		@Override
		protected void process(List<ProjectTreeInsertionData> chunks) {
			for(ProjectTreeInsertionData chunk:chunks) {
				TristateCheckBoxTreeNode parent = chunk.parent;
				TristateCheckBoxTreeNode child = chunk.child;
				int index = chunk.index;

				if(index == -1)
					parent.add(child);
				else
					parent.insert(child, index);
				int childIdx = parent.getIndex(child);
				getCheckboxTreeModel().nodesWereInserted(parent, new int[]{ childIdx });
			}
		}

		@Override
		protected void done() {
			try {
				get();
				expandRow(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
