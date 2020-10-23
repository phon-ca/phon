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
package ca.phon.app.opgraph.analysis;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.undo.*;

import ca.phon.app.log.*;
import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.library.*;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.app.project.*;
import ca.phon.app.workspace.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.edits.graph.*;
import ca.phon.opgraph.dag.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.extensions.*;
import ca.phon.project.*;
import ca.phon.util.*;

public class AnalysisOpGraphEditorModel extends OpgraphEditorModel {

	private JPanel debugSettings;

	private JComboBox<Project> projectList;

	private ParticipantsPanel participantSelector;

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(AnalysisOpGraphEditorModel.class.getName());

	public AnalysisOpGraphEditorModel() {
		this(new OpGraph());
	}

	public AnalysisOpGraphEditorModel(OpGraph opgraph) {
		super(opgraph);

		WizardExtension ext = opgraph.getExtension(WizardExtension.class);
		if(ext != null && !(ext instanceof AnalysisWizardExtension)) {
			throw new IllegalArgumentException("Graph is not an analysis document.");
		}
		if(ext == null) {
			ext = new AnalysisWizardExtension(opgraph);
			opgraph.putExtension(WizardExtension.class, ext);
		}

		init();
	}

	private void addAnalysisNode(OpNode analysisNode) {
		final WizardExtension graphExtension = getWizardExtension();
		final OpGraph parentGraph = getDocument().getGraph();
		final WizardExtension analysisExt = ((CompositeNode)analysisNode).getGraph().getExtension(WizardExtension.class);

		// attempt to setup links for project, selected session and selected participants
		final OpNode parentProjectNode = parentGraph.getNodesByName("Project").stream().findFirst().orElse(null);
		final OpNode parentSessionsNode = parentGraph.getNodesByName("Selected Sessions").stream().findFirst().orElse(null);
		final OpNode parentParticipantsNode = parentGraph.getNodesByName("Selected Participants").stream().findFirst().orElse(null);

		if(parentProjectNode != null) {
			try {
				final OpLink projectLink =
						new OpLink(parentProjectNode, "obj", analysisNode, "project");
				parentGraph.add(projectLink);
			} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException | InvalidEdgeException e1) {
				LOGGER.warn( e1.getLocalizedMessage(), e1);
			}
		}

		if(parentSessionsNode != null) {
			try {
				final OpLink sessionsLink =
						new OpLink(parentSessionsNode, "obj", analysisNode, "selectedSessions");
				parentGraph.add(sessionsLink);
			} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException | InvalidEdgeException e1) {
				LOGGER.warn( e1.getLocalizedMessage(), e1);
			}
		}

		if(parentParticipantsNode != null) {
			try {
				final OpLink participantsLink =
						new OpLink(parentParticipantsNode, "obj", analysisNode, "selectedParticipants");
				parentGraph.add(participantsLink);
			} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException | InvalidEdgeException e1) {
				LOGGER.warn( e1.getLocalizedMessage(), e1);
			}
		}

		for(OpNode node:analysisExt) {
			graphExtension.addNode(node);
			graphExtension.setNodeForced(node, analysisExt.isNodeForced(node));

			String nodeTitle = analysisExt.getWizardTitle();
			if(analysisExt.getNodeTitle(node).trim().length() > 0) {
				nodeTitle +=  " " + analysisExt.getNodeTitle(node);
			} else {
				nodeTitle += ( node.getName().equals("Parameters") || node.getName().equals(analysisExt.getWizardTitle()) ? "" : " " + node.getName());
			}
			graphExtension.setNodeTitle(node, nodeTitle);
		}

		for(OpNode optionalNode:analysisExt.getOptionalNodes()) {
			graphExtension.addOptionalNode(optionalNode);
			graphExtension.setOptionalNodeDefault(optionalNode, analysisExt.getOptionalNodeDefault(optionalNode));
		}
	}

	private void init() {
		PhonNodeLibrary.install(getNodeLibrary().getLibrary());
		GraphOutlineExtension.install(getDocument(), getGraphOutline(), getWizardExtension());

		getDocument().getUndoSupport().addUndoableEditListener( (e) -> {
			final UndoableEdit edit = e.getEdit();
			if(edit instanceof AddNodeEdit) {
				final OpNode addedNode = ((AddNodeEdit)edit).getNode();
				if(addedNode instanceof CompositeNode) {
					final OpGraph addedGraph = ((CompositeNode)addedNode).getGraph();

					final WizardExtension wizardExt = addedGraph.getExtension(WizardExtension.class);
					if(wizardExt != null && wizardExt instanceof AnalysisWizardExtension) {
						addAnalysisNode(addedNode);
					}
				}
			}
		});
	}

	public ParticipantsPanel getParticipantSelector() {
		return this.participantSelector;
	}

	protected WizardExtension getWizardExtension() {
		return getDocument().getRootGraph().getExtension(WizardExtension.class);
	}

	@Override
	public Tuple<String, String> getNoun() {
		return new Tuple<>("analysis", "analyses");
	}

	@Override
	protected Map<String, JComponent> getViewMap() {
		final Map<String, JComponent> retVal = super.getViewMap();
		retVal.put("Debug Settings", getDebugSettings());
		return retVal;
	}

	protected JComponent getDebugSettings() {
		if(debugSettings == null) {
			debugSettings = new JPanel();

			final Workspace workspace = Workspace.userWorkspace();
			projectList = new JComboBox<Project>(workspace.getProjects().toArray(new Project[0]));
			projectList.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Project"),
					projectList.getBorder()));

			projectList.addItemListener( (e) -> {
				participantSelector.setProject((Project)projectList.getSelectedItem());
			} );

			participantSelector = new ParticipantsPanel();
			final JScrollPane sessionScroller = new JScrollPane(participantSelector);
			sessionScroller.setBorder(BorderFactory.createTitledBorder("Sessions & Participants"));

			debugSettings.setLayout(new BorderLayout());
			debugSettings.add(projectList, BorderLayout.NORTH);
			debugSettings.add(sessionScroller, BorderLayout.CENTER);
		}
		return debugSettings;
	}
	
	@Override
	public ViewLocation getDefaultViewLocation(String viewName) {
		ViewLocation retVal = super.getDefaultViewLocation(viewName);
		
		if(viewName.equals("Debug Settings")) {
			retVal = ViewLocation.WEST;
		} else if(viewName.equals("Report Template")) {
			retVal = ViewLocation.WEST;
		}
		return retVal;
	}

	@Override
	public Rectangle getInitialViewBounds(String viewName) {
		Rectangle retVal = new Rectangle();
		switch(viewName) {
		case "Canvas":
			retVal.setBounds(200, 0, 600, 600);
			break;

		case "Debug Settings":
			retVal.setBounds(0, 200, 200, 200);
			break;

		case "Report Template":
			retVal.setBounds(0, 0, 200, 200);
			break;

		case "Console":
			retVal.setBounds(0, 200, 200, 200);
			break;

		case "Debug":
			retVal.setBounds(0, 200, 200, 200);
			break;

		case "Connections":
			retVal.setBounds(800, 200, 200, 200);
			break;

		case "Library":
			retVal.setBounds(0, 0, 200, 200);
			break;

		case "Settings":
			retVal.setBounds(800, 0, 200, 200);
			break;

		default:
			retVal.setBounds(0, 0, 200, 200);
			break;
		}
		return retVal;
	}

	@Override
	public boolean isViewVisibleByDefault(String viewName) {
		return super.isViewVisibleByDefault(viewName)
				|| viewName.equals("Debug Settings")
				|| viewName.equals("Report Template");
	}

	@Override
	public String getDefaultFolder() {
		return UserAnalysisHandler.DEFAULT_USER_ANALYSIS_FOLDER;
	}

	@Override
	public String getTitle() {
		return "Composer (Analysis)";
	}

	@Override
	public boolean validate() {
		return super.validate();
	}

	@Override
	public void setupContext(OpContext context) {
		super.setupContext(context);

		context.put("_project", projectList.getSelectedItem());
		context.put("_selectedSessions", participantSelector.getSessionSelector().getSelectedSessions());
		context.put("_selectedParticipants", participantSelector.getParticipantSelector().getSelectedParticpants());
		context.put("_buffers", new MultiBufferPanel());
	}

}
