package ca.phon.app.project.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;

import ca.phon.app.project.ProjectWindow;
import ca.phon.project.Project;

/**
 * Duplicate sessions selected in project window.  Session names
 * are suffixed with an index.
 * 
 * E.g., <code>MySession</code> becomes <code>MySession (1)</code>
 *
 */
public class DuplicateSessionAction extends ProjectWindowAction {
	
	private final static Logger LOGGER = Logger.getLogger(DuplicateSessionAction.class.getName());

	private static final long serialVersionUID = 140755322409598286L;

	public DuplicateSessionAction(ProjectWindow projectWindow) {
		super(projectWindow);
		
		putValue(NAME, "Duplicate Session");
		putValue(SHORT_DESCRIPTION, "Duplicate selected session(s)");
	}

	@Override
	public void hookableActionPerformed(ActionEvent ae) {
		// duplicate all selected sessions
		final String corpus = getWindow().getSelectedCorpus();
		if(corpus == null) return;
		
		final List<String> sessionNames = getWindow().getSelectedSessionNames();
		final List<String> dupSessionNames = new ArrayList<>();
		final Project project = getWindow().getProject();
		for(String sessionName:sessionNames) {
			int idx = 0;
			String dupSessionName = sessionName + " (" + (++idx) + ")";
			while(project.getCorpusSessions(corpus).contains(dupSessionName)) {
				dupSessionName = sessionName + " (" + (++idx) + ")";
			}
			final File oldSessionFile = new File(project.getSessionPath(corpus, sessionName));
			final File dupSessionFile = new File(project.getSessionPath(corpus, dupSessionName));
			try {
				FileUtils.copyFile(oldSessionFile, dupSessionFile);
				dupSessionNames.add(dupSessionName);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				Toolkit.getDefaultToolkit().beep();
				showMessage("Duplicate Session", e.getLocalizedMessage());
			}
		}
		if(sessionNames.size() > 0) {
			int indices[] = new int[dupSessionNames.size()];
			getWindow().refreshProject();
			for(int i = 0; i < dupSessionNames.size(); i++) {
				String sessionName = dupSessionNames.get(i);
				indices[i] = project.getCorpusSessions(corpus).indexOf(sessionName);
			}
			getWindow().getSessionList().setSelectedIndices(indices);
		}
	}

}
