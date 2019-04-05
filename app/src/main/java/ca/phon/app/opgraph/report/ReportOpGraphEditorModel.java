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
package ca.phon.app.opgraph.report;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.CompoundBorder;
import javax.swing.undo.UndoableEdit;

import ca.phon.app.opgraph.analysis.AnalysisWizardExtension;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.library.PhonNodeLibrary;
import ca.phon.app.opgraph.wizard.GraphOutlineExtension;
import ca.phon.app.opgraph.wizard.NodeWizardReportTemplate;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.query.QueryHistoryTableModel;
import ca.phon.app.workspace.Workspace;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpLink;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.app.edits.graph.AddNodeEdit;
import ca.phon.opgraph.dag.CycleDetectedException;
import ca.phon.opgraph.dag.InvalidEdgeException;
import ca.phon.opgraph.dag.VertexNotFoundException;
import ca.phon.opgraph.exceptions.ItemMissingException;
import ca.phon.opgraph.extensions.CompositeNode;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.util.Tuple;

public class ReportOpGraphEditorModel extends OpgraphEditorModel {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(ReportOpGraphEditorModel.class.getName());

	private JPanel debugSettings;

	private JComboBox<Project> projectList;

	private JTable queryTable;

	public ReportOpGraphEditorModel() {
		this(new OpGraph());
	}

	public ReportOpGraphEditorModel(OpGraph graph) {
		super(graph);

		WizardExtension wizardExt = graph.getExtension(WizardExtension.class);
		if(wizardExt == null || (wizardExt instanceof AnalysisWizardExtension && wizardExt.size() == 0)) {
			wizardExt = new ReportWizardExtension(graph);
			graph.putExtension(WizardExtension.class, wizardExt);
		}

		init();
	}

	protected WizardExtension getWizardExtension() {
		return getDocument().getRootGraph().getExtension(WizardExtension.class);
	}

	@Override
	public Tuple<String, String> getNoun() {
		return new Tuple<>("Report", "Reports");
	}

	@Override
	protected Map<String, JComponent> getViewMap() {
		final Map<String, JComponent> retVal = super.getViewMap();
		retVal.put("Debug Settings", getDebugSettings());
		return retVal;
	}

	private void addReportNode(OpNode reportNode) {
		final WizardExtension graphExtension = getWizardExtension();
		final OpGraph parentGraph = getDocument().getGraph();
		final WizardExtension reportExt = ((CompositeNode)reportNode).getGraph().getExtension(WizardExtension.class);

		final OpNode parentProjectNode = parentGraph.getNodesByName("Project").stream().findFirst().orElse(null);
		final OpNode parentQueryIDNode = parentGraph.getNodesByName("Query ID").stream().findFirst().orElse(null);
		final OpNode parentSelectedResultsNode = parentGraph.getNodesByName("Selected Results").stream().findFirst().orElse(null);

		if(parentProjectNode != null) {
			try {
				final OpLink projectLink =
						new OpLink(parentProjectNode, "obj", reportNode, "project");
				parentGraph.add(projectLink);
			} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException | InvalidEdgeException e1) {
				LOGGER.warn( e1.getLocalizedMessage(), e1);
			}
		}

		if(parentQueryIDNode != null) {
			try {
				final OpLink queryIdLink =
						new OpLink(parentQueryIDNode, "obj", reportNode, "queryId");
				parentGraph.add(queryIdLink);
			} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException | InvalidEdgeException e1) {
				LOGGER.warn( e1.getLocalizedMessage(), e1);
			}
		}
		
		if(parentSelectedResultsNode != null) {
			try {
				final OpLink resultsLink = 
						new OpLink(parentSelectedResultsNode, "obj", reportNode, "selectedResults");
				parentGraph.add(resultsLink);
			} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException | InvalidEdgeException e1) {
				LOGGER.warn( e1.getLocalizedMessage(), e1);
			}
		}

		for(OpNode node:reportExt) {
			graphExtension.addNode(node);
			graphExtension.setNodeForced(node, reportExt.isNodeForced(node));

			String nodeTitle = reportExt.getWizardTitle();
			if(reportExt.getNodeTitle(node).trim().length() > 0) {
				nodeTitle +=  " " + reportExt.getNodeTitle(node);
			} else {
				nodeTitle += ( node.getName().equals("Parameters") || node.getName().equals(reportExt.getWizardTitle()) ? "" : " " + node.getName());
			}
			graphExtension.setNodeTitle(node, nodeTitle);
		}

		for(OpNode optionalNode:reportExt.getOptionalNodes()) {
			graphExtension.addOptionalNode(optionalNode);
			graphExtension.setOptionalNodeDefault(optionalNode, reportExt.getOptionalNodeDefault(optionalNode));
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
					if(wizardExt != null && wizardExt instanceof ReportWizardExtension) {
						addReportNode(addedNode);
					}
				}
			}
		} );
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

	protected JComponent getDebugSettings() {
		if(debugSettings == null) {
			debugSettings = new JPanel(new BorderLayout());

			final Workspace workspace = Workspace.userWorkspace();
			projectList = new JComboBox<Project>(workspace.getProjects().toArray(new Project[0]));
			projectList.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Project"),
					projectList.getBorder()));

			projectList.addItemListener( (ItemEvent e) -> {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					final QueryHistoryTableModel model = new QueryHistoryTableModel((Project)projectList.getSelectedItem());
					queryTable.setModel(model);
					model.update();
				}
			});

			queryTable = new JTable();
			final JScrollPane queryScroller = new JScrollPane(queryTable);
			queryScroller.setBorder(BorderFactory.createTitledBorder("Query History"));

			debugSettings.add(projectList, BorderLayout.NORTH);
			debugSettings.add(queryScroller, BorderLayout.CENTER);
		}
		return debugSettings;
	}

	@Override
	public void setupContext(OpContext context) {
		if(projectList.getSelectedItem() != null) {
			context.put("_project", projectList.getSelectedItem());
			if(queryTable.getModel() instanceof QueryHistoryTableModel) {
				final QueryHistoryTableModel model = (QueryHistoryTableModel)queryTable.getModel();
				final int selectedIdx = queryTable.convertRowIndexToModel(queryTable.getSelectedRow());
				if(selectedIdx >= 0) {
					final Query selectedQuery = model.getQueryForRow(selectedIdx);
					context.put("_queryId", selectedQuery.getUUID().toString());
				}
			}
		}
	}

	@Override
	public String getDefaultFolder() {
		return UserReportHandler.DEFAULT_USER_REPORT_FOLDER;
	}

	@Override
	public String getTitle() {
		return "Composer (Report)";
	}

}
