/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import java.io.*;
import java.util.*;

import ca.phon.app.log.*;
import ca.phon.project.*;
import ca.phon.project.exceptions.*;

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
		
		Properties props = project.getExtension(Properties.class);
		try(FileOutputStream fout = new FileOutputStream(new File(shadowFolder, ".properties"))) {
			props.store(fout, "");
		} catch (IOException e) {
			LogUtil.severe(e);
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

	/*
	 * Delegates
	 */
	public String getName() {
		return project.getName();
	}
	
	public List<String> getCorpora() {
		if(project != null)
			return project.getCorpora();
		else 
			return new ArrayList<String>();
	}
	
}
