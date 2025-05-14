/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.project;

import ca.phon.project.*;
import ca.phon.project.exceptions.ProjectConfigurationException;

import java.io.*;
import java.util.*;

/**
 * Shadow projects are used as temporary projects with the same
 * data and properties as a delegate project.  This is useful
 * when running queries where some temporary files are stored
 * inside the project folder.
 *
 */
public final class ShadowProject extends LocalProject {
	
	private final Project project;
	
	private Map<String, String> corpusPathMap = new HashMap<String, String>();
		
	/**
	 * Create a new shadow project.
	 * 
	 * @param project
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
		final Iterator<String> corpusItr = project.getCorpusIterator();
		while(corpusItr.hasNext()) {
			final String corpusName = corpusItr.next();
			retVal.setCorpusPath(corpusName, project.getCorpusPath(corpusName));
		}

		final ProjectResources projectResources = project.getExtension(ProjectResources.class);
		if(projectResources != null) {
			retVal.setResourceLocation(projectResources.getResourceLocation());
		}

		for(String mediaFolder:project.getProjectMediaFolders()) {
			retVal.addProjectMediaFolder(mediaFolder);
		}
		return retVal;
	}
	
	protected ShadowProject(File shadowFolder, Project project) throws ProjectConfigurationException {
		super(shadowFolder);
		
		this.project = project;
	}

	@Override
	public String getCorpusPath(String corpus) {
		if(corpusPathMap.containsKey(corpus)) {
			return corpusPathMap.get(corpus);
		} else {
			return super.getCorpusPath(corpus);
		}
	}

	@Override
	public void setCorpusPath(String corpus, String path) {
		corpusPathMap.put(corpus, path);
	}

	@Override
	public String getName() {
		return project.getName();
	}
	
}
