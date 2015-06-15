package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import ca.phon.app.project.NewSessionEP;
import ca.phon.app.project.ProjectWindow;
import ca.phon.plugin.PluginEntryPointRunner;

public class NewSessionAction extends ProjectWindowAction {

	private static final long serialVersionUID = 3077154531739507863L;

	public NewSessionAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "New Session...");
		putValue(SHORT_DESCRIPTION, "Add new session to project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		HashMap<String, Object> initInfo = 
				new HashMap<String, Object>();
		initInfo.put("project", getWindow().getProject());
		initInfo.put(NewSessionEP.PROJECT_WINDOW_PROP, getWindow());
		
		PluginEntryPointRunner.executePluginInBackground("NewSession", initInfo);
	}

}
