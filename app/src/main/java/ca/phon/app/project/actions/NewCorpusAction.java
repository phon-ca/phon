package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;

import ca.phon.app.project.ProjectWindow;
import ca.phon.plugin.PluginEntryPointRunner;

public class NewCorpusAction extends ProjectWindowAction {

	private static final long serialVersionUID = -4385987381468266104L;

	public NewCorpusAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "New Corpus...");
		putValue(SHORT_DESCRIPTION, "Add corpus to project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		HashMap<String, Object> initInfo = 
				new HashMap<String, Object>();
		initInfo.put("project", getWindow().getProject());
		PluginEntryPointRunner.executePluginInBackground("NewCorpus", initInfo);
	}

}
