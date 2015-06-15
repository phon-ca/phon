package ca.phon.app.project.actions;

import java.lang.ref.WeakReference;

import ca.phon.app.hooks.HookableAction;
import ca.phon.app.project.ProjectWindow;

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
	
}
