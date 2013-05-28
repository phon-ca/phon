package ca.phon.app.project;

import java.lang.ref.WeakReference;

import ca.phon.extensions.Extension;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

/**
 * Extension for {@link CommonModuleFrame} that adds
 * a project reference to the window.
 */
@Extension(CommonModuleFrame.class)
public class ProjectFrameExtension {

	/**
	 * Weak reference to project
	 */
	private final WeakReference<Project> projectRef;
	
	public ProjectFrameExtension(Project project) {
		projectRef = new WeakReference<Project>(project);
	}
	
	public Project getProject() {
		return projectRef.get();
	}
	
}
