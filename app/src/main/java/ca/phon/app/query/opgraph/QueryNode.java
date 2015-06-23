package ca.phon.app.query.opgraph;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.joda.time.DateTime;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
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
	showInLibrary=false
)
public class QueryNode extends OpNode implements NodeSettings {
	
	private QueryScript queryScript;
	
	private InputField projectInputField = new InputField("project", "Project", false, true, Project.class);
	
	private InputField inputField = new InputField("record containers", "List of record containers", false,
			true, new RecordContainerTypeValidator());
	
	private OutputField projectOutputField = new OutputField("project", "Project", true, Project.class);
	
	private OutputField queryField = new OutputField("query",
			"Query parameters", true, Query.class);

	private OutputField outputField = new OutputField("result sets", 
			"Result set, one per input record container", true, ResultSet[].class);
	
	private OutputField scriptOutput = new OutputField("buffer",
			"Text output from query", true, String.class);
	
	public QueryNode() {
		this(new QueryScript(""));
	}
	
	public QueryNode(QueryScript queryScript) {
		super();
		
		this.queryScript = queryScript;
		
		super.putField(projectInputField);
		super.putField(inputField);
		super.putField(projectOutputField);
		super.putField(queryField);
		super.putField(outputField);
		super.putField(scriptOutput);
		
		putExtension(NodeSettings.class, this);
	}

	@Override
	public void operate(OpContext opCtx) throws ProcessingException {
		final Project project = (Project)opCtx.get(projectInputField);
		if(project == null) throw new ProcessingException("No project available");
		
		final Object inputObj = opCtx.get(inputField);
		if(inputObj == null) throw new ProcessingException("No session information given");
		
		// ensure query form validates (if available)
		if(scriptPanel != null && !scriptPanel.checkParams()) {
			throw new ProcessingException("Invalid query settings");
		}
		
		final QueryManager qm = QueryManager.getInstance();
		final QueryFactory queryFactory = qm.createQueryFactory();
		final Query query = queryFactory.createQuery(project);
		
		final QueryScript queryScript = getQueryScript();
		final QueryScriptContext ctx = queryScript.getQueryContext();
		
		ScriptParameters scriptParams = new ScriptParameters();
		try {
			scriptParams = ctx.getScriptParameters(ctx.getEvaluatedScope());
		} catch (PhonScriptException e) {
			throw new ProcessingException(e);
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
		
		query.setDate(DateTime.now());
		
		final List<RecordContainer> recordContainers =
				RecordContainer.toRecordContainers(project, inputObj);
		
		final ResultSet[] results = new ResultSet[recordContainers.size()];
		
		final ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		final PrintStream scriptOutputStream = new PrintStream(bOut);
		ctx.redirectStdErr(scriptOutputStream);
		ctx.redirectStdOut(scriptOutputStream);
		
		int serial = 0;
		for(RecordContainer rc:recordContainers) {
			try {
				QueryTask task = new QueryTask(project, rc.getSession(), rc.idxIterator(), queryScript, ++serial);
				task.run();
				results[serial-1] = task.getResultSet();
			} catch (Exception e) {
				throw new ProcessingException(e);
			}
		}
		
		// setup outputs
		opCtx.put(projectOutputField, project);
		opCtx.put(outputField, results);
		opCtx.put(scriptOutput, new String(bOut.toByteArray()));
		opCtx.put(queryField, query);
	}
	
	public QueryScript getQueryScript() {
		if(scriptPanel != null) {
			return scriptPanel.getScript();
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
