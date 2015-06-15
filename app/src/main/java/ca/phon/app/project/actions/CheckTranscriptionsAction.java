package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import ca.phon.app.project.ProjectWindow;
import ca.phon.plugin.PluginEntryPointRunner;

public class CheckTranscriptionsAction extends ProjectWindowAction {
	
	private static final long serialVersionUID = -5819488757671662388L;

	public CheckTranscriptionsAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Check Transcriptions...");
		putValue(SHORT_DESCRIPTION, "Check IPA Transcriptions for project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getWindow().getProject());
		
		PluginEntryPointRunner.executePluginInBackground("CheckIPA", initInfo);
	}

}
