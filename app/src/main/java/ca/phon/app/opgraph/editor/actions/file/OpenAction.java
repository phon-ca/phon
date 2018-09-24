/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.editor.actions.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.KeyStroke;

import ca.phon.app.opgraph.OpgraphIO;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.editor.OpgraphEditorModelFactory;
import ca.phon.app.opgraph.editor.OpgraphFileFilter;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.opgraph.OpGraph;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.util.RecentFiles;

public class OpenAction extends OpgraphEditorAction {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(OpenAction.class.getName());

	private static final long serialVersionUID = 1416397464535529114L;
	
	public final static String TXT = "Open...";
	
	public final static String DESC = "Open graph";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_O,
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

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
			props.setInitialFolder(getEditor().getModel().getDefaultFolder());
			
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
			e.printStackTrace();
			LOGGER.error( e.getLocalizedMessage(), e);
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
	}

}
