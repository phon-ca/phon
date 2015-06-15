package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import ca.phon.app.project.ProjectWindow;
import ca.phon.plugin.PluginEntryPointRunner;

public class DeriveSessionAction extends ProjectWindowAction {

	private static final long serialVersionUID = 4025880051460438742L;

	public DeriveSessionAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Derive Session...");
		putValue(SHORT_DESCRIPTION, "Create a new session from existing data");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getWindow().getProject());
		
		PluginEntryPointRunner.executePluginInBackground("DeriveSession", initInfo);
	}

}
