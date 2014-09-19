package ca.phon.app.menu.workspace;

import java.awt.event.ActionEvent;
import java.io.File;

import ca.phon.app.hooks.HookableAction;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogListener;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.workspace.Workspace;

public class SelectWorkspaceCommand extends HookableAction {

	private static final long serialVersionUID = 6739632731206685754L;

	private final static String TXT = "Select workspace folder...";
	private final static String DESC = "Select workspace folder...";
	
	public SelectWorkspaceCommand() {
		super();
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setCanChooseDirectories(true);
		props.setCanChooseFiles(false);
		props.setAllowMultipleSelection(false);
		props.setCanCreateDirectories(true);
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setListener(workspaceFolderListener);
		props.setTitle("Select workspace folder");
		NativeDialogs.showOpenDialog(props);
	}
	
	private final NativeDialogListener workspaceFolderListener = new NativeDialogListener() {
		
		@Override
		public void nativeDialogEvent(NativeDialogEvent event) {
			final String selectedPath = (String)event.getDialogData();
			if(selectedPath != null) {
				Workspace.setUserWorkspaceFolder(new File(selectedPath));
			}
		}
		
	};

}
