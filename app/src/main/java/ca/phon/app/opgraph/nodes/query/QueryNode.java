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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.nodes.*;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.query.ScriptPanel;
import ca.phon.project.Project;
import ca.phon.query.db.*;
import ca.phon.query.script.*;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.*;
import ca.phon.session.Session;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTaskListener;
import ca.phon.worker.PhonTask.TaskStatus;

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
		Session currentSession = null;
		QueryTask currentTask = null;
		for(RecordContainer rc:recordContainers) {
			checkCanceled();
			Session session = rc.getSession();
			firePropertyChange("session", currentSession, session);
			currentSession = session;
			try {
				QueryTask task = new QueryTask(project, currentSession, rc.idxIterator(), queryScript, ++serial);
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
			
			if(CommonModuleFrame.getCurrentFrame() instanceof OpgraphEditor) {
				scriptPanel.setSwapButtonVisible(true);
			}
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
