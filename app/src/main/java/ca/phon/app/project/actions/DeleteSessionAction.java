package ca.phon.app.project.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.util.OSInfo;

public class DeleteSessionAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(DeleteSessionAction.class.getName());

	private static final long serialVersionUID = -1541549069772255530L;

	public DeleteSessionAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(Action.NAME, "Delete session");
		putValue(Action.SHORT_DESCRIPTION, "Delete selected session");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		final List<String> sessionNames = getWindow().getSelectedSessionNames();
		if(sessionNames.size() == 0) return;
		
		final MessageDialogProperties props = new MessageDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setRunAsync(false);
		if(sessionNames.size() > 1) {
			props.setHeader("Delete selected sessions?");
			props.setMessage("Move selected sessions to " + 
					(OSInfo.isWindows() ? " Recycle Bin" : " Trash") + "?");
		} else {
			props.setHeader("Delete session: " + sessionNames.get(0));
			props.setMessage("Move session to " + 
					(OSInfo.isWindows() ? " Recycle Bin" : " Trash") + "?");
		}
		
		props.setOptions(MessageDialogProperties.okCancelOptions);
		int retVal = NativeDialogs.showMessageDialog(props);
		
		final Project project = getWindow().getProject();
		final String corpus = getWindow().getSelectedCorpus();
		if(retVal == 0) {
			for(String sessionName:sessionNames) {
				try {
					UUID writeLock = project.getSessionWriteLock(corpus, sessionName);
					project.removeSession(corpus, sessionName, writeLock);
					project.releaseSessionWriteLock(corpus, sessionName, writeLock);
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					Toolkit.getDefaultToolkit().beep();
					showMessage("Delete Session", e.getLocalizedMessage());
				}
			}
			getWindow().getSessionList().clearSelection();
		}
	}

}
