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

import ca.phon.app.session.SessionSelector.SessionTreeNode;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.project.Project;
import ca.phon.session.SessionPath;
import ca.phon.ui.*;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel;

import java.awt.event.*;

/**
 * Updates the session selector when session editors open/close.
 */
public class SessionSelectorActiveEditorSupport {
	
	public SessionSelectorActiveEditorSupport() {
		super();
	}
	
	public void install(SessionSelector selector) {
		updateActiveEditors(selector);
		CommonModuleFrame.addNewWindowListener(this, new NewFrameListener(selector));
	}
	
	private void updateActiveEditors(SessionSelector selector) {
		Project project = selector.getProject();
		selector.getSelectionModel().clearSelection();
		for(String corpus:project.getCorpora()) {
			for(String session:project.getCorpusSessions(corpus)) {
				SessionPath sessionPath = new SessionPath(corpus, session);
				
				var treePath = selector.sessionPathToTreePath(sessionPath);
				if(treePath != null && treePath.getLastPathComponent() instanceof SessionSelector.SessionTreeNode) {
					SessionPath sp = (SessionPath)((SessionTreeNode)treePath.getLastPathComponent()).getUserObject();
					SessionEditor editor  = findEditorForSession(project, sessionPath);
					
					if(sp.getExtension(SessionEditor.class) != null
							&& sp.getExtension(SessionEditor.class) == editor) continue;
					
					if(editor != null) {
						editor.addWindowListener(new EditorWindowListener(selector, sp));
					}
					sp.putExtension(SessionEditor.class, editor);
					
					TristateCheckBoxTreeModel model = (TristateCheckBoxTreeModel)selector.getModel();
					model.valueForPathChanged(treePath, sp);
				}
			}
		}
	}

	private SessionEditor findEditorForSession(Project project, SessionPath sessionPath) {
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			if(cmf instanceof SessionEditor) {
				SessionEditor e = (SessionEditor)cmf;
				if(e.getProject() == project
						&& e.getSession().getCorpus().equals(sessionPath.getCorpus())
						&& e.getSession().getName().equals(sessionPath.getSession())) {
					return e;
				}
			}
		}
		return null;
	}
	
	private class NewFrameListener implements CommonModuleFrameCreatedListener {
		
		private final SessionSelector selector;
		
		public NewFrameListener(SessionSelector selector) {
			super();
			this.selector = selector;
		}

		@Override
		public void newWindow(CommonModuleFrame cmf) {
			if(cmf.getExtension(Project.class) == selector.getProject()
					&& cmf instanceof SessionEditor) {
				updateActiveEditors(selector);
			}
		}
		
	}
	
	private class EditorWindowListener implements WindowListener {
		
		private final SessionPath sp;
		
		private final SessionSelector selector;
		
		public EditorWindowListener(SessionSelector selector, SessionPath sp) {
			super();
			this.sp = sp;
			this.selector = selector;
		}

		@Override
		public void windowOpened(WindowEvent e) {
		}

		@Override
		public void windowClosing(WindowEvent e) {
		}

		@Override
		public void windowClosed(WindowEvent e) {
			sp.putExtension(SessionEditor.class, null);
			var treePath = selector.sessionPathToTreePath(sp);
			if(treePath != null) {
				selector.getSelectionModel().clearSelection();
				((TristateCheckBoxTreeModel)selector.getModel()).valueForPathChanged(treePath, sp);
			}
		}

		@Override
		public void windowIconified(WindowEvent e) {
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
		}

		@Override
		public void windowActivated(WindowEvent e) {
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
		}
		
	}
}
