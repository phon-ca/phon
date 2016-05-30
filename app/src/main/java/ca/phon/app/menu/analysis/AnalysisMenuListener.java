package ca.phon.app.menu.analysis;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import ca.phon.app.opgraph.analysis.AnalysisLibrary;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.util.PrefHelper;

public class AnalysisMenuListener implements MenuListener {

	@Override
	public void menuSelected(MenuEvent e) {
		final JMenu menu = (JMenu)e.getSource();
		menu.removeAll();
		
		final CommonModuleFrame currentFrame = CommonModuleFrame.getCurrentFrame();
		if(currentFrame == null) return;
		final Project project = CommonModuleFrame.getCurrentFrame().getExtension(Project.class);
		if(project == null) return;
		
		List<SessionPath> selectedSessions = new ArrayList<>();
		final Session session = CommonModuleFrame.getCurrentFrame().getExtension(Session.class);
		if(session != null) {
			selectedSessions.add(new SessionPath(session.getCorpus(), session.getName()));
		}
		
		final AnalysisLibrary library = new AnalysisLibrary();
		library.setupMenu(project, selectedSessions, menu);
		
		if(PrefHelper.getBoolean("phon.debug", false)) {
			menu.addSeparator();
			
			
		}
	}

	@Override
	public void menuDeselected(MenuEvent e) {
	}

	@Override
	public void menuCanceled(MenuEvent e) {
	}

}
