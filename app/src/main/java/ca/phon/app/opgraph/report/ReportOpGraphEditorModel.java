/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2017, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.app.opgraph.report;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Map;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.undo.UndoableEdit;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.app.edits.graph.AddNodeEdit;
import ca.gedge.opgraph.dag.*;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.extensions.CompositeNode;
import ca.phon.app.opgraph.analysis.AnalysisWizardExtension;
import ca.phon.app.opgraph.editor.OpgraphEditorModel;
import ca.phon.app.opgraph.nodes.PhonNodeLibrary;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.app.query.QueryHistoryTableModel;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.util.Tuple;
import ca.phon.workspace.Workspace;

public class ReportOpGraphEditorModel extends OpgraphEditorModel {

	private final static Logger LOGGER = Logger.getLogger(ReportOpGraphEditorModel.class.getName());

	private JPanel debugSettings;

	private ReportTemplateView reportTemplateView;

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
		retVal.put("Report Template", getReportTemplateView());
		retVal.put("Debug Settings", getDebugSettings());
		return retVal;
	}

	protected JComponent getReportTemplateView() {
		if(reportTemplateView == null) {
			reportTemplateView = new ReportTemplateView(getDocument());
		}
		return reportTemplateView;
	}

	private void addReportNode(OpNode reportNode) {
		final WizardExtension graphExtension = getWizardExtension();
		final OpGraph parentGraph = getDocument().getGraph();
		final WizardExtension reportExt = ((CompositeNode)reportNode).getGraph().getExtension(WizardExtension.class);

		final OpNode parentProjectNode = parentGraph.getNodesByName("Project").stream().findFirst().orElse(null);
		final OpNode parentQueryIDNode = parentGraph.getNodesByName("Query ID").stream().findFirst().orElse(null);

		if(parentProjectNode != null) {
			try {
				final OpLink projectLink =
						new OpLink(parentProjectNode, "obj", reportNode, "project");
				parentGraph.add(projectLink);
			} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e1) {
				LOGGER.log(Level.WARNING, e1.getLocalizedMessage(), e1);
			}
		}

		if(parentQueryIDNode != null) {
			try {
				final OpLink queryIdLink =
						new OpLink(parentQueryIDNode, "obj", reportNode, "queryId");
				parentGraph.add(queryIdLink);
			} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e1) {
				LOGGER.log(Level.WARNING, e1.getLocalizedMessage(), e1);
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

		// copy report template
		final NodeWizardReportTemplate prefixTemplate = graphExtension.getReportTemplate("Report Prefix");
		final NodeWizardReportTemplate suffixTemplate = graphExtension.getReportTemplate("Report Suffix");
		final NodeWizardReportTemplate pt =
				reportExt.getReportTemplate("Report Prefix");
		if(pt != null) {
			if(!prefixTemplate.getTemplate().contains(pt.getTemplate())) {
				prefixTemplate.setTemplate(prefixTemplate.getTemplate() + "\n" + pt.getTemplate());
			}
		}

		final NodeWizardReportTemplate st =
				reportExt.getReportTemplate("Report Suffix");
		if(st != null) {
			if(!suffixTemplate.getTemplate().contains(st.getTemplate())) {
				suffixTemplate.setTemplate(suffixTemplate.getTemplate() + "\n" + st.getTemplate());
			}
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
			final QueryHistoryTableModel model = (QueryHistoryTableModel)queryTable.getModel();
			final int selectedIdx = queryTable.convertRowIndexToModel(queryTable.getSelectedRow());
			if(selectedIdx >= 0) {
				final Query selectedQuery = model.getQueryForRow(selectedIdx);
				context.put("_queryId", selectedQuery.getUUID().toString());
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
