package ca.phon.app.query.actions;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import ca.phon.app.hooks.*;
import ca.phon.app.log.*;
import ca.phon.app.menu.file.*;
import ca.phon.query.script.*;
import ca.phon.ui.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.icons.*;

/**
 * Export query to a file on disk.
 */
public class ExportQueryAction extends HookableAction {
	
	private QueryScript queryScript;
	
	private String initialFolder;
	
	private String queryName;
	
	public ExportQueryAction(QueryScript queryScript, String initialFolder, String queryName) {
		super();
		
		this.queryScript = queryScript;
		this.initialFolder = initialFolder;
		this.queryName = queryName;
	
		final ImageIcon saveIcon = IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL);
		putValue(SMALL_ICON, saveIcon);
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		// attempt to create target folder if it doesn't exist
		if(initialFolder != null) {
			final File folder = new File(initialFolder);
			if(!folder.exists()) {
				folder.mkdirs();
			}
			if(!folder.isDirectory()) {
				LogUtil.warning("Target path could not be created or is not a folder");
				initialFolder = null;
			}
		}
		
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setCanCreateDirectories(true);
		props.setFileFilter(FileFilter.xmlFilter);
		props.setInitialFolder(initialFolder);
		
		if(queryName != null) {
			props.setInitialFile(queryName);
		}
		props.setNameFieldLabel("Query name");
		props.setPrompt("Save Query");
		props.setRunAsync(true);
		props.setListener(this::dialogClosed);
		
		NativeDialogs.showSaveDialog(props);
	}

	public void dialogClosed(NativeDialogEvent evt) {
		if(evt.getDialogResult() != NativeDialogEvent.OK_OPTION) return;
		
		String saveAs = evt.getDialogData().toString();
		try {
			QueryScriptLibrary.saveScriptToFile(queryScript, saveAs);
			
			final OpenFileHistory openFileHistory = new OpenFileHistory();
			openFileHistory.addToHistory(new File(saveAs));
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
		}
	}
	
}
