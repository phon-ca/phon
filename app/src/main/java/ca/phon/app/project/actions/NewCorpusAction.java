package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.project.NewCorpusDialog;
import ca.phon.app.project.ProjectWindow;

public class NewCorpusAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(NewCorpusAction.class.getName());

	private static final long serialVersionUID = -4385987381468266104L;
	
	public String corpusName;
	
	public String description;
	
	public boolean corpusCreated = false;
	
	public NewCorpusAction(ProjectWindow projectWindow) {
		this(projectWindow, null, null);
	}
	
	public NewCorpusAction(ProjectWindow projectWindow, String corpusName) {
		this(projectWindow, corpusName, "");
	}

	public NewCorpusAction(ProjectWindow projectWindow, String corpusName, String description) {
		super(projectWindow);
		this.corpusName = corpusName;
		this.description = description;
		
		putValue(NAME, "New Corpus...");
		putValue(SHORT_DESCRIPTION, "Add corpus to project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		String corpusName = this.corpusName;
		String desc = this.description;
		
		if(corpusName == null) {
			// show new corpus dialog
			final NewCorpusDialog dlg = new NewCorpusDialog(getWindow());
			dlg.setVisible(true);
			
			if(dlg.wasCanceled()) return;
			corpusName = dlg.getCorpusName();
			desc = dlg.getCorpusDescription();
		}
		
		try {
			getWindow().getProject().addCorpus(corpusName, desc);
			this.corpusCreated = true;
			getWindow().refreshProject();
		} catch (IOException e) {
			showMessage("New Corpus", e.getLocalizedMessage());
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}
	
	public boolean isCorpusCreated() {
		return this.corpusCreated;
	}

}
