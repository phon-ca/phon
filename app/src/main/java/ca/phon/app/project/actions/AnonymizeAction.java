package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import ca.phon.app.project.AnonymizeParticipantInfoEP;
import ca.phon.app.project.ProjectWindow;
import ca.phon.plugin.PluginEntryPointRunner;

public class AnonymizeAction extends ProjectWindowAction {

	private static final long serialVersionUID = 1438208708979568761L;

	public AnonymizeAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Anonymize Participant Information...");
		putValue(SHORT_DESCRIPTION, "Anonymize participant information for project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getWindow().getProject());
		
		PluginEntryPointRunner.executePluginInBackground(AnonymizeParticipantInfoEP.EP_NAME, initInfo);
	}

}
