package ca.phon.app.query;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.codec.digest.DigestUtils;
import org.jdesktop.swingx.HorizontalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.menu.query.QueryMenuListener;
import ca.phon.app.opgraph.OpgraphIO;
import ca.phon.app.opgraph.editor.SimpleEditorExtension;
import ca.phon.app.opgraph.editor.SimpleEditorPanel;
import ca.phon.app.opgraph.nodes.ReportNodeInstantiator;
import ca.phon.app.opgraph.report.ReportEditorModelInstantiator;
import ca.phon.app.opgraph.report.ReportLibrary;
import ca.phon.app.opgraph.report.ReportRunner;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.session.SessionSelector;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultSetManager;
import ca.phon.query.db.xml.XMLResultSetManager;
import ca.phon.query.history.ObjectFactory;
import ca.phon.query.history.ParamType;
import ca.phon.query.history.ParamsType;
import ca.phon.query.history.ProjectInfoType;
import ca.phon.query.history.QueryHistoryManager;
import ca.phon.query.history.QueryHistoryType;
import ca.phon.query.history.QueryInfoType;
import ca.phon.query.history.SessionInfoType;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.session.SessionPath;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
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
	private JPanel queryLeftPanel;
	private CardLayout queryLeftPanelLayout;
	private TitledPanel sessionSelectorPanel;
	private CardLayout queryResultsLayout;
	private TitledPanel queryResultsPanel;
	private TitledPanel queryRightPanel;
	private JSplitPane splitPane;
	private SessionSelector sessionSelector;
	private ScriptPanel scriptPanel;
	private JCheckBox includeExcludedBox;
	private JButton showResultsButton;
	private JButton showSessionsButton;
	private JButton saveQuerySettingsButton;
	private JButton resetQueryButton;
	private JButton runQueryButton;
	
	private JComboBox<String> queryRunnerBox;
	private JButton discardResultsButton;
	private DefaultComboBoxModel<String> queryRunnerComboBoxModel;
	private int queryIndex = 0;
	private Map<String, QueryRunnerPanel> queryRunners = new LinkedHashMap<>();
	
	private WizardStep reportEditorStep;
	private SimpleEditorPanel reportEditor;
	
	private Project project;
	private QueryScript queryScript;

	public QueryAndReportWizard(Project project, QueryScript queryScript) {
		// init with 'dummy' processor and graph as these will be created 0during the wizard
		super("Query : " + queryScript.getExtension(QueryName.class).getName(), new Processor(new OpGraph()), new OpGraph());
		
		this.project = project;
		putExtension(Project.class, project);
		
		this.queryScript = queryScript;
		loadPreviousQueryParameters(queryScript);
		
		// add query steps
		init();
		
		gotoStep(0);
	}
	
	private void init() {
		queryStep = createQueryStep();
		addWizardStep(0, queryStep);
		
		reportEditorStep = createReportConfigStep();
		addWizardStep(1, reportEditorStep);
		
		queryStep.setNextStep(1);
		
		reportEditorStep.setPrevStep(0);
		reportEditorStep.setNextStep(2);
		
		reportDataStep.setPrevStep(1);
		
		setWindowName("Query : " + queryScript.getExtension(QueryName.class).getName() + " (No results)");
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		
		// find query menu
		for(int i = 0; i < menuBar.getMenuCount(); i++) {
			final JMenu menu = menuBar.getMenu(i);
			
			if(menu.getText().equals("Query")) {
				// add listener to menu, remove current
				for(MenuListener listener:menu.getMenuListeners()) {
					if(listener instanceof QueryMenuListener)
						menu.removeMenuListener(listener);
				}
				
				QueryMenuListener queryMenuListener = new QueryMenuListener() {
					
					@Override
					public void menuSelected(MenuEvent e) {
						super.menuSelected(e);
						
						final PhonUIAction runAct = new PhonUIAction(QueryAndReportWizard.this, "executeQuery");
						runAct.putValue(PhonUIAction.NAME, "Run query");
						runAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
						runAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
						
						final PhonUIAction stopAct = new PhonUIAction(QueryAndReportWizard.this, "onStopQuery");
						stopAct.putValue(PhonUIAction.NAME, "Stop query");
						stopAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
						final JMenuItem stopItem = new JMenuItem(stopAct);
						boolean stopEnabled = (getCurrentQueryRunner() != null && getCurrentQueryRunner().isRunning());
						stopItem.setEnabled(stopEnabled);
						
						final PhonUIAction resetQueryAct = new PhonUIAction(this, "resetQueryParameters");
						resetQueryAct.putValue(PhonUIAction.NAME, "Reset query");
						resetQueryAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("misc/parameters-black", IconSize.SMALL));
						resetQueryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset query parameters to default");
						
						final PhonUIAction saveSettingsAct = new PhonUIAction(QueryAndReportWizard.this, "onSaveQuerySettings");
						saveSettingsAct.putValue(PhonUIAction.NAME, "Save query");
						saveSettingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save current query");
						final ImageIcon saveIcn = IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
						saveSettingsAct.putValue(PhonUIAction.SMALL_ICON, saveIcn);
						
						final PhonUIAction discardResultsAct = new PhonUIAction(QueryAndReportWizard.this, "discardResults");
						discardResultsAct.putValue(PhonUIAction.NAME, "Discard results");
						discardResultsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Discard current result set");
						discardResultsAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/process-stop", IconSize.SMALL));
						final JMenuItem discardResultsItem = new JMenuItem(discardResultsAct);
						boolean discardEnabled = (getCurrentQueryRunner() != null && !getCurrentQueryRunner().isRunning());
						discardResultsItem.setEnabled(discardEnabled);
						
						final JMenu queryResultsMenu = new JMenu("Results");
						queryResultsMenu.addMenuListener(new MenuListener() {
							
							@Override
							public void menuSelected(MenuEvent e) {
								queryResultsMenu.removeAll();
								for(int i = 0; i < queryRunnerComboBoxModel.getSize(); i++) {
									final String queryName = queryRunnerComboBoxModel.getElementAt(i);
									final PhonUIAction selectResultsAct = new PhonUIAction(QueryAndReportWizard.this, "selectResults", queryName);
									selectResultsAct.putValue(PhonUIAction.NAME, queryName);
									selectResultsAct.putValue(PhonUIAction.SELECTED_KEY, i == queryRunnerBox.getSelectedIndex());
									selectResultsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Select result set for " + queryName);
									
									final JCheckBoxMenuItem item = new JCheckBoxMenuItem(selectResultsAct);
									queryResultsMenu.add(item);
								}
								
								queryResultsMenu.addSeparator();
								
								final PhonUIAction discardAllAction = new PhonUIAction(QueryAndReportWizard.this, "discardAllResults");
								discardAllAction.putValue(PhonUIAction.NAME, "Discard all results");
								discardAllAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Discard all query results");
								discardAllAction.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/process-stop", IconSize.SMALL));
							
								queryResultsMenu.add(new JMenuItem(discardAllAction));
							}
							
							@Override
							public void menuDeselected(MenuEvent e) {
							}
							
							@Override
							public void menuCanceled(MenuEvent e) {
							}
						});
						
						menu.addSeparator();
						menu.add(new JMenuItem(saveSettingsAct));
						menu.add(new JMenuItem(resetQueryAct));
						menu.add(new JMenuItem(runAct));
						menu.add(stopItem);
						menu.addSeparator();
						menu.add(discardResultsItem);
						menu.add(queryResultsMenu);
					}
					
					@Override
					public void menuDeselected(MenuEvent e) {
						
					}
					
					@Override
					public void menuCanceled(MenuEvent e) {
						
					}
				};
				menu.addMenuListener(queryMenuListener);
				
				final MenuEvent me = new MenuEvent(menu);
				queryMenuListener.menuSelected(me);
								
			}
		}
	}
	
	@Override
	public void close() {
		if(getCurrentStep() == queryStep) {
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
		sessionSelector.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					final TreePath tp = sessionSelector.getPathForLocation(e.getX(), e.getY());
					
					if(tp.getLastPathComponent() instanceof ResultSetTreeNode) {
						// open results
						ResultSetTreeNode rsTreeNode = (ResultSetTreeNode)tp.getLastPathComponent();
						openQueryResults(rsTreeNode);
					}
				}
			}
			
		});
		
		final JScrollPane sessionScroller = new JScrollPane(sessionSelector);
		sessionScroller.setPreferredSize(new Dimension(350, 0));
		includeExcludedBox = new JCheckBox("Include excluded records");
		includeExcludedBox.setOpaque(false);
		includeExcludedBox.setForeground(Color.WHITE);
		
		sessionSelectorPanel = new TitledPanel("Select Sessions");
		sessionSelectorPanel.getContentContainer().setLayout(new BorderLayout());
		sessionSelectorPanel.getContentContainer().add(sessionScroller, BorderLayout.CENTER);

		final PhonUIAction showResultsAct = new PhonUIAction(this, "showResults");
		showResultsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show query results");
		showResultsAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("misc/right-arrow-white", IconSize.SMALL));
		showResultsButton = new JButton(showResultsAct);
		showResultsButton.setBorderPainted(false);
		
		final PhonUIAction showSessionSelectorAct = new PhonUIAction(this, "showSessionSelector");
		showSessionSelectorAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show session selector");
		showSessionSelectorAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("misc/left-arrow-white", IconSize.SMALL));
		showSessionsButton = new JButton(showSessionSelectorAct);
		showSessionsButton.setBorderPainted(false);
		
		final JPanel sessionSelectorRightDecoration = new JPanel(new HorizontalLayout());
		sessionSelectorRightDecoration.setOpaque(false);
		sessionSelectorRightDecoration.add(includeExcludedBox);
		sessionSelectorRightDecoration.add(showResultsButton);
		sessionSelectorPanel.setRightDecoration(sessionSelectorRightDecoration);
		
		queryRunnerComboBoxModel = new DefaultComboBoxModel<>();
		queryRunnerBox = new JComboBox<>(queryRunnerComboBoxModel);
		queryRunnerBox.addItemListener( (e) -> {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				final Object selected = queryRunnerBox.getSelectedItem();
				if(selected != null) {
					queryResultsLayout.show(queryResultsPanel.getContentContainer(), selected.toString());
					setWindowName("Query : " + queryScript.getExtension(QueryName.class).getName() + " (" + selected.toString() + ")");
					
					discardResultsButton.setEnabled(true);
					final QueryRunnerPanel runnerPanel = getCurrentQueryRunner();
					if(runnerPanel != null) {
						if(runnerPanel.isRunning()) {
							discardResultsButton.setIcon(IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
						} else {
							discardResultsButton.setIcon(IconManager.getInstance().getIcon("actions/process-stop", IconSize.SMALL));
						}
					}
				} else {
					discardResultsButton.setEnabled(false);
				}
			}
		});
		
		final PhonUIAction discardResultsAct = new PhonUIAction(this, "discardResults");
		discardResultsAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/process-stop", IconSize.SMALL));
		discardResultsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Discard results");
		discardResultsButton = new JButton(discardResultsAct);
		discardResultsButton.setBorderPainted(false);
		discardResultsButton.setEnabled(false);
		
		final JPanel queryResultsRightDecoration = new JPanel(new HorizontalLayout());
		queryResultsRightDecoration.setOpaque(false);
		queryResultsRightDecoration.add(queryRunnerBox);
		queryResultsRightDecoration.add(discardResultsButton);
		
		queryResultsLayout = new CardLayout();
		queryResultsPanel = new TitledPanel("Query Results");
		queryResultsPanel.getContentContainer().setLayout(queryResultsLayout);
		queryResultsPanel.setLeftDecoration(showSessionsButton);
		queryResultsPanel.setRightDecoration(queryResultsRightDecoration);
		
		final JPanel noResultsPanel = new JPanel(new BorderLayout());
		final JLabel noResultsLabel = new JLabel("No results");
		noResultsLabel.setEnabled(false);
		noResultsLabel.setFont(noResultsLabel.getFont().deriveFont(Font.ITALIC));
		noResultsLabel.setHorizontalAlignment(JLabel.CENTER);
		noResultsPanel.add(noResultsLabel, BorderLayout.CENTER);
		queryResultsPanel.getContentContainer().add(noResultsPanel, "noResults");
		
		queryLeftPanelLayout = new CardLayout();
		queryLeftPanel = new JPanel(queryLeftPanelLayout);
		queryLeftPanel.add(sessionSelectorPanel, "sessionSelector");
		queryLeftPanel.add(queryResultsPanel, "queryResults");
		queryLeftPanelLayout.show(queryLeftPanel, "sessionSelector"); 
				
		scriptPanel = new ScriptPanel(queryScript);
		final PhonUIAction saveSettingsAct = new PhonUIAction(this, "onSaveQuerySettings");
		saveSettingsAct.putValue(PhonUIAction.NAME, "Save query");
		saveSettingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save current query");
		final ImageIcon saveIcn = IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		saveSettingsAct.putValue(PhonUIAction.SMALL_ICON, saveIcn);
		saveQuerySettingsButton = new JButton(saveSettingsAct);
		
		queryRightPanel = new TitledPanel("Query");
		queryRightPanel.getContentContainer().setLayout(new BorderLayout());
		queryRightPanel.getContentContainer().add(scriptPanel, BorderLayout.CENTER);
		
		final PhonUIAction resetQueryAct = new PhonUIAction(this, "resetQueryParameters", queryRightPanel);
		resetQueryAct.putValue(PhonUIAction.NAME, "Reset query");
		resetQueryAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("misc/parameters-black", IconSize.SMALL));
		resetQueryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset query parameters to default");
		resetQueryButton = new JButton(resetQueryAct);
		
		final PhonUIAction runAct = new PhonUIAction(QueryAndReportWizard.this, "executeQuery");
		runAct.putValue(PhonUIAction.NAME, "Run query");
		runAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		runAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		runQueryButton = new JButton(runAct);
		
		final JComponent buttonBar = new JPanel(new HorizontalLayout());
		buttonBar.add(resetQueryButton);
		buttonBar.add(saveQuerySettingsButton);
		buttonBar.add(runQueryButton);
		buttonBar.setOpaque(false);
		queryRightPanel.setRightDecoration(buttonBar);
		
		splitPane = new JSplitPane();
		splitPane.setLeftComponent(queryLeftPanel);
		splitPane.setRightComponent(queryRightPanel);
		
		retVal.setLayout(new BorderLayout());
		retVal.add(splitPane, BorderLayout.CENTER);
		
		return retVal;
	}
	
	public void showResults() {
		queryLeftPanelLayout.show(queryLeftPanel, "queryResults");
	}
	
	public void showSessionSelector() {
		queryLeftPanelLayout.show(queryLeftPanel, "sessionSelector");
	}
	
	public void resetQueryParameters() {
		queryScript.resetContext();
		
		updateQueryForm();
	}
	
	private void updateQueryForm() {
		// don't update before init()
		if(this.scriptPanel == null) return;
		
		final TitledPanel parent = queryRightPanel;
		final ScriptPanel oldScriptPanel = this.scriptPanel;
		ScriptPanel newScriptPanel = new ScriptPanel(queryScript);
		parent.getContentContainer().remove(oldScriptPanel);
		parent.getContentContainer().add(newScriptPanel, BorderLayout.CENTER);
		parent.revalidate();
		
		this.scriptPanel = newScriptPanel;
	}
	
	private void loadPreviousQueryParameters(QueryScript queryScript) {
		final QueryName qn = queryScript.getExtension(QueryName.class);
		
		final QueryHistoryManager manager = new QueryHistoryManager();
		final QueryHistoryType history = manager.getQueryHistory(qn.getName());
		
		if(history.getQuery().size() > 0) {
			final QueryInfoType queryInfo = history.getQuery().get(0);
			loadParamsFromQueryInfo(queryInfo);
		}
	}
	
	private void loadParamsFromQueryInfo(QueryInfoType queryInfo) {
		this.queryScript.resetContext();
		
		final Map<String, Object> paramMap = new LinkedHashMap<>();
		final ParamsType previousParams = queryInfo.getParams();
		for(ParamType paramType:previousParams.getParam()) {
			paramMap.put(paramType.getId(), paramType.getValue());
		}
		
		try {
			ScriptParameters scriptParams = this.queryScript.getContext().getScriptParameters(
					this.queryScript.getContext().getEvaluatedScope());
			scriptParams.loadFromMap(paramMap);
		} catch (PhonScriptException e) {
			LogUtil.severe(e);
			Toolkit.getDefaultToolkit().beep();
		}
		
		updateQueryForm();
	}
	
	private void addToQueryHistory(Project project, Query query) throws IOException {
		final QueryName qn = queryScript.getExtension(QueryName.class);
		
		final ObjectFactory factory = new ObjectFactory();
		final QueryHistoryManager manager = new QueryHistoryManager();
		
		try {
			final QueryHistoryType queryHistory = manager.getQueryHistory(qn.getName());
			
			final QueryInfoType queryInfo = factory.createQueryInfoType();
			final ZonedDateTime date = ZonedDateTime.now(ZoneId.systemDefault());
			GregorianCalendar gcal = GregorianCalendar.from(date);
			XMLGregorianCalendar xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);		
			queryInfo.setDate(xcal);
			queryInfo.setHash( DigestUtils.sha256Hex(queryScript.getScript()) );
			
			final ScriptParameters scriptParams = queryScript.getContext().getScriptParameters(queryScript.getContext().getEvaluatedScope());
			final ParamsType params = factory.createParamsType();
			queryInfo.setParams(params);
			for(ScriptParam param:scriptParams) {	
				if(param.hasChanged()) {
					for(String paramId:param.getParamIds()) {
						final ParamType paramType = factory.createParamType();
						paramType.setId(paramId);
						paramType.setValue(param.getValue(paramId).toString());
						params.getParam().add(paramType);
					}
				}
			}
			
			final ProjectInfoType projectInfo = factory.createProjectInfoType();
			queryInfo.setProject(projectInfo);
	
			final ResultSetManager rsManager = new XMLResultSetManager();
			List<ResultSet> resultSets = rsManager.getResultSetsForQuery(project, query);
			
			for(ResultSet rs:resultSets) {
				final SessionInfoType sessionInfoType = factory.createSessionInfoType();
				sessionInfoType.setName(rs.getSessionPath());
				sessionInfoType.setResults(rs.numberOfResults(true));
				projectInfo.getSession().add(sessionInfoType);
			}
			
			manager.addQueryInfo(queryHistory, queryInfo);
			manager.saveQueryHistory(queryHistory, qn.getName());
		} catch (DatatypeConfigurationException | PhonScriptException e) {
			throw new IOException(e);
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
		if(queryRunnerBox == null || queryRunnerComboBoxModel.getSize() == 0) return null;
		return queryRunners.get(queryRunnerBox.getSelectedItem());
	}
	
	private String getCurrentQueryId() {
		final QueryRunnerPanel runnerPanel = getCurrentQueryRunner();
		if(runnerPanel != null)
			return runnerPanel.getQuery().getUUID().toString();
		return new String();
	}
	
	private Project getCurrentQueryProject() {
		final QueryRunnerPanel runnerPanel = getCurrentQueryRunner();
		if(runnerPanel != null)
			return (runnerPanel.isSaved() ? runnerPanel.getProject() : runnerPanel.getTempProject());
		else
			return null;
	}
	
	public void onStopQuery() {
		final QueryRunnerPanel currentRunner = getCurrentQueryRunner();
		if(currentRunner != null && currentRunner.isRunning())
			currentRunner.stopQuery();
	}
	
	public void executeQuery() {
		if(getCurrentStep() != queryStep)
			gotoStep(getStepIndex(queryStep));
		
		final QueryRunnerPanel currentRunner = getCurrentQueryRunner();
		if(currentRunner != null && currentRunner.isRunning()) {
			final String[] options = { "Ok", "Stop current query" };
			int retVal = showMessageDialog("Query", "Please wait for current query to complete.", options);
			if(retVal == 1) {
				currentRunner.stopQuery();
			}
			return;
		}
		
		if(sessionSelector.getSelectedSessions().size() == 0) {
			showSessionSelector();
			showMessageDialog("Select Sessions", "Please select at least one session", MessageDialogProperties.okOptions);
			return;
		}
		if(!scriptPanel.checkParams()) {
			showMessageDialog("Query Parameters", "Check query parameters.", MessageDialogProperties.okOptions);
			return;
		}
		
		final QueryRunnerPanel runnerPanel = 
				new QueryRunnerPanel(project, queryScript, sessionSelector.getSelectedSessions(), isIncludeExcluded());
		
		final String queryName = "Query " + (++queryIndex);
		queryResultsPanel.getContentContainer().add(runnerPanel, queryName);
		queryRunners.put(queryName, runnerPanel);
		queryRunnerComboBoxModel.addElement(queryName);
		queryRunnerBox.setSelectedItem(queryName);
		
		runnerPanel.addPropertyChangeListener("taskStatus", (e) -> {
			if(e.getNewValue() == TaskStatus.FINISHED || e.getNewValue() == TaskStatus.TERMINATED) {
				SwingUtilities.invokeLater( () -> addNodesToSessionSelector(queryName, runnerPanel) );
				try {
					addToQueryHistory(runnerPanel.getTempProject(), runnerPanel.getQuery());
				} catch (IOException e1) {
					LogUtil.severe(e1);
					Toolkit.getDefaultToolkit().beep();
				}
			}
			
			if(e.getNewValue() == TaskStatus.RUNNING) {
				discardResultsButton.setIcon(IconManager.getInstance().getIcon("actions/media-playback-stop", IconSize.SMALL));
			} else {
				discardResultsButton.setIcon(IconManager.getInstance().getIcon("actions/process-stop", IconSize.SMALL));
			}
		});
		
		final ImageIcon closeIcon = IconManager.getInstance().getIcon("actions/process-stop", IconSize.XSMALL);
		final PhonUIAction closeAction = new PhonUIAction(this, "discardResults", queryName);
		closeAction.putValue(PhonUIAction.SMALL_ICON, closeIcon);
		closeAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Discard results and close tab");
		
		showResults();
		runnerPanel.startQuery();
		discardResultsButton.setEnabled(true);
	}
	
	private void addNodesToSessionSelector(String queryName, QueryRunnerPanel runnerPanel) {
		final QueryManager qm = QueryManager.getSharedInstance();
		final ResultSetManager rsManager = qm.createResultSetManager();
		final Query q = runnerPanel.getQuery();
		
		final List<ResultSet> resultSets = rsManager.getResultSetsForQuery(runnerPanel.getTempProject(), q);
		for(ResultSet rs:resultSets) {
			final SessionPath sp = new SessionPath(rs.getSessionPath());
			final TreePath treePath = sessionSelector.sessionPathToTreePath(sp);
			
			final TristateCheckBoxTreeNode sessionNode = (TristateCheckBoxTreeNode)treePath.getLastPathComponent();
			
			final ResultSetTreeNode resultSetNode = new ResultSetTreeNode(queryName, rs);
			sessionNode.add(resultSetNode);
			
			sessionSelector.getCheckboxTreeModel().nodeStructureChanged(sessionNode);
			sessionSelector.expandPath(treePath);
		}
	}
	
	private void removeNodesFromSessionSelector(String queryName, QueryRunnerPanel runnerPanel) {
		final QueryManager qm = QueryManager.getSharedInstance();
		final ResultSetManager rsManager = qm.createResultSetManager();
		final Query q = runnerPanel.getQuery();
		
		final List<ResultSet> resultSets = rsManager.getResultSetsForQuery(runnerPanel.getTempProject(), q);
		for(ResultSet rs:resultSets) {
			final SessionPath sp = new SessionPath(rs.getSessionPath());
			final TreePath treePath = sessionSelector.sessionPathToTreePath(sp);
			
			final TristateCheckBoxTreeNode sessionNode = (TristateCheckBoxTreeNode)treePath.getLastPathComponent();
			
			for(int i = 0; i < sessionNode.getChildCount(); i++) {
				ResultSetTreeNode rsNode = (ResultSetTreeNode)sessionNode.getChildAt(i);
				if(rsNode.queryName.equals(queryName)) {
					sessionNode.remove(i);
					sessionSelector.getCheckboxTreeModel().nodesWereRemoved(sessionNode, new int[] {i}, new Object[] {rsNode});
					break;
				}
			}
			
		}
		
		// clean up results on disk
		try {
			rsManager.deleteQuery(runnerPanel.getProject(), q);
		} catch (IOException e) {
			LogUtil.log(Level.WARNING, e.getLocalizedMessage(), e);
		}
	}
	
	private void openQueryResults(ResultSetTreeNode rsTreeNode) {
		final QueryRunnerPanel runnerPanel = queryRunners.get(rsTreeNode.queryName);
		if(runnerPanel == null) return;
		
		HashMap<String, Object> initInfo = new HashMap<String, Object>();
		initInfo.put("resultset", rsTreeNode.getUserObject());
		initInfo.put("project", project);
		initInfo.put("tempProject", runnerPanel.getTempProject());
		initInfo.put("query", runnerPanel.getQuery());
		
		// open editor first....
		try {
			PluginEntryPointRunner.executePlugin("ResultSetViewer", initInfo);
		} catch (PluginException ex) {
			LogUtil.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
		}
	}
	
	public void selectResults(String queryName) {
		if(queryRunnerBox == null) return;
		queryRunnerBox.setSelectedItem(queryName);
	}
	
	public void discardAllResults() {
		final Set<QueryRunnerPanel> runnerPanelSet = new HashSet<>();
		runnerPanelSet.addAll(this.queryRunners.values());
		
		for(QueryRunnerPanel panel:runnerPanelSet) {
			discardResults(panel);
		}
	}
	
	public void discardResults() {
		if(getCurrentQueryRunner() != null)
			discardResults(getCurrentQueryRunner());
	}
	
	public void discardResults(QueryRunnerPanel panel) {
		final String queryName = queryRunnerBox.getSelectedItem().toString();
		
		if(panel.isRunning()) {
			onStopQuery();
		} else {
			queryRunnerComboBoxModel.removeElement(queryName);
			
			final QueryRunnerPanel queryRunner = queryRunners.remove(queryName);
			queryResultsLayout.removeLayoutComponent(queryRunner);
			
			removeNodesFromSessionSelector(queryName, queryRunner);
			
			if(queryRunnerComboBoxModel.getSize() == 0) {
				queryResultsLayout.show(queryResultsPanel.getContentContainer(), "noResults");
				setWindowName("Query : " + queryScript.getExtension(QueryName.class).getName() + " (No results)");
				
				discardResultsButton.setEnabled(false);
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
			final QueryRunnerPanel runnerPanel = getCurrentQueryRunner();
			
			if(runnerPanel == null) {
				executeQuery();
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
		if(getCurrentStep() == queryStep && !inInit && queryRunnerBox != null && queryRunnerComboBoxModel.getSize() == 0) {
			// create a new query runner panel
			executeQuery();
			return;
		} else if(getCurrentStep() == queryStep && !inInit && queryRunnerBox != null && queryRunnerComboBoxModel.getSize() > 0) {
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
	
	public class ResultSetTreeNode extends DefaultMutableTreeNode {
		
		private String queryName;
		
		public ResultSetTreeNode(String queryName, ResultSet resultSet) {
			super(resultSet);
			this.queryName = queryName;
		}
		
		@Override
		public String toString() {
			return queryName + " - " + ((ResultSet)getUserObject()).size() + " results";
		}
		
	}
	
}
