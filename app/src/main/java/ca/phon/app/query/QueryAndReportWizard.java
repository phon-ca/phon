package ca.phon.app.query;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.xml.bind.UnmarshalException;
import javax.xml.stream.XMLStreamException;

import org.jdesktop.swingx.HorizontalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.OpgraphIO;
import ca.phon.app.opgraph.editor.SimpleEditorExtension;
import ca.phon.app.opgraph.editor.SimpleEditorPanel;
import ca.phon.app.opgraph.nodes.ReportNodeInstantiator;
import ca.phon.app.opgraph.report.ReportEditorModelInstantiator;
import ca.phon.app.opgraph.report.ReportLibrary;
import ca.phon.app.opgraph.report.ReportRunner;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.project.git.ProjectGitController;
import ca.phon.app.session.SessionSelector;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.project.Project;
import ca.phon.query.script.LazyQueryScript;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.script.PhonScriptException;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.ActionTabComponent;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.PrefHelper;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask.TaskStatus;

public class QueryAndReportWizard extends NodeWizard {
	
	private static final long serialVersionUID = -3028026575633555881L;

	public final static String PREVIOUS_QUERY_PARAMETERS_FOLDER = QueryAndReportWizard.class.getName() + ".prevQueryParametersFolder";
	public final static String DEFAULT_QUERY_PARAMETERS_FOLDER = 
			PrefHelper.getUserDataFolder() + File.separator + "previous_query_parameters";
	private String prevQueryParametersFolder = 
			PrefHelper.get(PREVIOUS_QUERY_PARAMETERS_FOLDER, DEFAULT_QUERY_PARAMETERS_FOLDER);
	
	public final static String PREVIOUS_REPORT_FOLDER = QueryAndReportWizard.class.getName() + ".prevReportFolder";
	public final static String DEFAULT_REPORT_FOLDER = 
			PrefHelper.getUserDataFolder() + File.separator + "previous_query_reports";
	private String prevQueryReportFolder = 
			PrefHelper.get(PREVIOUS_REPORT_FOLDER, DEFAULT_REPORT_FOLDER);
	
	private WizardStep queryStep;
	private JSplitPane splitPane;
	private SessionSelector sessionSelector;
	private ScriptPanel scriptPanel;
	private JCheckBox includeExcludedBox;
	private JButton saveQuerySettingsButton;
	private JButton resetQueryButton;

	private WizardStep queryResultsStep;
	private JTabbedPane queryResultsPane;
	
	private WizardStep reportEditorStep;
	private SimpleEditorPanel reportEditor;
	
	private Project project;
	private QueryScript queryScript;

	public QueryAndReportWizard(Project project, QueryScript queryScript) {
		// init with 'dummy' processor and graph as these will be created 0during the wizard
		super("Query : " + queryScript.getExtension(QueryName.class).getName(), new Processor(new OpGraph()), new OpGraph());
		
		this.project = project;
		putExtension(Project.class, project);
		
		this.queryScript = loadPreviousQueryParameters(queryScript);

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
	
	@Override
	public void close() {
		if(getCurrentStep() == queryResultsStep) {
			final QueryRunnerPanel runnerPanel = getCurrentQueryRunner();
			if(runnerPanel != null && runnerPanel.isRunning()) {
				int retVal = showMessageDialog("Cancel query?", "Stop query and close window?", MessageDialogProperties.okCancelOptions);
				if(retVal == 1) return;
			}
		}
		super.close();
	}
	
	private WizardStep createQueryStep() {
		WizardStep retVal = new WizardStep();
		QueryName qn = queryScript.getExtension(QueryName.class);
		retVal.setTitle("Query : " + qn.getName());
		
		sessionSelector = new SessionSelector(this.project);
		final JScrollPane sessionScroller = new JScrollPane(sessionSelector);
		sessionScroller.setPreferredSize(new Dimension(350, 0));
		includeExcludedBox = new JCheckBox("Include excluded records");
		includeExcludedBox.setOpaque(false);
		includeExcludedBox.setForeground(Color.WHITE);
		
		TitledPanel sessionsPanel = new TitledPanel("Select Sessions");
		sessionsPanel.getContentContainer().setLayout(new BorderLayout());
		sessionsPanel.getContentContainer().add(sessionScroller, BorderLayout.CENTER);
		sessionsPanel.setRightDecoration(includeExcludedBox);
		
		scriptPanel = new ScriptPanel(queryScript);
		final PhonUIAction saveSettingsAct = new PhonUIAction(this, "onSaveQuerySettings");
		saveSettingsAct.putValue(PhonUIAction.NAME, "Save query");
		saveSettingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save current query");
		final ImageIcon saveIcn = IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		saveSettingsAct.putValue(PhonUIAction.SMALL_ICON, saveIcn);
		saveQuerySettingsButton = new JButton(saveSettingsAct);
		
		TitledPanel queryPanel = new TitledPanel("Query");
		queryPanel.getContentContainer().setLayout(new BorderLayout());
		queryPanel.getContentContainer().add(scriptPanel, BorderLayout.CENTER);
		
		final PhonUIAction resetQueryAct = new PhonUIAction(this, "resetQueryParameters", queryPanel);
		resetQueryAct.putValue(PhonUIAction.NAME, "Reset query");
		resetQueryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset query parameters to default");
		resetQueryButton = new JButton(resetQueryAct);
		
		final JComponent buttonBar = new JPanel(new HorizontalLayout());
		buttonBar.add(resetQueryButton);
		buttonBar.add(saveQuerySettingsButton);
		buttonBar.setOpaque(false);
		queryPanel.setRightDecoration(buttonBar);
		
		splitPane = new JSplitPane();
		splitPane.setLeftComponent(sessionsPanel);
		splitPane.setRightComponent(queryPanel);
		
		retVal.setLayout(new BorderLayout());
		retVal.add(splitPane, BorderLayout.CENTER);
		
		return retVal;
	}
	
	public void resetQueryParameters(PhonActionEvent pae) {
		final TitledPanel parent = (TitledPanel)pae.getData();
		final ScriptPanel oldScriptPanel = this.scriptPanel;
		
		queryScript.resetContext();
		
		ScriptPanel newScriptPanel = new ScriptPanel(queryScript);
		parent.getContentContainer().remove(oldScriptPanel);
		parent.getContentContainer().add(newScriptPanel, BorderLayout.CENTER);
		parent.revalidate();
		
		this.scriptPanel = newScriptPanel;
	}
	
	private QueryScript loadPreviousQueryParameters(QueryScript queryScript) {
		final QueryName qn = queryScript.getExtension(QueryName.class);
		final File previousParametersFile = new File(prevQueryParametersFolder, qn.getName() + ".xml");
		
		if(previousParametersFile.exists()) {
			try {
				QueryScript qs = new QueryScript(previousParametersFile.toURI().toURL());
			
				qs.getContext().getEvaluatedScope();
				// scripts should be exactly the same, if not bail
				if(!qs.getScript().equals(queryScript.getScript())) {
					throw new IOException("Issue loading previous query parameters; source and previous scripts do not match");
				}
				
				return qs;
			} catch (IOException | PhonScriptException e) {
				// invalid parameters file - delete
				boolean deleted = previousParametersFile.delete();
				if(!deleted) {
					LogUtil.severe("Could not delete query parameters file: " + previousParametersFile.getAbsolutePath());
					Toolkit.getDefaultToolkit().beep();
				}
				LogUtil.severe(e);
			}
		}
		return queryScript;
	}
	
	private void savePreviousQueryParameters() {
		final QueryName qn = queryScript.getExtension(QueryName.class);
		final File folder = new File(prevQueryParametersFolder);
		final File previousParametersFile = new File(prevQueryParametersFolder, qn.getName() + ".xml");
		
		if(!folder.exists()) {
			folder.mkdirs();
		}
		if(!previousParametersFile.getParentFile().exists()) {
			previousParametersFile.getParentFile().mkdirs();
		}
		
		try {
			QueryScriptLibrary.saveScriptToFile(queryScript, previousParametersFile.getAbsolutePath());
		} catch (IOException e) {
			LogUtil.severe(e);
		}
	}
	
	/**
	 * Load either the previous query report, or the default.
	 * 
	 */
	private void loadInitialQueryReport() {
		final QueryName qn = queryScript.getExtension(QueryName.class);
		final File folder = new File(prevQueryReportFolder);
		final File prevReportFile = new File(folder, qn.getName() + ".xml");
		
		final URL defaultReportURL = getClass().getResource("default_report.xml");
		
		if(prevReportFile.exists()) {
			try {
				final Consumer<SimpleEditorPanel.DocumentError> errHandler = (err) -> {
					try {
						if(err.getDocument().equals(prevReportFile.toURI().toURL()))
							reportEditor.addDocument(defaultReportURL);
					} catch (MalformedURLException e) {}
				};
				reportEditor.addDocumentErrorListener( errHandler );
				reportEditor.addDocument(prevReportFile);
			} catch (IOException e) {
				LogUtil.warning(e);
				reportEditor.addDocument(defaultReportURL);
			}
		} else {
			reportEditor.addDocument(defaultReportURL);
		}
		
		reportEditor.getModel().getDocument().markAsUnmodified();
	}
	
	private void savePreviousQueryReport() {
		final QueryName qn = queryScript.getExtension(QueryName.class);
		final File folder = new File(prevQueryReportFolder);
		final File prevReportFile = new File(folder, qn.getName() + ".xml");
		
		if(!folder.exists()) {
			folder.mkdirs();
		}
		if(!prevReportFile.getParentFile().exists()) {
			prevReportFile.getParentFile().mkdirs();
		}

		// make sure editor extension is up2d8
		reportEditor.getGraph().putExtension(SimpleEditorExtension.class, new SimpleEditorExtension(reportEditor.getMacroNodes()));
		
		try {
			OpgraphIO.write(reportEditor.getGraph(), prevReportFile);
		} catch (IOException e) {
			LogUtil.severe(e);
		}
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
		// toolbar customizations
		reportEditor.getRunButton().setVisible(false);
		reportEditor.getListTopPanel().add(super.globalOptionsPanel);
		
		retVal.setLayout(new BorderLayout());
		retVal.add(reportEditor, BorderLayout.CENTER);
		
		loadInitialQueryReport();
		
		return retVal;
	}
	
	private QueryRunnerPanel getCurrentQueryRunner() {
		if(queryResultsPane == null || queryResultsPane.getTabCount() == 0) return null;
		return (QueryRunnerPanel)queryResultsPane.getComponentAt(queryResultsPane.getSelectedIndex());
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
		
		// save previous query paramters on successful query
		runnerPanel.addPropertyChangeListener("numberComplete", (e) -> {
			if(((Integer)e.getNewValue()) == sessionSelector.getSelectedSessions().size()) {
				savePreviousQueryParameters();
			}
		});
		
		final ImageIcon closeIcon = IconManager.getInstance().getIcon("actions/process-stop", IconSize.XSMALL);
		final PhonUIAction closeAction = new PhonUIAction(this, "discardResults", runnerPanel);
		closeAction.putValue(PhonUIAction.SMALL_ICON, closeIcon);
		closeAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Discard results and close tab");
		final ActionTabComponent atc = new ActionTabComponent(queryResultsPane, closeAction);
		final int tabIdx = queryResultsPane.getTabCount()-1;
		queryResultsPane.setTabComponentAt(tabIdx, atc);
		
		runnerPanel.startQuery();
	}
	
	public void discardResults(QueryRunnerPanel panel) {
		final int idx = queryResultsPane.indexOfComponent(panel);
		if(idx >= 0) {
			if(panel.isRunning()) {
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setParentWindow(this);
				props.setRunAsync(true);
				props.setOptions(MessageDialogProperties.okCancelOptions);
				props.setTitle("Cancel Query");
				props.setMessage("Stop query?");
				props.setHeader("Cancel Query");
				props.setListener( (e) -> {
					if(e.getDialogResult() == 0) {
						if(idx >= 0 && idx < queryResultsPane.getTabCount()) {
							panel.stopQuery();
						}
					}
				});
				NativeDialogs.showMessageDialog(props);
			} else {
				if(idx >= 0 && idx < queryResultsPane.getTabCount()) {
					queryResultsPane.removeTabAt(idx);
				}
			}
		}
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
				showMessageDialog("Select Sessions", "Please select at least one session", MessageDialogProperties.okOptions);
				return;
			}
			if(!scriptPanel.checkParams()) {
				showMessageDialog("Query Parameters", "Check query parameters.", MessageDialogProperties.okOptions);
				return;
			}
		} else if(getCurrentStep() == queryResultsStep) {
			final QueryRunnerPanel runnerPanel = getCurrentQueryRunner();
			
			if(runnerPanel == null) {
				showMessageDialog("Results", "No results", MessageDialogProperties.okOptions);
				return;
			} else {
				if(runnerPanel.getTaskStatus() != TaskStatus.FINISHED) {
					int retVal = showMessageDialog("Results", "Query did not complete, continue anyway?", MessageDialogProperties.yesNoOptions);
					if(retVal == 1) {
						return;
					}
				}
			}
		} else if(getCurrentStep() == reportEditorStep) {
			savePreviousQueryReport();
		}
		super.next();
	}
	
	@Override
	public void gotoStep(int stepIdx) {
		if(stepIdx == super.getStepIndex(queryResultsStep) && getCurrentStep() == queryStep) {
			// create a new query runner panel
			executeQuery();
		} else if(getCurrentStep() == queryResultsStep) {
			QueryRunnerPanel queryRunner = getCurrentQueryRunner();
			if(queryRunner != null && queryRunner.isRunning()) {
				showMessageDialog("Results", "Please wait for query to complete", MessageDialogProperties.okOptions);
				return;
			}
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
