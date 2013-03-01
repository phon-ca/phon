package ca.phon.project;

import java.io.IOException;
import java.net.URL;

/**
 * Create projects from {@link URL}s
 */
public abstract class ProjectFactory {
	
	/**
	 * Create a new project for the given {@link URL}.
	 * 
	 * If the project does not exist, a new project
	 * is created at the given location.
	 * 
	 * @return project
	 * 
	 * @throws IOException
	 */
	public abstract Project createProject(URL url)
		throws IOException;
	
	
	
}
