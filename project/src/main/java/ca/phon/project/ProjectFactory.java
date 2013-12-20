package ca.phon.project;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.project.io.ObjectFactory;
import ca.phon.project.io.ProjectType;

/**
 * Create projects from {@link URL}s
 */
public class ProjectFactory {
	
	public ProjectFactory() {
		super();
	}
	
	/**
	 * Open a project from the specified
	 * folder.
	 * 
	 * @param projectFolder
	 * 
	 * @throws IOException if the given file object is
	 *  not a folder or does not exist
	 * @throws ProjectConfigurationException if the given
	 *  folder is not a phon project
	 */
	public Project openProject(File projectFolder) 
		throws IOException, ProjectConfigurationException {
		// check folder
		if(projectFolder == null) {
			throw new NullPointerException();
		}
		
		if(!projectFolder.exists()) {
			throw new FileNotFoundException(projectFolder.getAbsolutePath());
		}
		
		if(!projectFolder.isDirectory()) {
			throw new IOException("Given file object must be a folder.");
		}
		
		return new LocalProject(projectFolder);
	}
	
	/**
	 * Create  project at the specified folder.  The name of
	 * the project will automatically be set to the value of
	 * the final path element.
	 * 
	 * @param projectFolder
	 * 
	 * @throws IOException if a problem occurs while trying to 
	 *  access/write to the given location
	 */
	public Project createProject(File projectFolder) 
			throws IOException {
		final String projectName = projectFolder.getName();
		if(projectName.length() == 0) 
			throw new IOException("Project name cannot be null");
		
		// create project folder, use exsiting folder if it exists
		if(projectFolder.exists()) {
			if(!projectFolder.isDirectory()) {
				throw new IOException("Cannot create project at " + 
						projectFolder.getAbsolutePath() + ".  File already exists.");
			}
		} else {
			if(!projectFolder.mkdirs()) {
				throw new IOException("Could not create project folder at " + 
						projectFolder.getAbsolutePath() + ".");
			}
		}
		
		// create the new project.xml file
		final ObjectFactory factory = new ObjectFactory();
		final ProjectType projectType = factory.createProjectType();
		projectType.setName(projectName);
		projectType.setUuid(UUID.randomUUID().toString());
		
		final File projectFile = new File(projectFolder, "project.xml");
		
		try {
			final JAXBContext context = JAXBContext.newInstance(ObjectFactory.class);
			final Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(factory.createProject(projectType), projectFile);
		} catch (JAXBException ex) {
			throw new IOException(ex);
		}
		
		try {
			return openProject(projectFolder);
		} catch (ProjectConfigurationException pe) {
			throw new IOException(pe);
		}
	}
}
