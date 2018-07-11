/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.app.modules;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import ca.phon.app.project.DesktopProjectFactory;
import ca.phon.app.workspace.Workspace;
import ca.phon.project.Project;
import ca.phon.project.ProjectFactory;
import ca.phon.project.exceptions.ProjectConfigurationException;
import ca.phon.session.Session;

/**
 * Argument information for plug-in entry points.
 * 
 * This class includes helper methods for parsing
 * command line arguments and accessing commonly needed
 * argument values such as project, corpus, and session.
 * 
 */
public class EntryPointArgs extends HashMap<String, Object> {
	
	private final static Logger LOGGER = Logger.getLogger(EntryPointArgs.class.getName());

	private static final long serialVersionUID = -3794413262334673920L;

	/*
	 * Keys
	 */
	/**
	 * project name: if defined, Phon will attempt to locate the named project
	 *  in the current workspace folder.
	 */
	public final static String PROJECT_NAME = "projectName";
	public final static String PROJECT_NAME_OPT = "p";
	public final static String PROJECT_NAME_DESC = "Project name in workspace";
	
	/**
	 * project location: if defined, Phon will attempt to locate the project
	 *  at the given path.  This key takes priority over project name.
	 */
	public final static String PROJECT_LOCATION = "projectLocation";
	public final static String PROJECT_LOCATION_OPT = "pl";
	public final static String PROJECT_LOCATION_DESC = "Project location";
	
	/**
	 * project: if defined, Phon will use the given Object value as the 
	 *  project.  This value overrides all others for project definition.
	 */
	public final static String PROJECT_OBJECT = "project";
	
	/**
	 * <p>session name: if defined, Phon will attempt to locate the named session
	 *  in the provided project.  One of the project keys must be defined.<p>
	 *  
	 * <p>Session name can be entered as just the session name, in which a 
	 * corpus name must also be given, or as 'corpus.session'</p>
	 */
	public final static String SESSION_NAME = "sessionName";
	public final static String SESSION_NAME_OPT = "s";
	public final static String SESSION_NAME_DESC = "Session name";
	
	/**
	 * session: if defined, Phon will use the given Object as the returned Session
	 */
	public final static String SESSION_OBJECT = "session";
	
	/**
	 * corpus name: if defined, Phon will use the given value as the corpus name.
	 *  If session has a corpus name defined in it's string, this value is ignored.
	 */
	public final static String CORPUS_NAME = "corpusName";
	public final static String CORPUS_NAME_OPT = "c";
	public final static String CORPUS_NAME_DESC = "Corpus name";

	/* 
	 * Constructors from superclass
	 */
	public EntryPointArgs() {
		super();
	}

	public EntryPointArgs(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public EntryPointArgs(int initialCapacity) {
		super(initialCapacity);
	}

	public EntryPointArgs(Map<? extends String, ? extends Object> m) {
		super(m);
	}
	
	/**
	 * <p>Get the project based on either (in order):
	 * <ul><li>project object</li><li>project location</li><li>project name</li></ul>
	 * </p>
	 * 
	 * @return project or <code>null</code> if not specified
	 */
	public Project getProject() {
		Project retVal = null;
		
		final Object projectObj = get(PROJECT_OBJECT);
		final Object projectLoc = get(PROJECT_LOCATION);
		final Object projectName = get(PROJECT_NAME);
		
		File projectFile = null;
		
		if(projectObj != null && projectObj instanceof Project) {
			retVal = Project.class.cast(projectObj);
		} else if(projectLoc != null) {
			final String projectLocation = projectLoc.toString();
			projectFile = new File(projectLocation);
		} else if(projectName != null) {
			final Workspace workspace = Workspace.userWorkspace();
			projectFile = new File(workspace.getWorkspaceFolder(), projectName.toString());
		}
		
		if(projectFile != null) {
			final ProjectFactory factory = new DesktopProjectFactory();
			try {
				retVal = factory.openProject(projectFile);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			} catch (ProjectConfigurationException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		return retVal;
	}
	
	/**
	 * Get corpus name
	 * 
	 * @return specified corpus name or <code>null</code>
	 */
	public String getCorpus() {
		final Object corpusObj = get(CORPUS_NAME);
		return (corpusObj != null ? corpusObj.toString() : null);
	}
	
	
	/**
	 * Get session
	 * 
	 * @return session or <code>null</code> if not specified
	 */
	public Session getSession() throws IOException {
		Session retVal = null;
		
		final Object sessionObj = get(SESSION_OBJECT);
		final Object sessionName = get(SESSION_NAME);
		
		if(sessionObj != null && sessionObj instanceof Session) {
			retVal = Session.class.cast(sessionObj);
		} else if(sessionName != null) {
			final Project project = getProject();
			String corpus = getCorpus();
			String session = sessionName.toString();
			
			if(project != null) {
				if(corpus == null) {
					int firstDot = session.indexOf('.');
					if(firstDot > 0) {
						corpus = session.substring(0, firstDot);
						session = session.substring(firstDot+1);
					}
				}
				if(corpus != null && session != null) {
					retVal = project.openSession(corpus, session);
				} else {
					throw new IOException("Session location not specified.  Corpus: " + corpus
							+ ", Session: " + session);
				}
			} else {
				throw new IOException("Project not specified");
			}
		}
		
		return retVal;
	}

	/**
	 * Parse command line arguments.
	 * 
	 * @param args
	 */
	public void parseArgs(String[] args) {
		final Options options = new Options();
		options.addOption(PROJECT_NAME_OPT, PROJECT_NAME, true, PROJECT_NAME_DESC);
		options.addOption(PROJECT_LOCATION_OPT, PROJECT_LOCATION, true, PROJECT_LOCATION_DESC);
		options.addOption(CORPUS_NAME_OPT, CORPUS_NAME, true, CORPUS_NAME_DESC);
		options.addOption(SESSION_NAME_OPT, SESSION_NAME, true, SESSION_NAME_DESC);
		
		final CommandLineParser parser = new EntryPointArgParser();
		
		try {
			final CommandLine cmdLine = parser.parse(options, args, false);
			for(Option opt:cmdLine.getOptions()) {
				put(opt.getLongOpt(), opt.getValue());
			}
		} catch (ParseException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
}
