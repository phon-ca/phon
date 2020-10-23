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
package ca.phon.app.opgraph.nodes.query;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.nodes.*;
import ca.phon.app.opgraph.report.*;
import ca.phon.app.opgraph.report.tree.*;
import ca.phon.app.project.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.nodes.general.*;
import ca.phon.project.*;
import ca.phon.project.exceptions.*;
import ca.phon.query.db.*;
import ca.phon.session.*;
import ca.phon.ui.*;

/**
 * Select query report from file or created using the Report Composer.
 *  
 */
@OpNodeInfo(name="Query Report", category="Query", description="Execute query report", showInLibrary=true)
public class QueryReportNode extends OpNode implements NodeSettings {
	
	private final static String DEFAULT_REPORT_LOCATION = "ca/phon/app/query/default_report.xml";
	
	private InputField projectInput = new InputField("project", "temporary project input", false, true, Project.class);
	
	private InputField queryInput = new InputField("query", "query id", false, true, Query.class);
	
	private InputField resultsInput = new InputField("results", "List of sessions or query results", false,
			true, ResultSet[].class);
	
	private OutputField reportOutput = new OutputField("report", "report tree", true, ReportTree.class);
	
	private JPanel settingsPanel;
	private SimpleEditorPanel reportEditorPanel;
	private OpGraph reportGraph;
	
	public static URL getDefaultReportURL() {
		return ClassLoader.getSystemResource(DEFAULT_REPORT_LOCATION);
	}
	
	public QueryReportNode() {
		this(new OpGraph());
	}
	
	public QueryReportNode(OpGraph reportGraph) {
		super();
		
		this.reportGraph = reportGraph;
		
		setupFields();
		putExtension(NodeSettings.class, this);
	}
	
	private void setupFields() {
		putField(projectInput);
		putField(queryInput);
		putField(resultsInput);
		
		putField(reportOutput);
	}
	
	public OpGraph getReportGraph() {
		return (reportEditorPanel != null ? reportEditorPanel.getGraph() : this.reportGraph);
	}
	
	public void setReportGraph(OpGraph reportGraph) {
		this.reportGraph = reportGraph;
		if(reportEditorPanel != null) {
			reportEditorPanel.getModel().getDocument().reset(null, reportGraph);
		}
	}

	@Override
	public void operate(OpContext ctx) throws ProcessingException {
		final Project project = (Project)ctx.get(projectInput);
		final Query query = (Query)ctx.get(queryInput);
		final ResultSet[] results = (ResultSet[])ctx.get(resultsInput);
		
		final OpGraph graph = getReportGraph();
		if(graph == null)
			throw new ProcessingException(null, "Report graph not found");
		
		// save query to temporary project
		List<SessionPath> sessions = new ArrayList<>();
		try {
			final Project tempProject = ShadowProject.of(project);
			QueryManager qm = QueryManager.getSharedInstance();
			ResultSetManager manager = qm.createResultSetManager();
			manager.saveQuery(tempProject, query);
			
			for(ResultSet resultSet:results) {
				manager.saveResultSet(tempProject, query, resultSet);
				sessions.add(new SessionPath(resultSet.getSessionPath()));
			}
			
			final ReportTree reportTree = new ReportTree();
			final Processor processor = new Processor(graph);
						
			processor.getContext().put("_project", tempProject);
			processor.getContext().put("_queryId", query.getUUID().toString());
			processor.getContext().put("_selectedSessions", sessions);
			processor.getContext().put("_reportTree", reportTree);
			
			// execute graph
			processor.stepAll();
			
			ctx.put(reportOutput, reportTree);
		} catch (IOException | ProjectConfigurationException e) {
			throw new ProcessingException(null, e);
		}
	}
		
	@Override
	public Component getComponent(GraphDocument doc) {
		if(settingsPanel == null) {
			settingsPanel = new JPanel(new BorderLayout());
			
			CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
			reportEditorPanel = new SimpleEditorPanel( (cmf != null ? cmf.getExtension(Project.class) : null),
					new ReportLibrary(), this.reportGraph, new ReportEditorModelInstantiator(), new ReportNodeInstantiator(),
					(qs, reportGraph) -> new MacroNode(),
					null );
			settingsPanel.add(reportEditorPanel, BorderLayout.CENTER);
		}
		return settingsPanel;
	}

	// settings are handled by a custom xml serializer
	@Override
	public Properties getSettings() {
		Properties retVal = new Properties();
		return retVal;
	}

	@Override
	public void loadSettings(Properties arg0) {
		
	}

}
