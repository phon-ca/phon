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

import ca.phon.app.opgraph.nodes.*;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.query.QueryHistoryAndNameToolbar;
import ca.phon.app.script.ScriptPanel;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.project.Project;
import ca.phon.query.db.*;
import ca.phon.query.history.QueryHistoryManager;
import ca.phon.query.script.*;
import ca.phon.script.*;
import ca.phon.script.params.*;
import ca.phon.session.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.TaskStatus;

import javax.swing.*;
import java.awt.*;
import java.beans.*;
import java.io.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;

@OpNodeInfo(
	category="Query",
	name="Query Node",
	description="Query script node",
	showInLibrary=true
)
public class QueryNode extends OpNode implements NodeSettings, ScriptNode {

	private QueryScript queryScript;
	
	private QueryHistoryManager queryHistoryManager;

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

	private OutputField scriptOutputField = new OutputField("script", "Query script", true, QueryScript.class);

	private OutputField bufferOutputField = new OutputField("buffer",
			"Text output from query", true, String.class);

	private OutputField paramsOutputField = new OutputField("parameters",
			"Parameters used for query, including those entered using the settings dialog", true, Map.class);
	
	private PropertyChangeSupport propSupport;
	
	public QueryNode() {
		this(new QueryScript(""));
	}

	public QueryNode(QueryScript queryScript) {
		super();

		this.queryScript = queryScript;
		this.queryHistoryManager = QueryHistoryManager.getCachedInstance(queryScript);
		
		super.putField(projectInputField);
		super.putField(sessionsInputField);
		super.putField(paramsInputField);
		super.putField(projectOutputField);
		super.putField(queryField);
		super.putField(paramsOutputField);
		super.putField(resultsField);
		super.putField(scriptOutputField);
		super.putField(bufferOutputField);
		
		this.propSupport = new PropertyChangeSupport(this);

		putExtension(NodeSettings.class, this);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return propSupport.getPropertyChangeListeners();
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(propertyName, listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
		return propSupport.getPropertyChangeListeners(propertyName);
	}

	public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, int oldValue, int newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) {
		propSupport.firePropertyChange(propertyName, oldValue, newValue);
	}

	public void firePropertyChange(PropertyChangeEvent event) {
		propSupport.firePropertyChange(event);
	}

	public boolean hasListeners(String propertyName) {
		return propSupport.hasListeners(propertyName);
	}

	@Override
	public void operate(OpContext opCtx) throws ProcessingException {
		boolean isStepInto = (opCtx.containsKey("__stepInto") ? Boolean.valueOf(opCtx.get("__stepInto").toString()) : false);

		final Project project = (Project)opCtx.get(projectInputField);
		if(project == null) throw new ProcessingException(null, "No project available");

		final Object inputObj = opCtx.get(sessionsInputField);
		if(inputObj == null) throw new ProcessingException(null, "No session information given");

		final Collection<Participant> selectedParticipants = (Collection<Participant>) opCtx.get("_selectedParticipants");
		if(selectedParticipants == null) throw new ProcessingException(null, "No selected participants");

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
						&& opCtx.containsKey(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION)
						&& !opCtx.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION).equals("default")) {
					sp.setValue(paramId, opCtx.get(NodeWizard.IGNORE_DIACRITICS_GLOBAL_OPTION));
				}

				if(paramId.endsWith("caseSensitive")
						&& opCtx.containsKey(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION)
						&& !opCtx.get(NodeWizard.CASE_SENSITIVE_GLOBAL_OPTION).equals("default")) {
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
				RecordContainer.toRecordContainers(project, selectedParticipants, inputObj);

		final ResultSet[] results = new ResultSet[recordContainers.size()];

		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final PrintStream scriptOutputStream = new PrintStream(bOut);
		ctx.redirectStdErr(scriptOutputStream);
		ctx.redirectStdOut(scriptOutputStream);

		int serial = 0;
		Session currentSession = null;
		QueryTask currentTask = null;
		boolean debugSession = isStepInto;
		for(RecordContainer rc:recordContainers) {
			checkCanceled();
			Session session = rc.getSession();
			firePropertyChange("session", currentSession, session);
			currentSession = session;
			try {
				QueryTask task = new QueryTask(project, currentSession, rc.idxIterator(), queryScript, ++serial);
				task.setDebugSession(debugSession);
				task.setDebugRecord(0);
				// only debug first session
				debugSession = false;
				task.addTaskListener( new PhonTaskListener() {
					
					@Override
					public void statusChanged(PhonTask task, TaskStatus oldStatus, TaskStatus newStatus) {
						firePropertyChange("task", oldStatus, newStatus);
					}
					
					@Override
					public void propertyChanged(PhonTask task, String property, Object oldValue, Object newValue) {
						firePropertyChange(property, oldValue, newValue);
					}
					
				});
				firePropertyChange("queryTask", currentTask, task);
				currentTask = task;
				task.run();
				results[serial-1] = task.getResultSet();
			} catch (Exception e) {
				throw new ProcessingException(null, e);
			}
		}
		firePropertyChange("numCompleted", serial-1, serial);

		// setup outputs
		opCtx.put(projectOutputField, project);
		opCtx.put(resultsField, results);
		opCtx.put(bufferOutputField, new String(bOut.toByteArray()));
		opCtx.put(queryField, query);
		opCtx.put(paramsOutputField, allParams);
		opCtx.put(scriptOutputField, queryScript);
	}
	
	@Override
	public PhonScript getScript() {
		return getQueryScript();
	}

	public QueryScript getQueryScript() {
		if(scriptPanel != null) {
			return (QueryScript)scriptPanel.getScript();
		} else {
			return this.queryScript;
		}
	}

	public class QueryNodeSettingsPanel extends JPanel {
		
		private ScriptPanel scriptPanel;
		
		public QueryNodeSettingsPanel(ScriptPanel scriptPanel) {
			super(new BorderLayout());
			this.scriptPanel = scriptPanel;
		}
		
		public ScriptPanel getScriptPanel() {
			return this.scriptPanel;
		}
		
	}
	
	private QueryNodeSettingsPanel settingsPanel;
	private ScriptPanel scriptPanel;
	private QueryHistoryAndNameToolbar queryHistoryPanel;

	public ScriptPanel getScriptPanel() { return scriptPanel; }

	public OpNode toOpNode() { return this; }

	public void reloadFields() throws PhonScriptException { }
	
	public QueryHistoryAndNameToolbar getQueryHistoryAndNameToolbar() {
		return queryHistoryPanel;
	}
	
	@Override
	public Component getComponent(GraphDocument arg0) {
		if(settingsPanel == null) {
			scriptPanel = new ScriptPanel(getQueryScript());
			settingsPanel = new QueryNodeSettingsPanel(scriptPanel);
			
			queryHistoryPanel = new QueryHistoryAndNameToolbar(queryHistoryManager, scriptPanel);
			
			settingsPanel.add(queryHistoryPanel, BorderLayout.NORTH);
			JScrollPane scriptScroller = new JScrollPane(scriptPanel);
			scriptScroller.getViewport().setBackground(scriptPanel.getBackground());
			settingsPanel.add(scriptScroller, BorderLayout.CENTER);
		}
		return settingsPanel;
	}

	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties arg0) {

	}

}
