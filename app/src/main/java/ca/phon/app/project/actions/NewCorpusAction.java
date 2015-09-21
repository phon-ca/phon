package ca.phon.app.project.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ca.phon.app.project.NewCorpusDialog;
import ca.phon.app.project.ProjectWindow;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

public class NewCorpusAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(NewCorpusAction.class.getName());

	private static final long serialVersionUID = -4385987381468266104L;

	public NewCorpusAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "New Corpus...");
		putValue(SHORT_DESCRIPTION, "Add corpus to project");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final NewCorpusDialog dlg = new NewCorpusDialog(getWindow());
		dlg.setVisible(true);
		
		// wait for dlg to close
		
		if(!dlg.wasCanceled()) {
			final String corpusName = dlg.getCorpusName();
			final String corpusDescription = dlg.getCorpusDescription();
			
			try {
				getWindow().getProject().addCorpus(corpusName, corpusDescription);
				getWindow().refreshProject();
			} catch (IOException e) {
				showMessage("New Corpus", e.getLocalizedMessage());
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}

	private void showMessage(String msg1, String msg2) {
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setOptions(MessageDialogProperties.okOptions);
		props.setHeader(msg1);
		props.setMessage(msg2);
		props.setParentWindow(getWindow());
		
		NativeDialogs.showDialog(props);
	}
	
}
