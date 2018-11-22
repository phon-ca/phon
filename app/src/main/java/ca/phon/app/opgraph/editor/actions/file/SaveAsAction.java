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
package ca.phon.app.opgraph.editor.actions.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.ui.toast.ToastFactory;
import ca.phon.util.RecentFiles;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class SaveAsAction extends OpgraphEditorAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(SaveAsAction.class.getName());

	private static final long serialVersionUID = -3563703815236430754L;
	
	public final static String TXT = "Save as...";
	
	public final static String DESC = "Save graph to file";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_S,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | KeyEvent.SHIFT_MASK);
	
	public final static ImageIcon ICON = 
			IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL);

	public SaveAsAction(OpgraphEditor editor) {
		super(editor);
	
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
		putValue(SMALL_ICON, ICON);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		if(getEditor().chooseFile()) {
			// call save on the editor
			try {
				getEditor().saveData();
				
				if(getEditor().getCurrentFile() != null) {
					final RecentFiles recentFiles = new RecentFiles(OpgraphEditor.RECENT_DOCS_PROP);
					recentFiles.addToHistory(getEditor().getCurrentFile());
				}
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
				ToastFactory.makeToast(e.getLocalizedMessage()).start(getEditor());
				Toolkit.getDefaultToolkit().beep();
			}
		}
	}

}
