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
package ca.phon.app.session.check;

import java.awt.*;
import java.io.*;
import java.util.List;

import javax.swing.*;

import ca.phon.app.log.*;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.app.session.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.project.*;
import ca.phon.session.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.wizard.*;

/**
 * Check sessions for errors.
 */
public class SessionCheckWizard extends NodeWizard {

	private static final long serialVersionUID = 6650736926995551274L;
	
	private final static String SESSION_CHECK_GRAPH = "session_check.xml";

	private SessionSelector sessionSelector;
	private SessionSelectorActiveEditorSupport editorSupport;
		
	public static SessionCheckWizard newWizard(Project project) {
		final InputStream in = SessionCheckWizard.class.getResourceAsStream(SESSION_CHECK_GRAPH);
		if(in == null) throw new IllegalStateException(SESSION_CHECK_GRAPH + " not found");
		
		try {
			OpGraph graph = OpgraphIO.read(in);
			
			return new SessionCheckWizard(project, new Processor(graph), graph);
		} catch (IOException e) {
			LogUtil.severe(e);
			throw new IllegalStateException(e);
		}
	}
	
	private SessionCheckWizard(Project project, Processor processor, OpGraph graph) {
		super("Session Check", processor, graph);
		
		putExtension(Project.class, project);
		
		globalOptionsPanel.setVisible(false);
		
		init();
	}
	
	public Project getProject() {
		return getExtension(Project.class);
	}
	
	private void init() {
		WizardStep step1 = getWizardStep(0);
		
		TitledPanel tp = new TitledPanel("Select Sessions");
		sessionSelector = new SessionSelector(getProject());
		editorSupport = new SessionSelectorActiveEditorSupport();
		editorSupport.install(sessionSelector);
		tp.setPreferredSize(new Dimension(350, 0));
		tp.setLayout(new BorderLayout());
		tp.add(new JScrollPane(sessionSelector), BorderLayout.CENTER);
		step1.add(tp, BorderLayout.WEST);
	}
	
	@Override
	public void next() {
		if(getCurrentStepIndex() == 0) {
			List<SessionPath> selectedSessions = sessionSelector.getSelectedSessions();
			if(selectedSessions.size() == 0) {
				showMessageDialog("Select Sessions", "Please select at least one session", MessageDialogProperties.okOptions);
				return;
			}
			
			getProcessor().getContext().put("_project", getProject());
			getProcessor().getContext().put("_selectedSessions", selectedSessions);
		}
		super.next();
	}
	
}
