/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

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

import org.jdesktop.swingx.HorizontalLayout;

import ca.phon.app.log.LogUtil;
import ca.phon.app.menu.query.QueryMenuListener;
import ca.phon.app.opgraph.OpgraphIO;
import ca.phon.app.opgraph.editor.SimpleEditorExtension;
import ca.phon.app.opgraph.editor.SimpleEditorPanel;
import ca.phon.app.opgraph.nodes.ReportNodeInstantiator;
import ca.phon.app.opgraph.nodes.report.NewReportNode;
import ca.phon.app.opgraph.report.ReportEditorModelInstantiator;
import ca.phon.app.opgraph.report.ReportLibrary;
import ca.phon.app.opgraph.report.ReportRunner;
import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.SectionHeaderNode;
import ca.phon.app.opgraph.wizard.NodeWizard;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.query.actions.DeleteAllUnnamedEntriesAction;
import ca.phon.app.query.actions.DeleteQueryHistoryAction;
import ca.phon.app.query.actions.DeleteQueryHistoryEntryAction;
import ca.phon.app.session.SessionSelector;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.ProcessorEvent;
import ca.phon.opgraph.ProcessorListener;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.plugin.PluginException;
import ca.phon.project.Project;
import ca.phon.query.db.Query;
import ca.phon.query.db.QueryManager;
import ca.phon.query.db.ResultSet;
import ca.phon.query.db.ResultSetManager;
import ca.phon.query.history.QueryHistoryManager;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParameters;
import ca.phon.script.params.history.ObjectFactory;
import ca.phon.script.params.history.ParamHistoryType;
import ca.phon.session.SessionPath;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
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
	private JSplitPane splitPane;
	
	private JPanel queryLeftPanel;
	private TitledPanel sessionSelectorPanel;
	private CardLayout queryLeftPanelLayout;
	private SessionSelector sessionSelector;
	
	private TitledPanel queryResultsPanel;
	private CardLayout queryResultsLayout;
	
	private TitledPanel queryPanel;
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

	private QueryHistoryManager queryHistoryManager;
	private QueryHistoryPanel queryHistoryPanel;
	
	private QueryExecutionHistory previousExeuction;
	
	public QueryAndReportWizard(Project project, QueryScript queryScript) {
		// init with 'dummy' processor and graph as these will be created 0during the wizard
		super("Query : " + queryScript.getExtension(QueryName.class).getName(), new Processor(new OpGraph()), new OpGraph());
		
		this.project = project;
		putExtension(Project.class, project);
		
		this.queryScript = queryScript;
		
		try {
			queryHistoryManager = QueryHistoryManager.newInstance(queryScript);
		} catch (IOException e) {
			final ObjectFactory factory = new ObjectFactory();
			final ParamHistoryType paramHistory = factory.createParamHistoryType();
			queryHistoryManager = new QueryHistoryManager(paramHistory);
			if(queryScript.getExtension(QueryName.class) != null)
				queryHistoryManager.getParamHistory().setScript(queryScript.getExtension(QueryName.class).getName());
		}
		// update hash
		queryHistoryManager.getParamHistory().setHash(queryScript.getHashString());
		
		// setup UI
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
						
						int idx = 0;
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
						
						final PhonUIAction resetQueryAct = new PhonUIAction(QueryAndReportWizard.this, "resetQueryParameters");
						resetQueryAct.putValue(PhonUIAction.NAME, "Clear query");
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
						
						final PhonUIAction historyPrevAct = new PhonUIAction(queryHistoryPanel, "goPrevious");
						historyPrevAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-previous", IconSize.SMALL));
						historyPrevAct.putValue(PhonUIAction.NAME, "View previous entry in query history");
						
						final PhonUIAction historyNextAct = new PhonUIAction(queryHistoryPanel, "goNext");
						historyNextAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-next", IconSize.SMALL));
						historyNextAct.putValue(PhonUIAction.NAME, "View next entry in query history");
						
						final PhonUIAction goFirstAct = new PhonUIAction(queryHistoryPanel, "gotoFirst");
						goFirstAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-first", IconSize.SMALL));
						goFirstAct.putValue(PhonUIAction.NAME, "View oldest entry in query history");
						
						final PhonUIAction goLastAct = new PhonUIAction(queryHistoryPanel, "gotoLast");
						goLastAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/go-last", IconSize.SMALL));
						goLastAct.putValue(PhonUIAction.NAME, "View most recent entry in query history");
						
						final DeleteQueryHistoryEntryAction deleteCurrentEntryAct = new DeleteQueryHistoryEntryAction(QueryAndReportWizard.this);
						final DeleteAllUnnamedEntriesAction deleteUnnamedEntriesAct = new DeleteAllUnnamedEntriesAction(QueryAndReportWizard.this);
						final DeleteQueryHistoryAction deleteAllEntriesAct = new DeleteQueryHistoryAction(QueryAndReportWizard.this);
						
						menu.add(new JMenuItem(saveSettingsAct), idx++);
						menu.add(new JMenuItem(resetQueryAct), idx++);
						menu.add(new JMenuItem(runAct), idx++);
						menu.add(stopItem, idx++);
						menu.insertSeparator(idx++);
						menu.add(discardResultsItem, idx++);
						menu.add(queryResultsMenu, idx++);
						menu.insertSeparator(idx++);
						menu.add(new JMenuItem(goFirstAct), idx++);
						menu.add(new JMenuItem(historyPrevAct), idx++);
						menu.add(new JMenuItem(historyNextAct), idx++);
						menu.add(new JMenuItem(goLastAct), idx++);
						menu.insertSeparator(idx++);
						menu.add(new JMenuItem(deleteCurrentEntryAct), idx++);
						menu.add(new JMenuItem(deleteUnnamedEntriesAct), idx++);
						menu.add(new JMenuItem(deleteAllEntriesAct), idx++);
						menu.insertSeparator(idx++);
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
		showResultsButton.setOpaque(false);
		
		final PhonUIAction showSessionSelectorAct = new PhonUIAction(this, "showSessionSelector");
		showSessionSelectorAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show session selector");
		showSessionSelectorAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("misc/left-arrow-white", IconSize.SMALL));
		showSessionsButton = new JButton(showSessionSelectorAct);
		showSessionsButton.setBorderPainted(false);
		showSessionsButton.setOpaque(false);
		
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
						
						if(runnerPanel.getTaskStatus() == TaskStatus.FINISHED || runnerPanel.getTaskStatus() == TaskStatus.TERMINATED
								|| runnerPanel.getTaskStatus() == TaskStatus.ERROR) {
							// load params from query
							loadParamsFromQuery(runnerPanel.getQuery());
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
		discardResultsButton.setOpaque(false);
		
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
		saveQuerySettingsButton.setOpaque(false);
		
		queryPanel = new TitledPanel("Query");
		queryPanel.getContentContainer().setLayout(new BorderLayout());
		queryPanel.getContentContainer().add(new JScrollPane(scriptPanel), BorderLayout.CENTER);
		
		final PhonUIAction resetQueryAct = new PhonUIAction(this, "resetQueryParameters", queryPanel);
		resetQueryAct.putValue(PhonUIAction.NAME, "Clear query");
		resetQueryAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("misc/parameters-black", IconSize.SMALL));
		resetQueryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset query parameters to default");
		resetQueryButton = new JButton(resetQueryAct);
		resetQueryButton.setOpaque(false);
		
		final PhonUIAction runAct = new PhonUIAction(QueryAndReportWizard.this, "executeQuery");
		runAct.putValue(PhonUIAction.NAME, "Run query");
		runAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		runAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		runQueryButton = new JButton(runAct);
		runQueryButton.setOpaque(false);
		
		queryHistoryPanel = new QueryHistoryPanel(queryHistoryManager, scriptPanel);
		if(queryHistoryManager.size() > 0)
			queryHistoryPanel.gotoLast();
		queryHistoryPanel.setOpaque(false);
		queryPanel.getContentContainer().add(queryHistoryPanel, BorderLayout.NORTH);
		
		final JComponent buttonBar = new JPanel(new HorizontalLayout());
		buttonBar.add(resetQueryButton);
		buttonBar.add(saveQuerySettingsButton);
		buttonBar.add(runQueryButton);
		buttonBar.setOpaque(false);
		queryPanel.setRightDecoration(buttonBar);
		
		splitPane = new JSplitPane();
		splitPane.setLeftComponent(queryLeftPanel);
		splitPane.setRightComponent(queryPanel);
		
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
	
	public void resetQueryParameters(PhonActionEvent pae) {
		queryScript.resetContext();
		
		updateQueryForm();
		queryHistoryPanel.updateLabelFromCurrentHash();
	}
	
	private void updateQueryForm() {
		// don't update before init()
		if(this.scriptPanel == null) return;
		this.scriptPanel.setScript(this.queryScript);
	}
	
	private void loadParamsFromQuery(Query query) {
		this.queryScript.resetContext();
		
		try {
			ScriptParameters scriptParams = this.queryScript.getContext().getScriptParameters(
					this.queryScript.getContext().getEvaluatedScope());
			final Map<String, Object> paramMap = new LinkedHashMap<>();
			paramMap.putAll(query.getScript().getParameters());
			scriptParams.loadFromMap(paramMap);
		} catch (PhonScriptException e) {
			LogUtil.severe(e);
			Toolkit.getDefaultToolkit().beep();
		}
		
		updateQueryForm();
		queryHistoryPanel.updateLabelFromCurrentHash();
	}
	
	private void addToQueryHistory() throws IOException {
		try {
			queryHistoryManager.addParamSet(queryScript);
			QueryHistoryManager.save(queryHistoryManager, queryScript);
		} catch (PhonScriptException e) {
			throw new IOException(e);
		}
		queryHistoryPanel.gotoLast();
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
		final SaveQueryDialog dialog = new SaveQueryDialog(this, queryScript, queryHistoryPanel.getStockQueries(), queryHistoryManager);
		dialog.setModal(true);
	
		String queryName = queryHistoryPanel.getQueryName();
		if(queryName != null)
			dialog.getForm().getNameField().setText(queryName);
		
		dialog.pack();
		dialog.setLocationRelativeTo(this);
		
		dialog.setVisible(true);
		
		// wait for dialog to finish...
		
		queryHistoryPanel.updateLabelFromCurrentHash();
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
	
	public QueryHistoryPanel getQueryHistoryPanel() {
		return this.queryHistoryPanel;
	}
	
	public QueryScript getQueryScript() {
		return this.queryScript;
	}
	
	/**
	 * Return the map of executed queries.
	 * 
	 * @return
	 */
	public Map<String, QueryRunnerPanel> getQueryRunners() {
		return Collections.unmodifiableMap(queryRunners);
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
					addToQueryHistory();
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
		
		previousExeuction = currentSettings();
		showResults();
		runnerPanel.startQuery();
		discardResultsButton.setEnabled(true);
	}
	
	private AtomicReference<ReportTree> masterTreeRef = new AtomicReference<>();
	@Override
	public void executeGraph() throws ProcessingException {
		getProcessor().addProcessorListener( (e) -> {
			if(e.getType() == ProcessorEvent.Type.COMPLETE) {
				getProcessor().getContext().put(NewReportNode.REPORT_TREE_KEY, masterTreeRef.get());
			}
		});
		super.executeGraph();
	}

	@Override
	protected void setupContext(OpContext ctx) {
		super.setupContext(ctx);
		
		String queryName = getTitle();
		final QueryName qn = queryScript.getExtension(QueryName.class);
		if(qn != null)
			queryName = qn.getName();
		
		final ReportTree masterTree = (ReportTree)ctx.get(NewReportNode.REPORT_TREE_KEY);
		final ReportTree queryTree = new ReportTree(new SectionHeaderNode(queryName));
		masterTree.getRoot().add(queryTree.getRoot());
		masterTreeRef.set(masterTree);
		
		ctx.put(NewReportNode.REPORT_TREE_KEY, queryTree);
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
			LogUtil.severe(e.getLocalizedMessage(), e);
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
			LogUtil.severe(ex.getLocalizedMessage(), ex);
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
			// execute one query at a time
			final QueryRunnerPanel runnerPanel = getCurrentQueryRunner();
			if(runnerPanel != null) {
				if(runnerPanel.getTaskStatus() != TaskStatus.FINISHED) {
					int retVal = showMessageDialog(getTitle(), "Query did not complete, continue anyway?", MessageDialogProperties.yesNoOptions);
					if(retVal == 1) {
						return;
					}
				}
			}
			
			// run if no query has executed
			QueryExecutionHistory currentSettings = currentSettings();
			boolean shouldRun = (runnerPanel == null);
			if(previousExeuction != null) {
				if(!previousExeuction.equals(currentSettings)) {
					// ask to execute query again...
					int retVal = showMessageDialog(getTitle(), "Query parameters or session data has changed, run query again?", 
							MessageDialogProperties.yesNoCancelOptions);
					shouldRun = (retVal == 0);
					if(retVal == 2) {
						return;
					}
				}
			}
			if(shouldRun) {
				executeQuery();
				return;
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
	
	private QueryExecutionHistory currentSettings() {
		String paramHash = "";
		try {
			ScriptParameters scriptParams = 
					getQueryScript().getContext().getScriptParameters(getQueryScript().getContext().getEvaluatedScope());
			paramHash = scriptParams.getHashString();
		} catch (PhonScriptException e) {
			LogUtil.severe(e);
		}
			
		List<SessionPath> selectedSessions = sessionSelector.getSelectedSessions();
		Map<SessionPath, ZonedDateTime> sessionModTimes = new HashMap<>();
		for(SessionPath sp:selectedSessions) {
			ZonedDateTime modTime = project.getSessionModificationTime(sp.getCorpus(), sp.getSession());
			sessionModTimes.put(sp, modTime);
		}
			
		return new QueryExecutionHistory(paramHash, selectedSessions, sessionModTimes);
	}
	
	private class QueryExecutionHistory {
		private String paramHash;
		private List<SessionPath> selectedSessions;
		private Map<SessionPath, ZonedDateTime> sessionModTimes;
		
		public QueryExecutionHistory(String paramHash, List<SessionPath> selectedSessions,
				Map<SessionPath, ZonedDateTime> sessionModTimes) {
			super();
			this.paramHash = paramHash;
			this.selectedSessions = selectedSessions;
			this.sessionModTimes = sessionModTimes;
		}

		@Override
		public boolean equals(Object o) {
			if(!(o instanceof QueryExecutionHistory)) return false;
			
			QueryExecutionHistory h = (QueryExecutionHistory)o;
			
			boolean retVal = true;
			
			retVal = paramHash.equals(h.paramHash);
			if(retVal) {
				retVal = (selectedSessions.size() == h.selectedSessions.size() 
						&& selectedSessions.containsAll(h.selectedSessions));
				
				if(retVal) {
					// check all modifiation times
					for(SessionPath sp:selectedSessions) {
						ZonedDateTime myTime = sessionModTimes.get(sp);
						ZonedDateTime theirTime = h.sessionModTimes.get(sp);
						
						retVal &= myTime.isEqual(theirTime);
					}
				}
			}
			
			return retVal;
		}
	}
	
}
