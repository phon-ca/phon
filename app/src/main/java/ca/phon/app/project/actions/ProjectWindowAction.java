package ca.phon.app.project.actions;

import java.lang.ref.WeakReference;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.project.ProjectWindow;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

public abstract class ProjectWindowAction extends HookableAction {

	private static final long serialVersionUID = 7949135760405306345L;

	private final WeakReference<ProjectWindow> projectWindowRef;
	
	public ProjectWindowAction(ProjectWindow projectWindow) {
		super();
		this.projectWindowRef = new WeakReference<ProjectWindow>(projectWindow);
	}
	
	public ProjectWindow getWindow() {
		return this.projectWindowRef.get();
	}

	protected void showMessage(String msg1, String msg2) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setOptions(MessageDialogProperties.okOptions);
		props.setHeader(msg1);
		props.setMessage(msg2);
		props.setParentWindow(getWindow());
		
		NativeDialogs.showDialog(props);
	}
	
}
