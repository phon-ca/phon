/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
		library.setupMenu(project, selectedSessions, menu.getPopupMenu());
	}

	@Override
	public void menuDeselected(MenuEvent e) {
	}

	@Override
	public void menuCanceled(MenuEvent e) {
	}

}
