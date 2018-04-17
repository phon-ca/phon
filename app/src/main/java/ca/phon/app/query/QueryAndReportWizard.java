package ca.phon.app.query;

import java.awt.BorderLayout;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.phon.app.opgraph.editor.SimpleEditorPanel;
import ca.phon.app.opgraph.nodes.ReportNodeInstantiator;
import ca.phon.app.opgraph.report.ReportEditorModelInstantiator;
import ca.phon.app.opgraph.report.ReportLibrary;
import ca.phon.app.opgraph.report.ReportRunner;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.wizard.WizardStep;

public class QueryAndReportWizard extends NodeWizard {
	
	private WizardStep queryStep;
	private JSplitPane splitPane;
	private SessionSelector sessionSelector;
	private ScriptPanel scriptPanel;

	private WizardStep queryResultsStep;
	private JTabbedPane queryResultsPane;
	
	private WizardStep reportEditorStep;
	private SimpleEditorPanel reportEditor;
	
	private Project project;
	private QueryScript queryScript;

	public QueryAndReportWizard(Project project, QueryScript queryScript) {
		// init with 'dummy' processor and graph as these will be created 0during the wizard
		super(queryScript.getExtension(QueryName.class).getName(), new Processor(new OpGraph()), new OpGraph());
		
		this.project = project;
		putExtension(Project.class, project);
		
		this.queryScript = queryScript;

		// add query steps
		init();
		
		gotoStep(0);
	}
	
	private void init() {
		queryStep = createQueryStep();
		addWizardStep(0, queryStep);
		
		queryResultsStep = createQueryResultsStep();
		addWizardStep(1, queryResultsStep);
		
		reportEditorStep = createReportConfigStep();
		addWizardStep(2, reportEditorStep);
		
		queryStep.setNextStep(1);
		queryResultsStep.setPrevStep(0);
		queryResultsStep.setNextStep(2);
		reportEditorStep.setPrevStep(1);
		reportEditorStep.setNextStep(3);
		
		reportDataStep.setPrevStep(2);
	}
	
	private WizardStep createQueryStep() {
		WizardStep retVal = new WizardStep();
		QueryName qn = queryScript.getExtension(QueryName.class);
		retVal.setTitle("Query : " + qn.getName());
		
		sessionSelector = new SessionSelector(this.project);
		TitledPanel sessionsPanel = new TitledPanel("Select Sessions", new JScrollPane(sessionSelector));
		
		scriptPanel = new ScriptPanel(queryScript);
		TitledPanel queryPanel = new TitledPanel("Query : " + qn.getName(), scriptPanel);
		
		splitPane = new JSplitPane();
		splitPane.setLeftComponent(sessionsPanel);
		splitPane.setRightComponent(queryPanel);
		
		SwingUtilities.invokeLater( ()-> splitPane.setDividerLocation(0.4f) );
		
		retVal.setLayout(new BorderLayout());
		retVal.add(splitPane, BorderLayout.CENTER);
		
		return retVal;
	}
	
	private WizardStep createQueryResultsStep() {
		WizardStep retVal = new WizardStep();
		retVal.setTitle("Results");
		
		queryResultsPane = new JTabbedPane();
		TitledPanel resultsPane = new TitledPanel("Result Sets", queryResultsPane);
		
		retVal.setLayout(new BorderLayout());
		retVal.add(resultsPane, BorderLayout.CENTER);
		
		return retVal;
	}
	
	private WizardStep createReportConfigStep() {
		WizardStep retVal = new WizardStep();
		retVal.setTitle("Report Setup");
		
		reportEditor = new SimpleEditorPanel(
				project,
				new ReportLibrary(), new ReportEditorModelInstantiator(), new ReportNodeInstantiator(),
				(qs) -> new MacroNode(),
				(graph, project) -> new ReportRunner(graph, getCurrentQueryProject(), getCurrentQueryId()) );
		TitledPanel configPane = new TitledPanel("Report Configuration", reportEditor);
		
		retVal.setLayout(new BorderLayout());
		retVal.add(configPane, BorderLayout.CENTER);
		
		return retVal;
	}
	
	private String getCurrentQueryId() {
		if(queryResultsPane.getTabCount() == 0)
			return "";
	
		final QueryRunnerPanel runnerPanel = 
				(QueryRunnerPanel)queryResultsPane.getComponentAt(queryResultsPane.getSelectedIndex());
		return runnerPanel.getQuery().getUUID().toString();
	}
	
	private Project getCurrentQueryProject() {
		if(queryResultsPane.getTabCount() == 0)
			return null;
	
		final QueryRunnerPanel runnerPanel = 
				(QueryRunnerPanel)queryResultsPane.getComponentAt(queryResultsPane.getSelectedIndex());
		return (runnerPanel.isSaved() ? runnerPanel.getProject() : runnerPanel.getTempProject());
	}
	
	public void executeQuery() {
		final QueryRunnerPanel runnerPanel = 
				new QueryRunnerPanel(project, queryScript, sessionSelector.getSelectedSessions(), isIncludeExcluded());
		queryResultsPane.add("Results", runnerPanel);
		queryResultsPane.setSelectedComponent(runnerPanel);
		queryResultsPane.revalidate();
		
		runnerPanel.startQuery();
	}
	
	public boolean isIncludeExcluded() {
		return false;
	}

	@Override
	public void next() {
		if(getCurrentStep() == queryStep) {
			if(sessionSelector.getSelectedSessions().size() == 0) {
				return;
			}
			if(!scriptPanel.checkParams()) {
				return;
			}
		}
		super.next();
	}
	
	@Override
	public void gotoStep(int stepIdx) {
		if(stepIdx == super.getStepIndex(queryResultsStep) && getCurrentStep() == queryStep) {
			// create a new query runner panel
			executeQuery();
		} else if(!inInit && stepIdx == super.getStepIndex(reportDataStep)) {
			
			// install correct processor
			final OpGraph graph = reportEditor.getGraph();
			final Processor processor = new Processor(graph);
			processor.getContext().put("_project", getCurrentQueryProject());
			processor.getContext().put("_queryId", getCurrentQueryId());
			processor.getContext().put("_selectedSessions", sessionSelector.getSelectedSessions());
			
			setProcessor(processor);
		}
		super.gotoStep(stepIdx);
	}
	
}
