package ca.phon.app.query.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.ImageIcon;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.log.LogUtil;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

/**
 * Export query to a file on disk.
 */
public class ExportQueryAction extends HookableAction {
	
	private QueryScript queryScript;
	
	private String initialFolder;
	
	public ExportQueryAction(QueryScript queryScript, String initialFolder) {
		super();
		
		this.queryScript = queryScript;
		this.initialFolder = initialFolder;
	
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
		
		final QueryName qn = queryScript.getExtension(QueryName.class);
		if(qn != null) {
			props.setInitialFile(qn.getName() + ".xml");
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
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep();
			LogUtil.severe(e);
		}
	}
	
}
