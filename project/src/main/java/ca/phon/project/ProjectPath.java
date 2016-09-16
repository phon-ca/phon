/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
