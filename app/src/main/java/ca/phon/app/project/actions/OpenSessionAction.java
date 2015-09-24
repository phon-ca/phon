package ca.phon.app.project.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.project.ProjectWindow;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.session.SessionPath;

public class OpenSessionAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(OpenSessionAction.class.getName());

	private static final long serialVersionUID = 9111187064505853422L;

	private String corpus = null;
	private String sessionName = null;
	
	public OpenSessionAction(ProjectWindow projectWindow) {
		this(projectWindow, null, null);
	}
	
	public OpenSessionAction(ProjectWindow projectWindow, String corpus, String sessionName) {
		super(projectWindow);
		
		this.corpus = corpus;
		this.sessionName = sessionName;
		
		putValue(NAME, "Open Session...");
		putValue(SHORT_DESCRIPTION, "Open session in a new editor window");
	}
	

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final String corpus = 
				(this.corpus == null ? getWindow().getSelectedCorpus() : this.corpus);
		final String sessionName =
				(this.sessionName == null ? getWindow().getSelectedSessionName() : this.sessionName);
		
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("project", getWindow().getProject());
		initInfo.put("sessionName", new SessionPath(corpus, sessionName).toString());
		initInfo.put("blindmode", getWindow().isBlindMode());
		
		try {
			PluginEntryPointRunner.executePlugin("SessionEditor", initInfo);
		} catch (PluginException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			showMessage("Open Session", e.getLocalizedMessage());
			Toolkit.getDefaultToolkit().beep();
		}
	}

}
