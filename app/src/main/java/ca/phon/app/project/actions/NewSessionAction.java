package ca.phon.app.project.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.project.NewSessionDialog;
import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;

public class NewSessionAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(NewSessionAction.class.getName());

	private static final long serialVersionUID = 3077154531739507863L;
	
	private String corpus;
	
	private String sessionName;
	
	private boolean sessionCreated = false;

	public NewSessionAction(ProjectWindow projectWindow) {
		this(projectWindow, null, null);
	}
	
	public NewSessionAction(ProjectWindow projectWindow, String corpus, String sessionName) {
		super(projectWindow);
		
		this.corpus = corpus;
		this.sessionName = sessionName;
		
		putValue(NAME, "New Session...");
		putValue(SHORT_DESCRIPTION, "Add new session to project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final Project proj = getWindow().getProject();
		String corpusName = 
				(this.corpus == null ? getWindow().getSelectedCorpus() : this.corpus);
		String sessionName = this.sessionName;
		
		if(sessionName == null) {
			NewSessionDialog dlg = null;
			if(getWindow().getSelectedCorpus() == null) {
				dlg = new NewSessionDialog(proj);
			} else {
				dlg = new NewSessionDialog(proj, corpusName);
			}
			dlg.setModal(true);
			dlg.pack();
			dlg.setVisible(true);
			
			if(!dlg.wasCanceled()) {
				corpusName = dlg.getCorpusName();
				sessionName = dlg.getSessionName();
			}
		}
		
		// create session
		try {
			proj.createSessionFromTemplate(corpusName, sessionName);
			sessionCreated = true;
			getWindow().refreshProject();
		} catch (IOException e) {
			Toolkit.getDefaultToolkit().beep();
			showMessage("New Session", e.getLocalizedMessage());
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	public boolean isSessionCreated() {
		return this.sessionCreated;
	}

}
