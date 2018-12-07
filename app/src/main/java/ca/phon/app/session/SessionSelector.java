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
package ca.phon.app.session;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.CommonModuleFrameCreatedListener;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTree;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellEditor;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellRenderer;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
import ca.phon.util.CollatorFactory;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Displays an interface for selection one or more
 * sessions in a given project.
 *
 */
public class SessionSelector extends TristateCheckBoxTree {

	private static final long serialVersionUID = 5336741342440773144L;

	public static TristateCheckBoxTreeModel createModel(Project project, boolean hideEmptyCorpora) {
		if(project == null)
			return new TristateCheckBoxTreeModel(new DefaultMutableTreeNode("No project"));

		final ProjectTreeNode root = new ProjectTreeNode(project);
		root.setEnablePartialCheck(false);

		// create new tree structure
		Collator collator = CollatorFactory.defaultCollator();
		List<String> corpora = project.getCorpora();
		Collections.sort(corpora, collator);
		for(String corpus:corpora) {
			CorpusTreeNode corpusNode = new CorpusTreeNode(corpus);
			corpusNode.setEnablePartialCheck(false);

			List<String> sessions = project.getCorpusSessions(corpus);
			if(sessions.size() == 0 && hideEmptyCorpora) continue;
			Collections.sort(sessions, collator);
			for(String session:sessions) {
				SessionPath sp = new SessionPath(corpus, session);
				
				SessionTreeNode sessionNode = new SessionTreeNode(sp);
				sessionNode.setEnablePartialCheck(false);
				corpusNode.add(sessionNode);
			}
			root.add(corpusNode);
		}

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
	}

	public Project getProject() {
		return this.project;
	}

	public void setProject(Project project) {
		final Project oldProject = this.project;
		this.project = project;
		super.firePropertyChange("project", oldProject, project);

		setModel(createModel(project, isHideEmptyCorpora()));
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

		super.expandRow(0);
	}

	public TreePath sessionPathToTreePath(SessionPath sessionPath) {
		final TristateCheckBoxTreeNode root = (TristateCheckBoxTreeNode)getModel().getRoot();
		for(int i = 0; i < root.getChildCount(); i++) {
			final TristateCheckBoxTreeNode corpusNode = (TristateCheckBoxTreeNode)root.getChildAt(i);
			if(corpusNode.getUserObject().equals(sessionPath.getCorpus())) {
				for(int j = 0; j < corpusNode.getChildCount(); j++) {
					final TristateCheckBoxTreeNode sessionNode = (TristateCheckBoxTreeNode)corpusNode.getChildAt(j);
					if(sessionNode.getUserObject().equals(sessionPath)) {
						final TreePath checkPath = new TreePath(
								new Object[]{ root, corpusNode, sessionNode });
						return checkPath;
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
			if(checkPath.getPath().length != 3)
				continue;
			
			SessionPath loc = (SessionPath)((SessionTreeNode)checkPath.getPath()[2]).getUserObject();
			retVal.add(loc);
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
					panel.getLabel().setText(sp.getSession());
					if(editor != null) {
						StringBuffer lblTxt = new StringBuffer();
						lblTxt.append("(O) ").append(sp.getSession());
						if(editor.hasUnsavedChanges()) {
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
	
}
