/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.opgraph.nodes.project;

import java.awt.BorderLayout;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import ca.phon.app.session.SessionSelector;
import ca.phon.opgraph.InputField;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.OpNodeInfo;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;

@OpNodeInfo(
	category="Project",
	name="Select Sessions",
	description="Select sessions for project.  A session selector dialog will be presented to the user if" +
	" a pre-defined set of sessions is not given.",
	showInLibrary=true
)
public class SessionSelectorNode extends OpNode implements NodeSettings {
	
	private final InputField projectField = 
			new InputField("project", "Project for session selection", true, true);
	
	private OutputField projectOutputField = new OutputField("project", "Project", true, Project.class);
	
	private final OutputField sessionOutputField = 
			new OutputField("selected sessions", "Selected sessions", true, List.class);
	
	public SessionSelectorNode() {
		super();
		
		putField(projectField);
		putField(projectOutputField);
		putField(sessionOutputField);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(final OpContext context) throws ProcessingException {
		// get project
		final Project project = 
				(Project)(context.get(projectField) != null ? context.get(projectField)
						: context.get("_project"));
		if(project == null) throw new ProcessingException(null, "No project available");
		
		final Session session = (Session)context.get("_session");
		context.put(projectOutputField, project);
		if(session != null) {
			context.put(sessionOutputField, Collections.singletonList(session));
		} else {
			Runnable onEDT = () -> {
				final SessionSelector selector = new SessionSelector(project);
				final JScrollPane scroller = new JScrollPane(selector);
				
				
				final DialogHeader header = new DialogHeader("Select Sessions", "");
				
				final JDialog selectorDialog = new JDialog();
				selectorDialog.setModal(true);
				selectorDialog.setTitle("Select Sessions");
				selectorDialog.setLayout(new BorderLayout());
				selectorDialog.add(header, BorderLayout.NORTH);
				selectorDialog.add(scroller, BorderLayout.CENTER);
				
				final JButton okButton = new JButton("Ok");
				okButton.addActionListener((e) -> { 
					selectorDialog.setVisible(false); 
					context.put(projectOutputField, project);
					context.put(sessionOutputField, selector.getSelectedSessions());
				});
				selectorDialog.add(ButtonBarBuilder.buildOkBar(okButton), BorderLayout.SOUTH);
				selectorDialog.getRootPane().setDefaultButton(okButton);
				
				selectorDialog.setSize(500, 600);
				selectorDialog.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
				selectorDialog.setVisible(true);
			};
			if(SwingUtilities.isEventDispatchThread())
				onEDT.run();
			else
				try {
					SwingUtilities.invokeAndWait(onEDT);
				} catch (InvocationTargetException | InterruptedException e) {
					throw new ProcessingException(null, e);
				}
		}
	}
	
	@Override
	public Component getComponent(GraphDocument document) {
		return new JPanel();
	}

	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties properties) {
	}

}
