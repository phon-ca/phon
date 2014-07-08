package ca.phon.project;

import ca.phon.extensions.Extension;

/**
 * Project extension which provides refresh support for a project.
 *
 */
@Extension(Project.class)
public interface ProjectRefresh {

	/**
	 * Refresh the corpus/session information 
	 * for a project.
	 */
	public void refresh();
	
}
