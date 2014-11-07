package ca.phon.query.analysis;

import java.util.List;

import ca.phon.project.Project;
import ca.phon.session.SessionPath;

public class QueryAnalysisInput {
	
	private Project project;

	private List<SessionPath> sessions;

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public List<SessionPath> getSessions() {
		return sessions;
	}

	public void setSessions(List<SessionPath> sessions) {
		this.sessions = sessions;
	}

}
