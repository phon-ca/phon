package ca.phon.app.project;

import java.awt.BorderLayout;

import javax.swing.JButton;

import ca.phon.project.Project;
import ca.phon.session.Participant;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;

/**
 * Window for viewing and modifying {@link Participant}s for a {@link Project}.
 */
public class ProjectParticpantsWindow extends CommonModuleFrame {
	
	private DialogHeader header;
	
	private ParticipantsPanel participantsPanel;
	
	public ProjectParticpantsWindow(Project project) {
		super("Project Participants");
		
		putExtension(Project.class, project);
		init();
	}
	
	public Project getProject() {
		return getExtension(Project.class);
	}

	private void init() {
		setLayout(new BorderLayout());
		
		header = new DialogHeader("Project Participants", "View and modify participants for the project");
		add(header, BorderLayout.NORTH);
		
		participantsPanel = new ParticipantsPanel(getProject());
		add(participantsPanel, BorderLayout.CENTER);
	}
	
}
