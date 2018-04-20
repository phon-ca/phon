package ca.phon.app.query;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
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
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.app.session.SessionSelector;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class QueryAndReportWizard extends NodeWizard {
	
	private WizardStep queryStep;
	private JSplitPane splitPane;
	private SessionSelector sessionSelector;
	private ScriptPanel scriptPanel;
	private JCheckBox includeExcludedBox;
	private JButton saveQuerySettingsButton;

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
		sessionSelector.setPreferredSize(new Dimension(350, 0));
		final JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(new JScrollPane(sessionSelector), BorderLayout.CENTER);
		includeExcludedBox = new JCheckBox("Include excluded records");
		leftPanel.add(includeExcludedBox, BorderLayout.SOUTH);
		TitledPanel sessionsPanel = new TitledPanel("Select Sessions", leftPanel);
		
		scriptPanel = new ScriptPanel(queryScript);
		
		final PhonUIAction saveSettingsAct = new PhonUIAction(this, "onSaveQuerySettings");
		saveSettingsAct.putValue(PhonUIAction.NAME, "Save query parameters");
		saveSettingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save current query parameters");
		final ImageIcon saveIcn = IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		saveSettingsAct.putValue(PhonUIAction.SMALL_ICON, saveIcn);
		saveQuerySettingsButton = new JButton(saveSettingsAct);
		
		final JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(scriptPanel, BorderLayout.CENTER);
		rightPanel.add(ButtonBarBuilder.buildOkBar(saveQuerySettingsButton), BorderLayout.SOUTH);
		
		TitledPanel queryPanel = new TitledPanel("Query Parameters", rightPanel);
		
		splitPane = new JSplitPane();
		splitPane.setLeftComponent(sessionsPanel);
		splitPane.setRightComponent(queryPanel);
		
		retVal.setLayout(new BorderLayout());
		retVal.add(splitPane, BorderLayout.CENTER);
		
		return retVal;
	}
	
	public void onSaveQuerySettings() {
		final SaveQueryDialog dialog = new SaveQueryDialog(this, queryScript);
		dialog.setModal(true);
		
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		
		dialog.setVisible(true);
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
		retVal.setTitle("Report Composer");
		
		reportEditor = new SimpleEditorPanel(
				project,
				new ReportLibrary(), new ReportEditorModelInstantiator(), new ReportNodeInstantiator(),
				(qs) -> new MacroNode(),
				(graph, project) -> new ReportRunner(graph, getCurrentQueryProject(), getCurrentQueryId()) );
		TitledPanel configPane = new TitledPanel("Report Composer", reportEditor);
		
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
		return this.includeExcludedBox.isSelected();
	}
	
	@Override
	public Tuple<String, String> getNoun() {
		return new Tuple<>("Report", "Reports");
	}

	@Override
	public void next() {
		if(getCurrentStep() == queryStep) {
			if(sessionSelector.getSelectedSessions().size() == 0) {
				showMessageDialog("Select Sessions", "Please select at least one session", new String[] {"Ok"});
				return;
			}
			if(!scriptPanel.checkParams()) {
				showMessageDialog("Query Parameters", "Check query parameters.", new String[] {"Ok"});
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
			
			final WizardExtension ext = graph.getExtension(WizardExtension.class);
			if(ext != null)
				ext.setWizardTitle(queryScript.getExtension(QueryName.class).getName());
			
			final Processor processor = new Processor(graph);
			processor.getContext().put("_project", getCurrentQueryProject());
			processor.getContext().put("_queryId", getCurrentQueryId());
			processor.getContext().put("_selectedSessions", sessionSelector.getSelectedSessions());
			
			setProcessor(processor);
		}
		super.gotoStep(stepIdx);
	}
	
}
