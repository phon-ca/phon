package ca.phon.app.project;

import java.util.List;

import javax.swing.JPanel;

import ca.phon.project.Project;
import ca.phon.session.SessionPath;

public class ContextPanel extends JPanel {

	private Project project;
	
	private List<SessionPath> selectedPaths;
	
	public ContextPanel(Project project) {
		super();
		
		this.project = project;
		
		init();
	}
	
	private void init() {
		
	}
	
	private void update() {
		removeAll();
		
		addProjectInformation();
	}
	
	private void addProjectInformation() {
		
	}
	
}
