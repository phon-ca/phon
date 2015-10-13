package ca.phon.app.query.opgraph;

import java.awt.Component;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.query.report.ReportEditor;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.ResultSet;
import ca.phon.query.report.ReportBuilder;
import ca.phon.query.report.ReportBuilderException;
import ca.phon.query.report.ReportBuilderFactory;
import ca.phon.query.report.io.ReportDesign;

@OpNodeInfo(
		name="Report",
		category="Report",
		description="Create report from a pre-defined design file",
		showInLibrary=true
)
public class ReportDesignNode extends OpNode implements NodeSettings {
	
	private InputField projectInputField = 
			new InputField("project", "Project", false, true, Project.class);
	
	private InputField queryInputField =
			new InputField("query", "Query", false, true, Query.class);

	private InputField resultSetsField =
			new InputField("result sets", "Result sets from query", false, true, ResultSet[].class);
	
	private OutputField projectOutputField = 
			new OutputField("project", "Project", true, Project.class);
	

	private OutputField reportField = 
			new OutputField("report", "Generated report as a string", true, String.class);
	
	private ReportDesign reportDesign;
	
	private ReportEditor reportEditor;
	
	public ReportDesignNode() {
		this(new ReportDesign());
	}
	
	public ReportDesignNode(ReportDesign reportDesign) {
		super();
		
		this.reportDesign = reportDesign;
		
		putField(projectInputField);
		putField(queryInputField);
		putField(resultSetsField);
		putField(projectOutputField);
		putField(reportField);
		
		putExtension(NodeSettings.class, this);
	}
	
	@Override
	public void operate(OpContext context) throws ProcessingException {
		final Project project = (Project)context.get(projectInputField);
		if(project == null) throw new ProcessingException(null, "Project cannot be null");
		
		final Query query = (Query)context.get(queryInputField);
		if(query == null) throw new ProcessingException(null, "Query cannot be null");
		
		final ResultSet[] resultSets = (ResultSet[])context.get(resultSetsField);
		if(resultSets == null || resultSets.length == 0)
			throw new ProcessingException(null, "No result sets given");
		
		final ReportBuilder builder = ReportBuilderFactory.getInstance().getBuilder("CSV");
		try {
			final ByteArrayOutputStream bout = new ByteArrayOutputStream();
			builder.buildReport(getReportDesign(), project, query, resultSets, bout);
			
			context.put(reportField, new String(bout.toByteArray(), "UTF-8"));
		} catch (ReportBuilderException | UnsupportedEncodingException e) {
			throw new ProcessingException(null, e);
		}
		
		context.put(projectOutputField, project);
	}
	
	public ReportDesign getReportDesign() {
		return 
				(this.reportEditor != null ? this.reportEditor.getReportDesign() : this.reportDesign);
	}

	@Override
	public Component getComponent(GraphDocument document) {
		if(this.reportEditor == null) {
			this.reportEditor = 
					(this.reportDesign == null ? new ReportEditor() : new ReportEditor(reportDesign));
		}
		return this.reportEditor;
	}

	@Override
	public Properties getSettings() {
		return new Properties();
	}

	@Override
	public void loadSettings(Properties properties) {
		
	}

}
