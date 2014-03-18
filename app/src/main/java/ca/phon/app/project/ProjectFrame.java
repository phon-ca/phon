package ca.phon.app.project;

import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;

/**
 * <p>Common module frame with project extension.</p>
 * 
 * <p>Manual retrieval of the project data can be done
 * as shown below:</br>
 * 
 * <pre>
 * final ProjectFrameExtension pfe = frame.getExtension(ProjectFrameExtension.class);
 * if(pfe != null) return pfe.getProject();
 * </pre>
 *</p>
 */
public class ProjectFrame extends CommonModuleFrame {
	
	private static final long serialVersionUID = 4160918049917672755L;

	public ProjectFrame() {
		this(null);
	}
	
	public ProjectFrame(Project project) {
		super();
		setProject(project);
	}

	/**
	 * Get the project associated with the frame
	 * 
	 * @return project
	 */
	public Project getProject() {
		return getExtension(Project.class);
	}
	
	/**
	 * Set the project associated with the frame
	 * 
	 * @param project
	 */
	public void setProject(Project project) {
		putExtension(Project.class, project);
	}
	
	
}
