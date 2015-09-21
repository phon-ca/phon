package ca.phon.app.project.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;

/**
 * Delete corpus/corpora which are currently selected
 * in the project window.
 * 
 */
public class DeleteCorpusAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(DeleteCorpusAction.class.getName());
	
	private static final long serialVersionUID = -6953043638785028830L;

	public DeleteCorpusAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(Action.NAME, "Delete corpus");
		putValue(Action.SHORT_DESCRIPTION, "Delete selected corpora");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final List<String> corpora = getWindow().getSelectedCorpora();
		if(corpora.size() == 0) return;
		
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setRunAsync(false);
		if(corpora.size() > 1) {
			props.setHeader("Delete selected corpora?");
		} else {
			props.setHeader("Delete corpus: " + corpora.get(0));
		}
		props.setMessage("All sessions in" +
				(corpora.size() > 1 ? " these corpora" : " this corpus") + " will also be deleted! This action cannot be undone.");
		props.setOptions(MessageDialogProperties.okCancelOptions);
		int retVal = NativeDialogs.showMessageDialog(props);
		
		final Project project = getWindow().getProject();
		if(retVal == 0) {
			for(String corpus:corpora) {
				try {
					project.removeCorpus(corpus);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					Toolkit.getDefaultToolkit().beep();
					showMessage("Delete Corpus", e.getLocalizedMessage());
				}
			}
		}
	}

}
