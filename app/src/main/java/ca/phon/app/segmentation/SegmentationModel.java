package ca.phon.app.segmentation;

import ca.phon.project.Project;
import ca.phon.session.Session;

public class SegmentationModel {
	
	private Project project;
	
	private Session session;

	public SegmentationModel(Project project, Session session) {
		this.project = project;
		this.session = session;
	}
	
	public Session getSession() {
		return this.session;
	}
	
	public void setSession(Session session) {
		this.session = session;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}
	
}
