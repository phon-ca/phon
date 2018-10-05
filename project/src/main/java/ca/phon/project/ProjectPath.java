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
package ca.phon.project;

import java.io.File;

import ca.phon.session.Session;

/**
 * Path used for project drag & drop support.  The path may 
 * point to either a corpus folder or session file.
 */
public class ProjectPath {
	
	private Project project;
	
	private String corpus;
	
	private String session;
	
	public ProjectPath(Project project) {
		this(project, null, null);
	}
	
	public ProjectPath(Project project, Session session) {
		this(project, session.getCorpus(), session.getName());
	}
	
	public ProjectPath(Project project, String corpus, String sessionName) {
		this.project = project;
		this.corpus = corpus;
		this.session = sessionName;
	}
	
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public String getCorpus() {
		return corpus;
	}

	public void setCorpus(String corpus) {
		this.corpus = corpus;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}
	
	public boolean isProjectPath() {
		return (project != null && corpus == null && session == null);
	}
	
	public boolean isCorpusPath() {
		return (project != null && corpus != null && session == null);
	}
	
	public boolean isSessionPath() {
		return (project != null && corpus != null && session != null);
	}
	
	/**
	 * Return absolute file path
	 * 
	 * @return
	 */
	public String getAbsolutePath() {
		if(project != null) {
			if(corpus != null) {
				if(session != null) {
					return project.getSessionPath(corpus, session);
				} else {
					return project.getCorpusPath(corpus);
				}
			} else {
				return project.getLocation();
			}
		} else {
			return "";
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(project != null) sb.append(project.getName()).append(File.separator);
		if(corpus != null) sb.append(corpus).append(File.separator);
		if(session != null) sb.append(session);
		return sb.toString();
	}
	
}
