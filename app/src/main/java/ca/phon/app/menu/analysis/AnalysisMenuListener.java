/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.menu.analysis;

import ca.phon.app.opgraph.analysis.AnalysisLibrary;
import ca.phon.project.Project;
import ca.phon.session.*;
import ca.phon.ui.CommonModuleFrame;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

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
