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
package ca.phon.app.opgraph.nodes.query;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.nodes.RecordContainer;
import ca.phon.app.opgraph.nodes.RecordContainerTypeValidator;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.query.ScriptPanel;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryFactory;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.Script;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptContext;
import ca.phon.query.script.QueryTask;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;

@OpNodeInfo(
	category="Query",
	name="Query Node",
	description="Query script node",
	showInLibrary=true
)
public class QueryNode extends OpNode implements NodeSettings {
	
	private QueryScript queryScript;
	
	private InputField projectInputField = new InputField("project", "Project", false, true, Project.class);
	
	private InputField sessionsInputField = new InputField("sessions", "List of sessions or query results", false,
			true, new RecordContainerTypeValidator());
	
	private InputField paramsInputField = new InputField("parameters", "Map of query parameters, these will override query settings.",
			true, true, Map.class);
	
	private OutputField projectOutputField = new OutputField("project", "Project", true, Project.class);
	
	private OutputField queryField = new OutputField("query",
			"Query parameters", true, Query.class);

	private OutputField resultsField = new OutputField("results", 
			"Result set, one per input session", true, ResultSet[].class);
	
	private OutputField scriptOutput = new OutputField("buffer",
			"Text output from query", true, String.class);
	
	private OutputField paramsOutputField = new OutputField("parameters",
			"Parameters used for query, including those entered using the settings dialog", true, Map.class);
	
	public QueryNode() {
		this(new QueryScript(""));
	}
	
	public QueryNode(QueryScript queryScript) {
		super();
		
		this.queryScript = queryScript;
		
		super.putField(projectInputField);
		super.putField(sessionsInputField);
		super.putField(paramsInputField);
		super.putField(projectOutputField);
		super.putField(queryField);
		super.putField(paramsOutputField);
		super.putField(resultsField);
		super.putField(scriptOutput);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext opCtx) throws ProcessingException {
		final Project project = (Project)opCtx.get(projectInputField);
		if(project == null) throw new ProcessingException(null, "No project available");
		
		final Object inputObj = opCtx.get(sessionsInputField);
		if(inputObj == null) throw new ProcessingException(null, "No session information given");
		
		final QueryManager qm = QueryManager.getInstance();
		final QueryFactory queryFactory = qm.createQueryFactory();
		final Query query = queryFactory.createQuery(project);
		
		final QueryScript queryScript = getQueryScript();
		final QueryScriptContext ctx = queryScript.getQueryContext();
		
		ScriptParameters scriptParams = new ScriptParameters();
		try {
			scriptParams = ctx.getScriptParameters(ctx.getEvaluatedScope());
		} catch (PhonScriptException e) {
			throw new ProcessingException(null, e);
		}
		
		final Script qScript = query.getScript();
		qScript.setSource(queryScript.getScript());
		final Map<String, String> sparams = new HashMap<String, String>();
		for(ScriptParam sp:scriptParams) {
			if(sp.hasChanged()) {
				for(String paramid:sp.getParamIds()) {
					sparams.put(paramid, sp.getValue(paramid).toString());
				}
			}
		}
		qScript.setParameters(sparams);
		qScript.setMimeType("text/javascript");
		query.setDate(LocalDateTime.now());

		final Map<?, ?> inputParams = (Map<?,?>)opCtx.get(paramsInputField);
		final Map<String, Object> allParams = new LinkedHashMap<>();
		for(ScriptParam sp:scriptParams) {
			for(String paramId:sp.getParamIds()) {
				if(inputParams != null && inputParams.containsKey(paramId)) {
					sp.setValue(paramId, inputParams.get(paramId));
				}
				
				if(paramId.endsWith("ignoreDiacritics")
						&& opCtx.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION)) {
					sp.setValue(paramId, opCtx.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION));
				}
				
				if(paramId.endsWith("caseSensitive")
						&& opCtx.containsKey(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION)) {
					sp.setValue(paramId, opCtx.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION));
				}
				
				allParams.put(paramId, sp.getValue(paramId));
			}
		}
		
		// ensure query form validates (if available)
		if(scriptPanel != null && !scriptPanel.checkParams()) {
			throw new ProcessingException(null, "Invalid query settings");
		}
				
		final List<RecordContainer> recordContainers =
				RecordContainer.toRecordContainers(project, inputObj);
		
		final ResultSet[] results = new ResultSet[recordContainers.size()];
		
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final PrintStream scriptOutputStream = new PrintStream(bOut);
		ctx.redirectStdErr(scriptOutputStream);
		ctx.redirectStdOut(scriptOutputStream);
		
		int serial = 0;
		for(RecordContainer rc:recordContainers) {
			checkCanceled();
			try {
				QueryTask task = new QueryTask(project, rc.getSession(), rc.idxIterator(), queryScript, ++serial);
				task.run();
				results[serial-1] = task.getResultSet();
			} catch (Exception e) {
				throw new ProcessingException(null, e);
			}
		}
		
		// setup outputs
		opCtx.put(projectOutputField, project);
		opCtx.put(resultsField, results);
		opCtx.put(scriptOutput, new String(bOut.toByteArray()));
		opCtx.put(queryField, query);
		opCtx.put(paramsOutputField, allParams);
	}
	
	public QueryScript getQueryScript() {
		if(scriptPanel != null) {
			return (QueryScript)scriptPanel.getScript();
		} else {
			return this.queryScript;
		}
	}

	private ScriptPanel scriptPanel;
	@Override
	public Component getComponent(GraphDocument arg0) {
		if(scriptPanel == null) {
			scriptPanel = new ScriptPanel(getQueryScript());
		}
		return scriptPanel;
	}

	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties arg0) {
		
	}

}
