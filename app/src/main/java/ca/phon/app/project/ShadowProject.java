package ca.phon.app.project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import ca.phon.project.LocalProject;
import ca.phon.project.Project;
import ca.phon.project.ProjectEvent;
import ca.phon.project.ProjectListener;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.session.Participant;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.session.io.SessionReader;
import ca.phon.session.io.SessionWriter;

/**
 * Shadow projects are used as temporary projects with the same
 * data and properties as a delegate project.  This is useful
 * when running queries where some temporary files are stored
 * inside the project folder.
 *
 */
public final class ShadowProject extends LocalProject {
	
	private final Project project;
		
	/**
	 * Create a new shadow project.
	 * 
	 * @param preject
	 * @return shadow project
	 */
	public static ShadowProject of(Project project) throws ProjectConfigurationException {
		final UUID uuid = project.getUUID();
		final String tmpFolder = System.getProperty("java.io.tmpdir");
		final String tmpProjectFolder =
				tmpFolder + File.separator + "phon-" + Long.toHexString(uuid.getLeastSignificantBits());
		File shadowFolder = new File(tmpProjectFolder);
		if(!shadowFolder.exists()) {
			shadowFolder.mkdirs();
		}
		
		final ShadowProject retVal = new ShadowProject(shadowFolder, project);
		for(String corpusName:project.getCorpora()) {
			retVal.setCorpusPath(corpusName, project.getCorpusPath(corpusName));
		}
		retVal.setRecourceLocation(project.getResourceLocation());
		return retVal;
	}
	
	protected ShadowProject(File shadowFolder, Project project) throws ProjectConfigurationException {
		super(shadowFolder);
		
		this.project = project;
	}

	@Override
	public String getProjectMediaFolder() {
		File mediaFolder = new File(project.getProjectMediaFolder());
		if(!mediaFolder.isAbsolute()) {
			mediaFolder = new File(project.getLocation() + File.separator + mediaFolder);
		}
		return mediaFolder.getAbsolutePath();
	}

	@Override
	public String getCorpusMediaFolder(String corpus) {
		File mediaFolder = new File(project.getCorpusMediaFolder(corpus));
		if(!mediaFolder.isAbsolute()) {
			mediaFolder = new File(project.getLocation() + File.separator + mediaFolder);
		}
		return mediaFolder.getAbsolutePath();
	}

	/*
	 * Delegates
	 */
	public String getName() {
		return project.getName();
	}
	
	public List<String> getCorpora() {
		return project.getCorpora();
	}
	
}
