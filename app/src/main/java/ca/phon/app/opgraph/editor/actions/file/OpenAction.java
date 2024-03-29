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
package ca.phon.app.opgraph.editor.actions.file;

import ca.phon.app.log.LogUtil;
import ca.phon.app.menu.file.OpenFileHistory;
import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.app.OpgraphIO;
import ca.phon.ui.nativedialogs.*;
import ca.phon.util.RecentFiles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;

public class OpenAction extends OpgraphEditorAction {
	
	private static final long serialVersionUID = 1416397464535529114L;
	
	public final static String TXT = "Open...";
	
	public final static String DESC = "Open graph";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_O,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());

	private File openFile;
	
	public OpenAction(OpgraphEditor editor) {
		this(editor, null);
	}
	
	public OpenAction(OpgraphEditor editor, File openFile) {
		super(editor);
		
		this.openFile = openFile;
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		boolean useCurrentWindow = true;
		if(getEditor().getCurrentFile() != null
				|| getEditor().hasUnsavedChanges()) {
			// ask to use new window
			final String opts[] = new String[] { "Use current window", "Use new window" };
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(getEditor());
			props.setOptions(opts);
			props.setTitle("Choose window");
			props.setHeader(props.getTitle());
			props.setMessage("Use current window for new document?");
			props.setRunAsync(false);
			
			final int ret = NativeDialogs.showMessageDialog(props);
			useCurrentWindow = (ret == 0);
		}
		
		if(useCurrentWindow && getEditor().hasUnsavedChanges()) {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(getEditor());
			props.setOptions(MessageDialogProperties.yesNoCancelOptions);
			props.setTitle("Save changes");
			props.setHeader(props.getTitle());
			props.setMessage("Save changes before starting a new graph?");
			props.setRunAsync(false);
			
			final int ret = NativeDialogs.showMessageDialog(props);
			if(ret == 0) /* yes */ {
				(new SaveAction(getEditor())).actionPerformed(arg0);
			} else if(ret == 1) /* no */ {
				// do nothing
			} else /* cancel */ {
				return;
			}
		}
		
		if(openFile == null) {
			final OpenDialogProperties props = new OpenDialogProperties();
			props.setParentWindow(getEditor());
			props.setCanChooseFiles(true);
			props.setCanChooseDirectories(false);
			props.setAllowMultipleSelection(false);
			props.setTitle("Open Graph");
			props.setRunAsync(false);
			props.setFileFilter(new OpgraphFileFilter());
			
			final List<String> savePath = NativeDialogs.showOpenDialog(props);
			if(savePath != null && savePath.size() > 0) {
				openFile = new File(savePath.get(0));
			} else {
				return;
			}
		}
		try {
			openFile(openFile, useCurrentWindow);
		} catch (IOException | ClassNotFoundException e) {
			LogUtil.severe(e);
		}
	}
	
	private void openFile(File file, boolean useCurrentWindow) throws ClassNotFoundException, IOException {
		final OpGraph graph = OpgraphIO.read(file);
		final OpgraphEditorModelFactory factory = new OpgraphEditorModelFactory();
		final OpgraphEditorModel model = factory.fromGraph(graph);
		
		if(useCurrentWindow) {
			getEditor().setModel(model);
			getEditor().setCurrentFile(file);
		} else {
			final OpgraphEditor editor = new OpgraphEditor(model);
			editor.setCurrentFile(file);
			editor.pack();
			editor.setSize(1064, 768);
			editor.setLocationByPlatform(true);
			editor.setVisible(true);
		}
		
		// add to recent documents list
		final RecentFiles recentFiles = new RecentFiles(OpgraphEditor.RECENT_DOCS_PROP);
		recentFiles.addToHistory(file);
		
		OpenFileHistory openFileHistory = new OpenFileHistory();
		openFileHistory.addToHistory(file);
	}

}
