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
package ca.phon.app.query;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

import ca.phon.app.script.ScriptPanel;
import org.jdesktop.swingx.*;

import ca.phon.app.log.*;
import ca.phon.app.menu.query.*;
import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.nodes.*;
import ca.phon.app.opgraph.nodes.report.*;
import ca.phon.app.opgraph.report.*;
import ca.phon.app.opgraph.report.tree.*;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.app.query.QueryAndReportWizardSettings.*;
import ca.phon.app.query.actions.*;
import ca.phon.app.session.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.nodes.general.*;
import ca.phon.plugin.*;
import ca.phon.project.*;
import ca.phon.query.db.*;
import ca.phon.query.history.*;
import ca.phon.query.script.*;
import ca.phon.script.*;
import ca.phon.script.params.*;
import ca.phon.script.params.history.*;
import ca.phon.session.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.menu.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.tristatecheckbox.*;
import ca.phon.ui.wizard.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;
import ca.phon.worker.PhonTask.*;

/**
 * Wizard for executing queries and producing HTML reports.
 * 
 */
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
	
	public final static String STOCK_REPORT_HASH_FILE = QueryAndReportWizard.class.getName() + ".stockReportHashFile";
	public final static String DEFAULT_STOCK_REPORT_HASH_FILE = 
			PrefHelper.getUserDataFolder() + File.separator + "reporthash.properties";
	private String stockReportHashFile = 
			PrefHelper.get(STOCK_REPORT_HASH_FILE, DEFAULT_STOCK_REPORT_HASH_FILE);
	
	private WizardStep queryStep;
	private JSplitPane splitPane;
	
	private JSplitPane queryLeftPanel;
	private TitledPanel sessionSelectorPanel;
	private SessionSelector sessionSelector;
	
	private TitledPanel queryResultsPanel;
	private CardLayout queryResultsLayout;
	
	private TitledPanel queryPanel;
	private ScriptPanel scriptPanel;
	private JCheckBox includeExcludedBox;
	private JButton duplicateQueryButton;
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
	private QueryHistoryAndNameToolbar queryHistoryPanel;
	
	private CountDownLatch queryLatch;
	private QueryExecutionHistory previousExeuction;
	
	private final QueryAndReportWizardSettings settings;
	
	private int windowIdx = 0;

	/**
	 * Find all open results sets for the given session.
	 *
	 * @param session
	 * @return all open result sets along with query names for given session
	 */
	public static List<Tuple<QueryAndReportWizard, Tuple<String, ResultSet>>> findOpenResultSets(Session session) {
		List<Tuple<QueryAndReportWizard, Tuple<String, ResultSet>>> retVal = new ArrayList<>();
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			if(cmf instanceof QueryAndReportWizard) {
				QueryAndReportWizard wizard = (QueryAndReportWizard)cmf;
				var openQueries = wizard.getQueryRunners();
				for(String queryName:openQueries.keySet()) {
					var runnerPanel = openQueries.get(queryName);

					final QueryManager queryManager = QueryManager.getSharedInstance();
					final ResultSetManager rsManager = queryManager.createResultSetManager();

					var sessionPath = new SessionPath(session.getCorpus(), session.getName());
					var rs = rsManager.getResultSetsForQuery(runnerPanel.getTempProject(), runnerPanel.getQuery())
							.stream().filter( (currentRs) -> currentRs.getSessionPath().equals(sessionPath.toString()) )
							.findAny();
					if(rs.isPresent()) {
						var tuple = new Tuple<>(wizard, new Tuple<>(queryName, rs.get()));
						retVal.add(tuple);
					}
				}
			}
		}
		return retVal;
	}
	
	public QueryAndReportWizard(Project project, QueryScript queryScript, QueryAndReportWizardSettings settings) {
		// init with 'dummy' processor and graph as these will be created 0during the wizard
		super("Query", new Processor(new OpGraph()), new OpGraph());
		
		this.settings = settings;
		this.project = project;
		putExtension(Project.class, project);
		
		this.queryScript = queryScript;
		this.queryHistoryManager = QueryHistoryManager.getCachedInstance(queryScript);
		
		// setup UI
		init();
		
		gotoStep(0);
	}
	
	private QueryAndReportWizard(int idx, Project project, QueryScript queryScript, QueryHistoryManager queryHistoryManager, QueryAndReportWizardSettings settings) {
		// init with 'dummy' processor and graph as these will be created 0during the wizard
		super("(" + idx + ") " + "Query", new Processor(new OpGraph()), new OpGraph());
		
		this.settings = settings;
		this.windowIdx = idx;
		this.project = project;
		putExtension(Project.class, project);
		
		this.queryScript = queryScript;
		this.queryHistoryManager = queryHistoryManager;
		
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
		
		nextButton.setText("Run query");
		setBounds(nextButton);
		
		updateWindowName();
	}
	
	public void updateWindowName() {
		QueryName qn = queryScript.getExtension(QueryName.class);
		if(qn == null) {
			qn = new QueryName("Untitled");
		}

		String queryName = (qn.getLocation() != null ? 
				(new File(URLDecoder.decode(qn.getLocation().getPath()))).getName() : 
				qn.getName());
		
		if(queryName.endsWith(".js") || queryName.endsWith(".xml")) {
			// remove extension
			queryName = queryName.substring(0, queryName.lastIndexOf('.'));
		}
		
		StringBuilder builder = new StringBuilder();
		if(windowIdx > 0) {
			builder.append("(").append(windowIdx).append(") ");
		}
		builder.append("Query : ").append(queryName);
		
		if(queryRunnerBox.getModel().getSize() > 0
				&& queryRunnerBox.getSelectedIndex() >= 0) {
			builder.append(" (").append(queryRunnerBox.getSelectedItem()).append(")");
		} else {
			builder.append(" (No results)");
		}
		
		setWindowName(builder.toString());
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
						stopAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/process-stop", IconSize.SMALL));
						final JMenuItem stopItem = new JMenuItem(stopAct);
						boolean stopEnabled = (getCurrentQueryRunner() != null && getCurrentQueryRunner().isRunning());
						stopItem.setEnabled(stopEnabled);
						
						final PhonUIAction resetQueryAct = new PhonUIAction(QueryAndReportWizard.this, "resetQueryParameters");
						resetQueryAct.putValue(PhonUIAction.NAME, "Clear query settings");
						resetQueryAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/draw-eraser", IconSize.SMALL));
						resetQueryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Clear query settings (reset to default)");
						
						final JMenu saveQueryMenu = new JMenu("Save Query");
						saveQueryMenu.setIcon(IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));
						saveQueryMenu.addMenuListener(new MenuListener() {
							
							@Override
							public void menuSelected(MenuEvent e) {
								saveQueryMenu.removeAll();
								
								if(queryHistoryPanel != null) {
									// setup menu from query history and name panel
									queryHistoryPanel.setupSaveMenu(new MenuBuilder(saveQueryMenu));
								}
							}
							
							@Override
							public void menuDeselected(MenuEvent e) {
								
							}
							
							@Override
							public void menuCanceled(MenuEvent e) {
								
							}
						});
						
						final PhonUIAction duplicateAct = new PhonUIAction(QueryAndReportWizard.this, "onDuplicateQueryWizard");
						duplicateAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/window-new", IconSize.SMALL));
						duplicateAct.putValue(PhonUIAction.NAME, "New window");
						
						final PhonUIAction discardResultsAct = new PhonUIAction(QueryAndReportWizard.this, "discardResults");
						discardResultsAct.putValue(PhonUIAction.NAME, "Discard results");
						discardResultsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Discard current result set");
						discardResultsAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL));
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
								discardAllAction.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL));
							
								queryResultsMenu.add(new JMenuItem(discardAllAction));
							}
							
							@Override
							public void menuDeselected(MenuEvent e) {
							}
							
							@Override
							public void menuCanceled(MenuEvent e) {
							}
						});
						
						final boolean hasStockQueries = queryHistoryPanel.getStockQueries().getNamedParamSets().size() > 0;
						final JMenu stockQueryMenu = new JMenu("Stock queries");
						stockQueryMenu.addMenuListener(new MenuListener() {
							
							@Override
							public void menuSelected(MenuEvent e) {
								stockQueryMenu.removeAll();
								List<ParamSetType> namedParamSets = queryHistoryPanel.getStockQueries().getNamedParamSets();
								namedParamSets.sort( (ps1, ps2) -> ps1.getName().compareTo(ps2.getName()) );
								for(ParamSetType paramSet:namedParamSets) {
									final PhonUIAction psAct = new PhonUIAction(QueryAndReportWizard.this, "loadNamedQuery", paramSet);
									psAct.putValue(PhonUIAction.NAME, paramSet.getName());
									stockQueryMenu.add(new JMenuItem(psAct));
								}
							}
							
							@Override
							public void menuDeselected(MenuEvent e) {
								
							}
							
							@Override
							public void menuCanceled(MenuEvent e) {
								
							}
							
						});
						
						final boolean hasUserQueries = queryHistoryManager.getNamedParamSets().size() > 0;
						JMenu userQueryMenu = new JMenu("User queries");
						userQueryMenu.addMenuListener(new MenuListener() {
							
							@Override
							public void menuSelected(MenuEvent e) {
								userQueryMenu.removeAll();
								List<ParamSetType> namedParamSets = queryHistoryManager.getNamedParamSets();
								namedParamSets.sort( (ps1, ps2) -> ps1.getName().compareTo(ps2.getName()) );
								for(ParamSetType paramSet:namedParamSets) {
									final PhonUIAction psAct = new PhonUIAction(QueryAndReportWizard.this, "loadNamedQuery", paramSet);
									psAct.putValue(PhonUIAction.NAME, paramSet.getName());
									userQueryMenu.add(new JMenuItem(psAct));
								}
							}
							
							@Override
							public void menuDeselected(MenuEvent e) {
								
							}
							
							@Override
							public void menuCanceled(MenuEvent e) {
								
							}
							
						});
						
						final DeleteAllUnnamedEntriesAction deleteUnnamedEntriesAct = new DeleteAllUnnamedEntriesAction(QueryAndReportWizard.this);
						menu.add(new JMenuItem(duplicateAct), idx++);
						
						final List<SimpleEditor> analysisComposers = new ArrayList<>();
						for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
							if(cmf instanceof SimpleEditor && cmf.getTitle().startsWith("Analysis Composer")) {
								SimpleEditor editor = (SimpleEditor)cmf;
								analysisComposers.add(editor);
							}
						}
						if(analysisComposers.size() == 0) {
							menu.add(new JMenuItem(new SendToAnalysisComposer(QueryAndReportWizard.this)), idx++);
						} else {
							JMenu sendToMenu = new JMenu("Send to Analysis Composer");
							
							for(SimpleEditor editor:analysisComposers) {
								sendToMenu.add(new SendToAnalysisComposer(QueryAndReportWizard.this, editor));
							}
							sendToMenu.addSeparator();
							sendToMenu.add(new SendToAnalysisComposer(QueryAndReportWizard.this));
							
							sendToMenu.setIcon(IconManager.getInstance().getIcon("actions/share", IconSize.SMALL));
							menu.add(sendToMenu, idx++);
						}
						
						menu.insertSeparator(idx++);
						menu.add(saveQueryMenu, idx++);
						menu.add(new JMenuItem(resetQueryAct), idx++);
						menu.add(new JMenuItem(runAct), idx++);
						menu.add(stopItem, idx++);
						menu.insertSeparator(idx++);
						menu.add(discardResultsItem, idx++);
						menu.add(queryResultsMenu, idx++);
						menu.insertSeparator(idx++);
						if(hasStockQueries || hasUserQueries) {
							final JMenuItem sepItem = new JMenuItem("-- Named Queries --");
							sepItem.setEnabled(false);
							menu.add(sepItem, idx++);
							if(hasStockQueries) {
								menu.add(stockQueryMenu, idx++);
							}
							if(hasUserQueries) {
								menu.add(userQueryMenu, idx++);
							}
							menu.insertSeparator(idx++);
						}
						menu.add(new JMenuItem(deleteUnnamedEntriesAct), idx++);
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
	
	public void loadNamedQuery(ParamSetType paramSet) {
		queryHistoryPanel.loadFromParamSet(paramSet);
		queryHistoryPanel.updateLabelFromCurrentHash();
	}
	
	@Override
	public void close() {
		if(getCurrentStep() == queryStep) {
			final QueryRunnerPanel runnerPanel = getCurrentQueryRunner();
			if(runnerPanel != null && runnerPanel.isRunning()) {
				int retVal = showMessageDialog("Stop query?", "Stop current query execution?", MessageDialogProperties.okCancelOptions);
				if(retVal == 0) {
					onStopQuery();
				}
				return;
			}
		}
		
		if(settings.isCleanHistoryOnClose()) {
			queryHistoryManager.removeAllUnnamedParamSets();
			
			try {
				QueryHistoryManager.save(queryHistoryPanel.getQueryHistoryManager(), (QueryScript)queryHistoryPanel.getScriptPanel().getScript());
			} catch (IOException e) {
				Toolkit.getDefaultToolkit().beep();
				LogUtil.severe(e);
			}
		}
		
		super.close();
	}
	
	private WizardStep createQueryStep() {
		WizardStep retVal = new WizardStep();
		QueryName qn = queryScript.getExtension(QueryName.class);
		retVal.setTitle("Query: " + qn.getName());
		
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

		final JPanel sessionSelectorRightDecoration = new JPanel(new HorizontalLayout());
		sessionSelectorRightDecoration.setOpaque(false);
		sessionSelectorRightDecoration.add(includeExcludedBox);
		sessionSelectorPanel.setRightDecoration(sessionSelectorRightDecoration);
		
		queryRunnerComboBoxModel = new DefaultComboBoxModel<>();
		queryRunnerBox = new JComboBox<>(queryRunnerComboBoxModel);
		queryRunnerBox.addItemListener( (e) -> {
			if(e.getStateChange() == ItemEvent.SELECTED) {
				final Object selected = queryRunnerBox.getSelectedItem();
				if(selected != null) {
					queryResultsLayout.show(queryResultsPanel.getContentContainer(), selected.toString());
					updateWindowName();
					
					discardResultsButton.setEnabled(true);
					final QueryRunnerPanel runnerPanel = getCurrentQueryRunner();
					if(runnerPanel != null) {
						if(runnerPanel.isRunning()) {
							discardResultsButton.setIcon(IconManager.getInstance().getIcon("actions/process-stop", IconSize.SMALL));
						} else {
							discardResultsButton.setIcon(IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL));
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
		discardResultsAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL));
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
		queryResultsPanel.setRightDecoration(queryResultsRightDecoration);
		
		final JPanel noResultsPanel = new JPanel(new BorderLayout());
		final JLabel noResultsLabel = new JLabel("No results");
		noResultsLabel.setEnabled(false);
		noResultsLabel.setFont(noResultsLabel.getFont().deriveFont(Font.ITALIC));
		noResultsLabel.setHorizontalAlignment(JLabel.CENTER);
		noResultsPanel.add(noResultsLabel, BorderLayout.CENTER);
		queryResultsPanel.getContentContainer().add(noResultsPanel, "noResults");
		queryResultsPanel.setVisible(false);
		
		queryLeftPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		queryLeftPanel.setLeftComponent(sessionSelectorPanel);
		queryLeftPanel.setRightComponent(queryResultsPanel);
				
		scriptPanel = new ScriptPanel(queryScript);
		
		queryPanel = new TitledPanel("Query");
		queryPanel.getContentContainer().setLayout(new BorderLayout());
		JScrollPane scriptScroller = new JScrollPane(scriptPanel);
		scriptScroller.getViewport().setBackground(scriptPanel.getBackground());
		queryPanel.getContentContainer().add(scriptScroller, BorderLayout.CENTER);
		
		final PhonUIAction duplicateQueryAct = new PhonUIAction(this, "onDuplicateQueryWizard");
		duplicateQueryAct.putValue(PhonUIAction.NAME, "New window");
		duplicateQueryAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open a new query wizard");
		duplicateQueryAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/window-new", IconSize.SMALL));
		duplicateQueryButton = new JButton(duplicateQueryAct);
		duplicateQueryButton.setOpaque(false);
		
		final PhonUIAction runAct = new PhonUIAction(QueryAndReportWizard.this, "executeQuery");
		runAct.putValue(PhonUIAction.NAME, "Run query");
		runAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		runAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL));
		runQueryButton = new JButton(runAct);
		runQueryButton.setOpaque(false);
		
		queryHistoryPanel = new QueryHistoryAndNameToolbar(queryHistoryManager, scriptPanel);
		
		if(settings.isLoadPreviousExecutionOnStartup()) {
			if(queryHistoryManager.size() > 0)
				queryHistoryPanel.gotoLast();
			else if(queryHistoryPanel.getStockQueries().size() > 0) {
				queryHistoryPanel.loadFromParamSet(queryHistoryPanel.getStockQueries().getParamSet(0));
				queryHistoryPanel.updateLabelFromCurrentHash();
			}
		}
		
		queryHistoryPanel.setOpaque(false);
		queryPanel.getContentContainer().add(queryHistoryPanel, BorderLayout.NORTH);
		
		final JComponent buttonBar = new JPanel(new HorizontalLayout());
		buttonBar.add(duplicateQueryButton);
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
		queryResultsPanel.setVisible(true);
		queryLeftPanel.setDividerLocation(0.4);
		queryLeftPanel.revalidate();
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
	
	private void updateNextButton() {
		if(getCurrentStepIndex() == getStepIndex(queryStep)) {
			if(previousExeuction != null) {
				nextButton.setText("Next: Report Composer");
			} else {
				nextButton.setText("Run query");
			}
		} else if(getCurrentStepIndex() == getStepIndex(reportEditorStep)) {
			nextButton.setText("Next: Report");
		}
		setBounds(nextButton);
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
		
		boolean reportHashesUnchanged = checkStockQueryReportHash();
		if(prevReportFile.exists() && !reportHashesUnchanged) {
			prevReportFile.delete();
		}
		
		if(settings.getReportLoadStrategy() == ReportLoadStrategy.LoadPreviousReport && prevReportFile.exists() && reportHashesUnchanged) {
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
			LogUtil.info("Loading default query report");
			reportEditor.addDocument(defaultReportURL);
		}
		
		reportEditor.getModel().getDocument().markAsUnmodified();
	}
	
	private void createStockQueryReportHash() {
		try {
			Properties props = loadInternalQueryReportHash();
			props.store(new FileOutputStream(new File(stockReportHashFile)), "");
		} catch (IOException e) {
			LogUtil.severe(e);
		}
	}
	
	private Properties loadInternalQueryReportHash() {
		Properties props = new Properties();
		try {
			Enumeration<URL> hashEnum = 
					getClass().getClassLoader().getResources("report/reports.hash");
			while(hashEnum.hasMoreElements()) {
				URL reportHashURL = hashEnum.nextElement();
				
				props.load(reportHashURL.openStream());
			}
		} catch (IOException e) {
			LogUtil.severe(e);
		}
		return props;
	}
	
	private boolean checkStockQueryReportHash() {
		boolean retVal = false;
		
		Properties internalHashes = loadInternalQueryReportHash();
		Properties existingHashes = new Properties();
		File hashFile = new File(stockReportHashFile);
		if(hashFile.exists()) {
			try {
				existingHashes.load(new FileInputStream(stockReportHashFile));
				
				if(existingHashes.keySet().containsAll(internalHashes.keySet())) {
					retVal = true;
					for(Object key:internalHashes.keySet()) {
						retVal &= existingHashes.get(key).equals(internalHashes.get(key));
					}
				}
			} catch (IOException e) {
				LogUtil.severe(e);
			}
		}
		
		if(!retVal) {
			LogUtil.info("Creating stock query report hash");
			createStockQueryReportHash();
		}
		
		return retVal;
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
		
	public void newWindow(Project project) {
		int wizardIdx = this.windowIdx;
		for(CommonModuleFrame cmf:CommonModuleFrame.getOpenWindows()) {
			if(cmf instanceof QueryAndReportWizard && cmf != this) {
				QueryAndReportWizard qw = (QueryAndReportWizard)cmf;
				if(qw.getQueryScript().getExtension(QueryName.class) == getQueryScript().getExtension(QueryName.class)) {
					wizardIdx = Math.max(wizardIdx, qw.windowIdx);
				}
			}
		}

		// create a new instance of the query script
		final QueryScript qs = new QueryScript(getQueryScript().getScript());
		
		try {
			ScriptParameters currentParams = getQueryScript().getContext().getScriptParameters(getQueryScript().getContext().getEvaluatedScope());
			ScriptParameters newParams = qs.getContext().getScriptParameters(qs.getContext().getEvaluatedScope());
			
			ScriptParameters.copyParams(currentParams, newParams);
		} catch (PhonScriptException e) {
			LogUtil.warning(e);
		}
		
		qs.putExtension(QueryName.class, getQueryScript().getExtension(QueryName.class));
		
		QueryAndReportWizardSettings settings = new QueryAndReportWizardSettings();
		settings.setLoadPreviousExecutionOnStartup(false);
		
		QueryAndReportWizard wizard = new QueryAndReportWizard(wizardIdx+1, project, qs, queryHistoryManager, settings);
		wizard.pack();
		wizard.setSize(getSize());
		wizard.cascadeWindow(this);
		wizard.setVisible(true);
	}
	
	public void newWindow() {
		newWindow(project);
	}
	
	public void onDuplicateQueryWizard() {
		newWindow();
	}
	
	private WizardStep createReportConfigStep() {
		WizardStep retVal = new WizardStep();
		retVal.setTitle("Report Composer");
		
		reportEditor = new SimpleEditorPanel(
				project,
				new ReportLibrary(), new ReportEditorModelInstantiator(), new ReportNodeInstantiator(),
				(qs, reportGraph) -> new MacroNode(),
				(graph, project) -> new ReportRunner(graph, getCurrentQueryProject(), getCurrentQueryId()) );
		// toolbar customizations
		reportEditor.getRunButton().setVisible(false);
		
		retVal.setLayout(new BorderLayout());
		retVal.add(reportEditor, BorderLayout.CENTER);
		
		loadInitialQueryReport();
		
		return retVal;
	}
	
	public QueryHistoryAndNameToolbar getQueryHistoryPanel() {
		return this.queryHistoryPanel;
	}
	
	public QueryScript getQueryScript() {
		return this.queryScript;
	}
	
	public SimpleEditorPanel getReportComposer() {
		return this.reportEditor;
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
		
		if(queryLatch != null && queryLatch.getCount() > 0) {
			// already executing a query!! bail
			return;
		}
		
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
				SwingUtilities.invokeLater( () -> { 
					addNodesToSessionSelector(queryName, runnerPanel);
				});
				try {
					addToQueryHistory();
				} catch (IOException e1) {
					LogUtil.severe(e1);
					Toolkit.getDefaultToolkit().beep();
				}
				
				queryLatch.countDown();
			}
			
			if(e.getNewValue() == TaskStatus.RUNNING) {
				SwingUtilities.invokeLater( () -> {
					discardResultsButton.setIcon(IconManager.getInstance().getIcon("actions/process-stop", IconSize.SMALL));
					nextButton.setVisible(false);
					
					btnStop.setBackground(Color.RED);
					btnStop.setForeground(Color.white);
					btnStop.setText("Stop");
					breadCrumbViewer.add(btnStop);
					setBounds(btnStop);
				});
			} else {
				SwingUtilities.invokeLater( () -> {
					discardResultsButton.setIcon(IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL));
					nextButton.setVisible(true);
					updateNextButton();
					breadCrumbViewer.remove(btnStop);
				});
			}
		});
		
		final ImageIcon closeIcon = IconManager.getInstance().getIcon("actions/list-remove", IconSize.XSMALL);
		final PhonUIAction closeAction = new PhonUIAction(this, "discardResults", queryName);
		closeAction.putValue(PhonUIAction.SMALL_ICON, closeIcon);
		closeAction.putValue(PhonUIAction.SHORT_DESCRIPTION, "Discard results");
		
		queryLatch = new CountDownLatch(1);
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
				updateWindowName();
				
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
			if(queryLatch == null || queryLatch.getCount() == 0) {
				// run if no query has executed
				QueryExecutionHistory currentSettings = currentSettings();
				boolean shouldRun = true;
				if(previousExeuction != null) {
					shouldRun = false;
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
			if(queryLatch != null && queryLatch.getCount() > 0) {
				showMessageDialog(getTitle(), "Please wait for query to complete", MessageDialogProperties.okOptions);
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
		updateNextButton();
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
