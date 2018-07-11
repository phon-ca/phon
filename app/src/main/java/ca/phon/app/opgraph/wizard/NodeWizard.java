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
package ca.phon.app.opgraph.wizard;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.TreePath;

import org.apache.velocity.tools.generic.MathTool;
import org.jdesktop.swingx.JXBusyLabel;

import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.LogUtil;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.app.log.actions.SaveBufferAction;
import ca.phon.app.log.actions.SaveBufferAsWorkbookAction;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.opgraph.GlobalParameter;
import ca.phon.app.opgraph.GlobalParameterPanel;
import ca.phon.app.opgraph.nodes.log.BufferNodeConstants;
import ca.phon.app.opgraph.nodes.log.PrintBufferNode;
import ca.phon.app.opgraph.nodes.query.QueryNode;
import ca.phon.app.opgraph.nodes.report.NewReportNode;
import ca.phon.app.opgraph.nodes.report.ReportSectionNode;
import ca.phon.app.opgraph.report.tree.ReportTree;
import ca.phon.app.opgraph.report.tree.ReportTreeNode;
import ca.phon.app.opgraph.report.tree.SectionHeaderNode;
import ca.phon.app.opgraph.report.tree.TableNode;
import ca.phon.app.opgraph.wizard.WizardOptionalsCheckboxTree.CheckedOpNode;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToFolderAction;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToFolderAction.ExportType;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToWorkbookAction;
import ca.phon.app.query.ScriptPanel;
import ca.phon.app.session.editor.SessionEditorEP;
import ca.phon.formatter.FormatterUtil;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.ProcessorEvent;
import ca.phon.opgraph.ProcessorListener;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.exceptions.ProcessingException;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.ParticipantHistory;
import ca.phon.project.Project;
import ca.phon.query.db.QueryFactory;
import ca.phon.query.db.Result;
import ca.phon.query.db.ResultValue;
import ca.phon.query.db.xml.XMLQueryFactory;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.query.script.QueryTask;
import ca.phon.session.Session;
import ca.phon.session.SessionPath;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.MsFormatter;
import ca.phon.util.PrefHelper;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonTask;
import ca.phon.worker.PhonTask.TaskStatus;
import ca.phon.worker.PhonWorker;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

/**
 * The Node wizard servers as the UI layer for opgraph
 * analysis and reports.  It provides the following steps
 * by default:
 * <ul>
 * <li>Introduction (if supplied in the {@link WizardExtension})</li>
 * <li>Optional Nodes (if any are defined in the {@link WizardExtension})</li>
 * <li>Settings for Nodes (if any are defined as 'required' in the {@link WizardExtension})</li>
 * <li>Report data generation</li>
 * <li>Report</li>
 * </ul>
 *
 * The wizard also provides a panel for buffer storage - used during opgraph execution
 * for report data.  Finally, any reports defined in the {@link WizardExtension} are displayed
 * during the last step of the wizard.  Reports utilize data generated during the
 * report data step.  Reports are written using Apache velocity syntax.  Table data may be
 * accessed from the <code>$tables</code> map variable, buffer text data may be
 * accessed from the <code>$buffers</code> map varaible.  The map keys are the names
 * of the buffers generated during the report data step.
 *
 * Other variables available to the velocity context are:
 * <ul>
 * <li><code>$Class</code> - static access to java.lang.Class</li>
 * <li><code>$FormatterUtil</code> - access to Phon object formatters</li>
 * <li><code>$project</code> - the project</li>
 * <li><code>$graph</code> - the opgraph used</li>
 * </ul>
 *
 */
public class NodeWizard extends WizardFrame {

	private static final long serialVersionUID = -652423592288338133L;

	private final static String DEFAULT_REPORT_FILE = "ca/phon/app/opgraph/wizard/DefaultReport.vm";

	private final static Logger LOGGER = Logger.getLogger(NodeWizard.class.getName());

	private Processor processor;

	private OpGraph graph;

	private WizardMultiBufferPanel bufferPanel;

	private JXBusyLabel busyLabel;

	private JLabel statusLabel;

	protected TitledPanel reportTitledPanel;
	protected WizardStep reportDataStep;
	private Timer reportTimer;
	private long reportStartTime;

	protected WizardStep optionalsStep;

	private WizardOptionalsCheckboxTree optionalsTree;

	private Map<OpNode, WizardStep> optionalSteps;

	protected GlobalParameterPanel globalOptionsPanel;
	public final static String CASE_SENSITIVE_GLOBAL_OPTION = GlobalParameter.CASE_SENSITIVE.getParamId();
	public final static String IGNORE_DIACRITICS_GLOBAL_OPTION = GlobalParameter.IGNORE_DIACRITICS.getParamId();
	public final static String INVENTORY_GROUPING_GLOBAL_OPTION = GlobalParameter.INVENTORY_GROUPING_COLUMN.getParamId();

	protected boolean inInit = true;

	boolean reportSaved = false;

	private volatile boolean running = false;

	private NodeWizardBreadcrumbButton btnRunAgain;

	private final WebViewInterface webViewInterface = new WebViewInterface();

	public NodeWizard(String title, Processor processor, OpGraph graph) {
		super(title);
		setBreadcrumbVisible(true);
		setWindowName(title);

		this.processor = processor;
		this.graph = graph;
		init();
		inInit = false;
	}

	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);

		final MenuBuilder builder = new MenuBuilder(menuBar);
		
		final JMenu reportMenu = builder.addMenu(".@Analysis", "Report");
		reportMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				setupReportMenu(reportMenu);
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		
		
		final JMenu bufferMenu = builder.addMenu(".@Report", "Buffer");
		bufferMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				bufferPanel.setupMenu(bufferMenu);
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
				
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
				
			}
		});
	}
	
	public void setupReportMenu(JMenu menu) {
		menu.removeAll();
		final MenuBuilder builder = new MenuBuilder(menu);
		
		// add global options
		if(globalOptionsPanel != null) {
			builder.addItem(".", "-- Global Options --").setEnabled(false);
			
			final String caseSensitiveValue = (globalOptionsPanel.isUseGlobalCaseSensitive()
					? (globalOptionsPanel.isCaseSensitive() ? "yes" : "no")
					: "default");
			final JMenu caseSensitiveMenu = builder.addMenu(".", "Case sensitive: " + caseSensitiveValue);
			
			final PhonUIAction defCSAct = new PhonUIAction(globalOptionsPanel, "useDefaultCaseSensitive");
			defCSAct.putValue(PhonUIAction.NAME, "default");
			defCSAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "");
			defCSAct.putValue(PhonUIAction.SELECTED_KEY, !globalOptionsPanel.isUseGlobalCaseSensitive());
			final JCheckBoxMenuItem defCSItem = new JCheckBoxMenuItem(defCSAct);
			caseSensitiveMenu.add(defCSItem);
			
			final PhonUIAction yesCSAct = new PhonUIAction(globalOptionsPanel, "setCaseSensitive", true);
			yesCSAct.putValue(PhonUIAction.NAME, "yes");
			yesCSAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override case sensitive options in report settings");
			yesCSAct.putValue(PhonUIAction.SELECTED_KEY, globalOptionsPanel.isUseGlobalCaseSensitive() && globalOptionsPanel.isCaseSensitive());
			final JCheckBoxMenuItem yesCSItem = new JCheckBoxMenuItem(yesCSAct);
			caseSensitiveMenu.add(yesCSItem);
			
			final PhonUIAction noCSAct = new PhonUIAction(globalOptionsPanel, "setCaseSensitive", false);
			noCSAct.putValue(PhonUIAction.NAME, "no");
			noCSAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override case sensitive options in report settings");
			noCSAct.putValue(PhonUIAction.SELECTED_KEY, globalOptionsPanel.isUseGlobalCaseSensitive() && !globalOptionsPanel.isCaseSensitive());
			final JCheckBoxMenuItem noCSItem = new JCheckBoxMenuItem(noCSAct);
			caseSensitiveMenu.add(noCSItem);
			
			final String ignoreDiacriticsValue = (globalOptionsPanel.isUseGlobalIgnoreDiacritics()
					? (globalOptionsPanel.isIgnoreDiacritics() ? "yes" : "no")
					: "default");
			final JMenu ignoreDiacriticsMenu = builder.addMenu(".", "Ignore diacritics: " + ignoreDiacriticsValue);
			
			final PhonUIAction defIDAct = new PhonUIAction(globalOptionsPanel, "useDefaultIgnoreDiacritics");
			defIDAct.putValue(PhonUIAction.NAME, "default");
			defIDAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "");
			defIDAct.putValue(PhonUIAction.SELECTED_KEY, !globalOptionsPanel.isUseGlobalIgnoreDiacritics());
			final JCheckBoxMenuItem defIDItem = new JCheckBoxMenuItem(defIDAct);
			ignoreDiacriticsMenu.add(defIDItem);
			
			final PhonUIAction yesIDAct = new PhonUIAction(globalOptionsPanel, "setIgnoreDiacritics", true);
			yesIDAct.putValue(PhonUIAction.NAME, "yes");
			yesIDAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override ignore diacritics options in report settings");
			yesIDAct.putValue(PhonUIAction.SELECTED_KEY, globalOptionsPanel.isUseGlobalIgnoreDiacritics() && globalOptionsPanel.isIgnoreDiacritics());
			final JCheckBoxMenuItem yesIDItem = new JCheckBoxMenuItem(yesIDAct);
			ignoreDiacriticsMenu.add(yesIDItem);
			
			final PhonUIAction noIDAct = new PhonUIAction(globalOptionsPanel, "setIgnoreDiacritics", false);
			noIDAct.putValue(PhonUIAction.NAME, "no");
			noIDAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override ignore diacritics options in report settings");
			noIDAct.putValue(PhonUIAction.SELECTED_KEY, globalOptionsPanel.isUseGlobalIgnoreDiacritics() && !globalOptionsPanel.isIgnoreDiacritics());
			final JCheckBoxMenuItem noIDItem = new JCheckBoxMenuItem(noIDAct);
			ignoreDiacriticsMenu.add(noIDItem);
			
			final String inventoryGroupingValue = (globalOptionsPanel.isUseInventoryGrouping()
					? globalOptionsPanel.getInventoryGrouping()
					: "default");
			final JMenu inventoryGroupingMenu = builder.addMenu(".", "Inventory grouping: " + inventoryGroupingValue);
			
			final PhonUIAction defIGAct = new PhonUIAction(globalOptionsPanel, "setInventoryGrouping", "default");
			defIGAct.putValue(PhonUIAction.NAME, "default");
			defIGAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "");
			defIGAct.putValue(PhonUIAction.SELECTED_KEY, !globalOptionsPanel.isUseInventoryGrouping());
			final JCheckBoxMenuItem defIGItem = new JCheckBoxMenuItem(defIGAct);
			inventoryGroupingMenu.add(defIGItem);
			
			final PhonUIAction sessionIGAct = new PhonUIAction(globalOptionsPanel, "setInventoryGrouping", "Session");
			sessionIGAct.putValue(PhonUIAction.NAME, "Session");
			sessionIGAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override inventory grouping options in report settings");
			sessionIGAct.putValue(PhonUIAction.SELECTED_KEY, globalOptionsPanel.isUseInventoryGrouping() && globalOptionsPanel.getInventoryGrouping().equals("Session"));
			final JCheckBoxMenuItem sessionIGItem = new JCheckBoxMenuItem(sessionIGAct);
			inventoryGroupingMenu.add(sessionIGItem);
			
			final PhonUIAction ageIGAct = new PhonUIAction(globalOptionsPanel, "setInventoryGrouping", "Age");
			ageIGAct.putValue(PhonUIAction.NAME, "Age");
			ageIGAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Override inventory grouping options in report settings");
			ageIGAct.putValue(PhonUIAction.SELECTED_KEY, globalOptionsPanel.isUseInventoryGrouping() && globalOptionsPanel.getInventoryGrouping().equals("Age"));
			final JCheckBoxMenuItem ageIGItem = new JCheckBoxMenuItem(ageIGAct);
			inventoryGroupingMenu.add(ageIGItem);
			
			builder.addSeparator(".", "_globalOptions");
		}
				
		final boolean hasReport = reportBufferAvailable();
		final JMenuItem runAgainItem = new JMenuItem("Run again");
		runAgainItem.setToolTipText("Clear results and run report again");
		runAgainItem.addActionListener( (e) -> gotoStep(super.getStepIndex(reportDataStep)) );
		runAgainItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.SHIFT_DOWN_MASK));
		runAgainItem.setEnabled(hasReport);
		
		final SaveTablesToWorkbookAction saveTablesToWorkbookAct = new SaveTablesToWorkbookAction(this);
		saveTablesToWorkbookAct.putValue(Action.NAME, "Export tables as Excel workbook...");
		saveTablesToWorkbookAct.putValue(Action.SHORT_DESCRIPTION, "Export report tables to a single Excel workbook");
		saveTablesToWorkbookAct.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));
		
		final SaveTablesToFolderAction saveTablesCSVAct = new SaveTablesToFolderAction(this, ExportType.CSV);
		saveTablesCSVAct.putValue(Action.NAME, "Export tables to folder (CSV)...");
		saveTablesCSVAct.putValue(Action.SHORT_DESCRIPTION, "Export report tables in CSV format to selected folder - one file per table.");
		saveTablesCSVAct.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));

		final SaveTablesToFolderAction saveTablesExcelAct = new SaveTablesToFolderAction(this, ExportType.EXCEL);
		saveTablesExcelAct.putValue(Action.NAME, "Export tables to folder (XLS)...");
		saveTablesExcelAct.putValue(Action.SHORT_DESCRIPTION, "Export report tables in Excel format to selected folder - one file per table.");
		saveTablesExcelAct.putValue(Action.SMALL_ICON, IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));
		
		final SaveBufferAction saveAct = new SaveBufferAction(getBufferPanel());
		saveAct.putValue(SaveBufferAction.NAME, "Save Report (HTML)...");
		saveAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		saveAct.putValue(PhonUIAction.SMALL_ICON,
				IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL));

		builder.addItem(".", runAgainItem);
		builder.addSeparator(".", "_run");
		builder.addItem(".", saveAct).setEnabled(hasReport);
		builder.addSeparator(".", "_save");
		builder.addItem(".", saveTablesToWorkbookAct).setEnabled(hasReport);
		builder.addItem(".", saveTablesExcelAct).setEnabled(hasReport);
		builder.addItem(".", saveTablesCSVAct).setEnabled(hasReport);
	}

	@Override
	public void close() {
		if(running) {
			// ask to cancel current analysis
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setRunAsync(true);
			props.setTitle("Close Window");
			props.setHeader(props.getTitle());
			props.setMessage("Cancel running process and close window?");
			props.setOptions(MessageDialogProperties.yesNoOptions);
			props.setParentWindow(this);
			props.setListener( (e) -> {
				if(e.getDialogResult() == 0) {
					stopExecution();
					SwingUtilities.invokeLater( () -> super.close() );
				}
			});
			NativeDialogs.showMessageDialog(props);
		} else {
			final boolean hasReport = getBufferPanel().getBufferNames().contains("Report");
			if(hasReport && !reportSaved) {
				// ask to save report
				final MessageDialogProperties props = new MessageDialogProperties();
				final String[] options = new String[] { "Close without saving", "Save Report as HTML", "Export Report to Excel\u2122", "Cancel" };
				props.setTitle("Save Results");
				props.setHeader("Save Results");
				props.setMessage("Save results before closing?");
				props.setRunAsync(true);
				props.setOptions(options);
				props.setDefaultOption(options[0]);
				props.setParentWindow(this);
				props.setListener( (e) -> {
					final int result = e.getDialogResult();
					if(result == 0) {
						SwingUtilities.invokeLater( () -> super.close() );
					} else if(result == 1) {
						SwingUtilities.invokeLater( this::saveReportAsHTML );
					} else if(result == 2) {
						SwingUtilities.invokeLater( this::saveReportAsExcel );
					} else if(result == 3) {
						return;
					}
				});
				NativeDialogs.showMessageDialog(props);
			} else {
				super.close();
			}
		}
	}
	
	public boolean reportBufferAvailable() {
		return bufferPanel.getBufferNames().contains("Report");
	}
	
	private void saveReportAsHTML() {
		final SaveBufferAction saveBufferAct = new SaveBufferAction(getBufferPanel(), "Report");
		SwingUtilities.invokeLater( () -> {
			saveBufferAct.actionPerformed(new ActionEvent(NodeWizard.this, 0, "save"));
			reportSaved = true;
//			super.close();
		});
	}

	private void saveReportAsExcel() {
		final SaveBufferAsWorkbookAction saveBufferAct = new SaveBufferAsWorkbookAction(getBufferPanel(), "Report");
		SwingUtilities.invokeLater( () -> {
			saveBufferAct.actionPerformed(new ActionEvent(NodeWizard.this, 0, "export"));
			reportSaved = true;
//			super.close();
		});
	}

	private void setBounds(JButton endBtn) {
		final Rectangle bounds =
				new Rectangle(breadCrumbViewer.getBreadcrumbViewerUI().getPreferredSize().width-endBtn.getInsets().left/2-1,
						0, endBtn.getPreferredSize().width, breadCrumbViewer.getHeight());
		endBtn.setBounds(bounds);
	}

	private void init() {
		globalOptionsPanel = new GlobalParameterPanel();
		
		// turn off parent navigation controls
		super.btnBack.setVisible(false);
		super.btnCancel.setVisible(false);
		super.btnFinish.setVisible(false);
		super.btnNext.setVisible(false);

		bufferPanel = new WizardMultiBufferPanel(this);

		btnNext = new NodeWizardBreadcrumbButton();
		btnNext.setFont(FontPreferences.getTitleFont());
		btnNext.setText("Next");
		btnNext.addActionListener( (e) -> next() );

		btnCancel = new NodeWizardBreadcrumbButton();
		btnCancel.setFont(FontPreferences.getTitleFont().deriveFont(Font.BOLD));
		btnCancel.setText("Stop");
		btnCancel.setBackground(Color.red);
		btnCancel.setForeground(Color.white);
		btnCancel.addActionListener( (e) -> cancel() );

		btnRunAgain = new NodeWizardBreadcrumbButton();
		btnRunAgain.setFont(FontPreferences.getTitleFont().deriveFont(Font.BOLD));
		btnRunAgain.setText("Run again");
		btnRunAgain.addActionListener( (e) -> gotoStep(super.getStepIndex(reportDataStep)) );
		btnRunAgain.setVisible(false);

		breadCrumbViewer.setStateBackground(btnNext.getBackground().darker());

		final Runnable updateBreadcrumbButtons = () -> {
			JButton endBtn = btnNext;
			if(breadCrumbViewer.getBreadcrumb().getCurrentState() == reportDataStep) {
				breadCrumbViewer.remove(btnNext);

				breadCrumbViewer.add(btnRunAgain);
				setBounds(btnRunAgain);

				breadCrumbViewer.add(btnCancel);
				setBounds(btnCancel);

				endBtn = btnCancel;
			} else {
				breadCrumbViewer.remove(btnRunAgain);
				breadCrumbViewer.remove(btnCancel);

				breadCrumbViewer.add(btnNext);
				setBounds(btnNext);
			}

			getRootPane().setDefaultButton(endBtn);

			breadCrumbViewer.revalidate();
			breadCrumbViewer.scrollRectToVisible(endBtn.getBounds());
		};

		breadCrumbViewer.setFont(FontPreferences.getTitleFont().deriveFont(Font.BOLD));
		breadCrumbViewer.setBackground(Color.white);
		breadCrumbViewer.getBreadcrumb().addBreadcrumbListener( (evt) -> {
			SwingUtilities.invokeLater(updateBreadcrumbButtons);
		});
		updateBreadcrumbButtons.run();

		final JScrollPane breadcrumbScroller = new JScrollPane(breadCrumbViewer);
		breadcrumbScroller.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.darkGray));
		breadcrumbScroller.getViewport().setBackground(breadCrumbViewer.getBackground());
		breadcrumbScroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		breadcrumbScroller.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 0));
		breadcrumbScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		add(breadcrumbScroller, BorderLayout.NORTH);

		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		busyLabel.getBusyPainter().setHighlightColor(Color.white);
		statusLabel = new JLabel();

		final WizardExtension nodeWizardList =
				(graph.getExtensionClasses().contains(WizardExtension.class)
				? graph.getExtension(WizardExtension.class)
				: new WizardExtension(getGraph()));
		int stepIdx = 0;

		if(nodeWizardList.getWizardMessage() != null
				&& nodeWizardList.getWizardMessage().length() > 0) {
			final WizardStep aboutStep = createIntroStep(nodeWizardList.getWizardTitle(),
					nodeWizardList.getWizardInfo().getMessageHTML());
			aboutStep.setTitle("About");
			aboutStep.setPrevStep(stepIdx-1);
			aboutStep.setNextStep(stepIdx+1);
			++stepIdx;

			addWizardStep(aboutStep);
		}

		if(nodeWizardList.getOptionalNodeCount() > 0) {
			optionalsStep = createOptionalsStep();
			optionalsStep.setPrevStep(stepIdx-1);
			optionalsStep.setNextStep(stepIdx+1);
			++stepIdx;

			addWizardStep(optionalsStep);
		}

		optionalSteps = new HashMap<>();
		if(optionalsStep == null) {
			// add settings nodes
			for(OpNode node:nodeWizardList) {
				if(nodeWizardList.isNodeForced(node)) {
					final WizardStep step = createStep(nodeWizardList, node);
					step.setPrevStep(stepIdx-1);
					step.setNextStep(stepIdx+1);
					addWizardStep(step);
					++stepIdx;
					optionalSteps.put(node, step);
				}
			}
		}

		reportDataStep = createReportStep();
		reportDataStep.setPrevStep(stepIdx-1);
		reportDataStep.setNextStep(-1);
		addWizardStep(reportDataStep);

		// setup card layout
		add(stepPanel, BorderLayout.CENTER);

	}

	/**
	 * Return the noun associated with the type of graph
	 * executed in the wizard.  The first element of the
	 * {@link Tuple} is the singleton version, while the
	 * second element is the plural.  String should be
	 * returned as all lower case.
	 *
	 * @return Tuple<String, String>
	 */
	public Tuple<String, String> getNoun() {
		return new Tuple<>("macro", "macros");
	}

	public MultiBufferPanel getBufferPanel() {
		return this.bufferPanel;
	}

	public OpGraph getGraph() {
		return this.graph;
	}

	public Processor getProcessor() {
		return processor;
	}
	
	public void setProcessor(Processor processor) {
		this.processor = processor;
		this.graph = processor.getGraph();
	}

	public WizardExtension getWizardExtension() {
		return this.graph.getExtension(WizardExtension.class);
	}
	
	private BufferPanel getLogBuffer() {
		if(!bufferPanel.getBufferNames().contains("Log")) {
			bufferPanel.createBuffer("Log");
		}
		return bufferPanel.getBuffer("Log");
	}
	
	private class QueryNodeListener implements PropertyChangeListener {
		
		private QueryNode qn;
		
		public QueryNodeListener(QueryNode qn) {
			this.qn = qn;
		}

		private int lastPrintedProgress = 0;
		private QueryTask currentTask = null;
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			StringBuffer buffer = new StringBuffer();
			if(e.getPropertyName().equals("session")) {
				// start new session query
				Session session = (Session)e.getNewValue();
				buffer.append(session.getCorpus()).append('.').append(session.getName());
			} else if(e.getPropertyName().equals("task")) {
				// task status
				TaskStatus ts = (TaskStatus)e.getNewValue();
				if(ts == TaskStatus.FINISHED) {
					int currentP = lastPrintedProgress;
					while((currentP += 5) <= 100) {
						buffer.append('*');
					}
					buffer.append(currentTask.getResultSet().size());
					buffer.append("\t\n");
					lastPrintedProgress = 0;
				} else if (ts == TaskStatus.ERROR) {
					buffer.append("\tError\n");
					qn.removePropertyChangeListener(this);
				} else if(ts == TaskStatus.TERMINATED) {
					buffer.append("\tTerminated\n");
					qn.removePropertyChangeListener(this);
				}
			} else if(e.getPropertyName().equals(PhonTask.PROGRESS_PROP)) {
				// query progress
				int progress = (int)Math.ceil((float)e.getNewValue());
				if(progress % 5 == 0 && progress != lastPrintedProgress) {
					int currentP = lastPrintedProgress;
					while((currentP += 5) <= progress) {
						buffer.append('*');
					}
					lastPrintedProgress = progress;						
				}
			} else if(e.getPropertyName().equals("queryTask")) {
				currentTask = (QueryTask)e.getNewValue();
			} else if(e.getPropertyName().equals("numCompleted")) {
				// only called at end of query
				qn.removePropertyChangeListener(this);
			}
			
			try (PrintWriter out = new PrintWriter(new OutputStreamWriter(getLogBuffer().getLogBuffer().getStdOutStream()))) {
				out.print(buffer.toString());
				out.flush();
			}
		}
		
	}

	private final ProcessorListener processorListener = new CurrentNodeProcessorListener();
	private class CurrentNodeProcessorListener implements ProcessorListener {

		@Override
		public void processorEvent(ProcessorEvent pe) {
			if(pe.getType() == ProcessorEvent.Type.BEGIN_NODE) {
				if(!running) executionStarted(pe);
				
				final String nodeName = pe.getNode().getName();
				if(pe.getNode() instanceof MacroNode) {
					MacroNode mn = (MacroNode)pe.getNode();
					mn.addProcessorListener(this);
				}
				SwingUtilities.invokeLater( () -> {
					if(!busyLabel.isBusy()) {
						busyLabel.setBusy(true);
					}
					statusLabel.setText(nodeName + "...");
					btnBack.setEnabled(false);
				});
				
				if(pe.getNode() instanceof QueryNode) {
					QueryNode qn = (QueryNode)pe.getNode();
					qn.addPropertyChangeListener(new QueryNodeListener(qn));
					try (PrintWriter out = new PrintWriter(new OutputStreamWriter(getLogBuffer().getLogBuffer().getStdOutStream()))) {
						out.println(pe.getNode().getName());
						out.flush();
					}
				}
				
			} else if(pe.getType() == ProcessorEvent.Type.FINISH_NODE) {
				if(pe.getNode() instanceof ReportSectionNode) {
					ReportSectionNode node = (ReportSectionNode)pe.getNode();
					final OpContext ctx = pe.getProcessor().getContext().getChildContext(node);
					if(ctx != null && ctx.get(node.sectionNodeOutput) != null) {
						ReportTreeNode treeNode = (ReportTreeNode)ctx.get(node.sectionNodeOutput);
						try (PrintWriter out = new PrintWriter(new OutputStreamWriter(getLogBuffer().getLogBuffer().getStdOutStream()))) {
							out.println("New report section: " + treeNode.getPath().toString());
							out.flush();
						}						
					}
				}
			}
		}
		
	}

	/**
	 * Called when the processors begins
	 */
	public void executionStarted(ProcessorEvent pe) {
		running = true;
	}

	/**
	 * Called when the processor ends
	 */
	public void executionEnded(ProcessorEvent pe) {
		running = false;
		breadCrumbViewer.setEnabled(true);
		btnCancel.setVisible(false);

		btnRunAgain.setVisible(true);
		getRootPane().setDefaultButton(btnRunAgain);

		btnBack.setEnabled(true);
	}

	public void stopExecution() {
		if(processor != null) {
			processor.stop();
		}
	}
	
	private String getSizeString(long bytes) {
		int kb = 1024;
		int mb = kb * 1024;
		int gb = mb * 1024;
		
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setMaximumFractionDigits(2);
		
		String retVal = bytes + " B";
		if(bytes > gb) {
			double numgbs = (double)bytes/(double)gb;
			retVal = nf.format(numgbs) + " GB";
		} else if(bytes > mb) {
			double nummbs = (double)bytes/(double)mb;
			retVal = nf.format(nummbs) + " MB";
		} else if(bytes > kb) {
			double numkbs = (double)bytes/(double)kb;
			retVal = nf.format(numkbs) + " KB";
		}
		
		return retVal;
	}

	/**
	 * Executes graph. During execution, data generated may be printed
	 * to buffers which are displayed during this stage.  If there
	 * is a buffer named 'Report Template' at the end of execution,
	 * a HTML report is generated using the contents of 'Report Template'
	 * which should be a velocity template.
	 *
	 * @throws ProcessingException
	 */
	public void executeGraph() throws ProcessingException {
		setupContext(processor.getContext());
		processor.reset();
		setupOptionals(processor.getContext());
		setupGlobalOptions(processor.getContext());
		processor.addProcessorListener(processorListener);
		processor.addProcessorListener( (pe) -> {
			if(pe.getType() == ProcessorEvent.Type.COMPLETE) {
				executionEnded(pe);
			}
		});

		reportStartTime = System.currentTimeMillis();
		reportTimer = new Timer(500, (e) -> {
			final long currentTime = System.currentTimeMillis();
			final long elapsedTime = currentTime - reportStartTime;

			final String title = String.format("Report (%s)", MsFormatter.msToDisplayString(elapsedTime).substring(0, 6));
			reportTitledPanel.setTitle(title);
		});
		reportTimer.start();

		try {
			SwingUtilities.invokeLater( () -> {
				btnCancel.setVisible(true);
				btnRunAgain.setVisible(false);
				breadCrumbViewer.setEnabled(false);
			});

			reportSaved = false;
			processor.stepAll();
			
			final ReportTree reportTree = (ReportTree)processor.getContext().get(NewReportNode.REPORT_TREE_KEY);
			if(PrefHelper.getBoolean("phon.debug", false) && reportTree != null) {
				final BufferPanel reportTemplateBuffer = bufferPanel.createBuffer("Report Template");

				final OutputStream os = reportTemplateBuffer.getLogBuffer().getStdOutStream();
				final PrintWriter writer = new PrintWriter(os);
				writer.write(reportTree.getReportTemplate() + "\n");
				writer.flush();
				writer.close();
			}

			// create temp file
			File tempFile = null;
			try {
				tempFile = File.createTempFile("phon", "report.html");
				tempFile.deleteOnExit();

				try(final FileOutputStream fout = new FileOutputStream(tempFile)) {
					final NodeWizardReportGenerator reportGenerator =
							new NodeWizardReportGenerator(this, reportTree.getReportTemplate(), fout);

					try (PrintWriter out = new PrintWriter(new OutputStreamWriter(getLogBuffer().getLogBuffer().getStdOutStream()))) {
						out.print("Generating report...");
						out.flush();
					}
					
					SwingUtilities.invokeLater(() -> {
						statusLabel.setText("Generating report...");
					});
					
					reportGenerator.generateReport();
					try (PrintWriter out = new PrintWriter(new OutputStreamWriter(getLogBuffer().getLogBuffer().getStdOutStream()))) {
						FileInputStream fin = new FileInputStream(tempFile);
						out.println(getSizeString(fin.getChannel().size()));
						out.flush();
						fin.close();
					}
				} catch (IOException | NodeWizardReportException e) {
					// throw to outer try
					throw new IOException(e);
				}

				// create buffer
				final String reportURL = tempFile.toURI().toString();
				final AtomicReference<BufferPanel> bufferPanelRef = new AtomicReference<BufferPanel>();
				try {
					SwingUtilities.invokeAndWait( () -> { 
						bufferPanelRef.getAndSet(bufferPanel.createBuffer("Report")); 
						bufferPanelRef.get().showHtml(false);
					});
					final BufferPanel reportBufferPanel = bufferPanelRef.get();
					final WebView webView = reportBufferPanel.getWebView();
					final HashMap<String, DefaultTableDataSource> tableMap = new HashMap<>();
					searchForTables(reportTree.getRoot(), tableMap);
					reportBufferPanel.setUserObject(reportTree);
					javafx.application.Platform.runLater(() -> {
						
						webView.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

							@Override
							public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
								if(newValue == State.SUCCEEDED) {
									final JSObject window = (JSObject) webView.getEngine().executeScript("window");
									window.setMember("project", getExtension(Project.class));
									window.setMember("buffers", bufferPanel);
									window.setMember("reportTree", reportTree);
									
									window.setMember("tableMap", tableMap);
									
									window.setMember("app", webViewInterface);

									// call functions to display app-specific UI elements
									webView.getEngine().executeScript("addMenuButtons()");
								}
							}

						});

						webView.getEngine().load(reportURL);
					});
				} catch (InterruptedException | InvocationTargetException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);

				final BufferPanel errPanel = getLogBuffer();
				errPanel.getLogBuffer().setForeground(Color.red);
				final PrintWriter writer = new PrintWriter(errPanel.getLogBuffer().getStdErrStream());
				e.printStackTrace(writer);
				writer.flush();
				writer.close();
			}

			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(false);
				reportTimer.stop();
				statusLabel.setText("");
				btnBack.setEnabled(true);

				breadCrumbViewer.setEnabled(true);

			});
		} catch (ProcessingException pe) {
			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(false);
				reportTimer.stop();
				statusLabel.setText(pe.getLocalizedMessage());

				final BufferPanel errPanel = getLogBuffer();
				errPanel.getLogBuffer().setForeground(Color.red);
				final PrintWriter writer = new PrintWriter(errPanel.getLogBuffer().getStdErrStream());

				if(pe.getContext().getCurrentNode() != null) {
					final List<OpNode> nodePath = graph.getNodePath(pe.getContext().getCurrentNode().getId());
					final String txt =
							nodePath.stream().map(OpNode::getName).collect(Collectors.joining("/"));
					writer.println(txt + " (" + pe.getContext().getCurrentNode().getId() + ")");
					// reset canceled status for node
					pe.getContext().getCurrentNode().setCanceled(false);
				}

				pe.printStackTrace(writer);
				writer.flush();
				writer.close();

				reportTimer.stop();

				executionEnded(new ProcessorEvent());
			});
			throw pe;
		}
	}

	public String loadDefaultReport() {
		final StringBuffer buffer = new StringBuffer();
		final InputStream defaultReportStream =
				getClass().getClassLoader().getResourceAsStream(DEFAULT_REPORT_FILE);
		if(defaultReportStream != null) {
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(defaultReportStream, "UTF-8"))) {
				String line = null;
				while((line = reader.readLine()) != null) {
					buffer.append(line).append("\n");
				}
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		} else {
			LOGGER.log(Level.WARNING, "Not found " + DEFAULT_REPORT_FILE, new FileNotFoundException(DEFAULT_REPORT_FILE));
		}
		return buffer.toString();
	}

	/**
	 * Setup report context variables for
	 * graph, buffers, etc.
	 *
	 * @param ctx
	 */
	public void setupReportContext(NodeWizardReportContext ctx) {
		final Map<String, String> buffers = new HashMap<>();
		final Map<String, DefaultTableDataSource> tables = new HashMap<>();
		
		final ReportTree reportTree = (ReportTree)processor.getContext().get(NewReportNode.REPORT_TREE_KEY);
		searchForTables(reportTree.getRoot(), tables);

		ctx.put("Class", Class.class);
		ctx.put("FormatterUtil", FormatterUtil.class);
		ctx.put("Math", new MathTool());
		ctx.put("ParticipantHistory", ParticipantHistory.class);

		ctx.put("graph", getGraph());
		ctx.put("bufferNames", bufferPanel.getBufferNames());
		ctx.put("buffers", buffers);
		ctx.put("tableMap", tables);
	}
	
	public void searchForTables(ReportTreeNode node, Map<String, DefaultTableDataSource> tableMap) {
		if(node instanceof TableNode) {
			final TableNode tableNode = (TableNode)node;
			tableMap.put(tableNode.getPath().toString(), (DefaultTableDataSource)tableNode.getTable());
		}
		
		for(ReportTreeNode child:node.getChildren()) {
			searchForTables(child, tableMap);
		}
	}

	public WizardOptionalsCheckboxTree getOptionalsTree() {
		return this.optionalsTree;
	}

	protected void setupContext(OpContext ctx) {
		ctx.put(BufferNodeConstants.BUFFER_CONTEXT_KEY, bufferPanel);
		ReportTree reportTree = new ReportTree(new SectionHeaderNode(getWizardExtension().getWizardTitle()));
		ctx.put(NewReportNode.REPORT_TREE_KEY, reportTree);
	}

	protected void setupOptionals(OpContext ctx) {
		if(optionalsTree == null) return;
		for(OpNode node:getWizardExtension().getOptionalNodes()) {
			final TreePath nodePath = optionalsTree.getNodePath(node);
			boolean enabled = 
					(optionalsTree != null ? optionalsTree.isPathChecked(nodePath) || optionalsTree.isPathPartiallyChecked(nodePath) : true);

			OpContext nodeCtx = ctx;
			for(int i = 1; i < nodePath.getPathCount(); i++) {
				CheckedOpNode treeNode = (CheckedOpNode)nodePath.getPathComponent(i);
				nodeCtx = nodeCtx.getChildContext(treeNode.getNode());
			}
			nodeCtx.put(OpNode.ENABLED_FIELD, Boolean.valueOf(enabled));
		}
	}

	protected void setupGlobalOptions(OpContext ctx) {
		if(globalOptionsPanel.isUseGlobalCaseSensitive())
			ctx.put(CASE_SENSITIVE_GLOBAL_OPTION, globalOptionsPanel.isCaseSensitive());
		if(globalOptionsPanel.isUseGlobalIgnoreDiacritics())
			ctx.put(IGNORE_DIACRITICS_GLOBAL_OPTION, globalOptionsPanel.isIgnoreDiacritics());
		if(globalOptionsPanel.isUseInventoryGrouping())
			ctx.put(INVENTORY_GROUPING_GLOBAL_OPTION, globalOptionsPanel.getInventoryGrouping());
	}

	protected WizardStep createStep(WizardExtension ext, OpNode node) {
		final NodeSettings settings = node.getExtension(NodeSettings.class);
		if(settings != null) {
			try {
				final Component comp = settings.getComponent(null);

				final WizardStep step = new WizardStep() {

					@Override
					public boolean validateStep() {
						if(comp instanceof ScriptPanel) {
							// validate settings
							return ((ScriptPanel)comp).checkParams();
						} else {
							return super.validateStep();
						}
					}

				};
				final BorderLayout layout = new BorderLayout();
				step.setLayout(layout);

				final TitledPanel panel = new TitledPanel(ext.getNodeTitle(node), 
						(comp instanceof ScriptPanel ? comp : new JScrollPane(comp)) );
				if(globalOptionsPanel.getParent() == null)
					panel.setRightDecoration(globalOptionsPanel);
				
				step.add(panel, BorderLayout.CENTER);

				step.setTitle(ext.getNodeTitle(node));
				step.putExtension(OpNode.class, node);

				return step;
			} catch (NullPointerException e) {
				// we have no document, this may cause an exception
				// depending on implementation - ignore it.
			}
		}
		return null;
	}

	protected WizardStep createIntroStep(String title, String message) {
		final WizardStep retVal = new WizardStep();

		retVal.setLayout(new BorderLayout());

		final JEditorPane editorPane = createHTMLPane();
		editorPane.setText(message);

		editorPane.scrollRectToVisible(new Rectangle(0, 0, 0, 0));

		final TitledPanel stepTitle =
				new TitledPanel(title, new JScrollPane(editorPane));
		retVal.add(stepTitle, BorderLayout.CENTER);

		return retVal;
	}

	private JEditorPane createHTMLPane() {
		final HTMLEditorKit editorKit = new HTMLEditorKit();
		final StyleSheet styleSheet = editorKit.getStyleSheet();
		final URL cssURL = getClass().getClassLoader().getResource("ca/phon/app/opgraph/wizard/wizard.css");
		if(cssURL != null) {
			try {
				styleSheet.loadRules(
						new InputStreamReader(cssURL.openStream(), "UTF-8"), cssURL);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}

		final JEditorPane editorPane = new JEditorPane("text/html", "");
		editorPane.setEditorKit(editorKit);
		editorPane.setEditable(false);
		return editorPane;
	}

	protected WizardStep createOptionalsStep() {
		final WizardStep retVal = new WizardStep();
		retVal.setTitle("Select " + getNoun().getObj2());

		retVal.setLayout(new BorderLayout());

		optionalsTree = new WizardOptionalsCheckboxTree(getWizardExtension());
		optionalsTree.addMouseListener(new OptionalsContextHandler());
		for(OpNode optionalNode:getWizardExtension().getOptionalNodes()) {
			if(getWizardExtension().getOptionalNodeDefault(optionalNode)) {
				optionalsTree.checkNode(optionalNode);
			}
		}

		final TitledPanel panel = new TitledPanel("Select " + getNoun().getObj2(), new JScrollPane(optionalsTree));
		retVal.add(panel, BorderLayout.CENTER);

		return retVal;
	}

	protected WizardStep createReportStep() {
		final WizardStep retVal = new WizardStep();
		retVal.setTitle("Report");

		retVal.setLayout(new BorderLayout());

		final MultiBufferPanel bufferPanel = getBufferPanel();
		reportTitledPanel = new TitledPanel("Report", bufferPanel);
		reportTitledPanel.setLeftDecoration(busyLabel);

		final JPanel currentNodePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		currentNodePanel.add(statusLabel);
		currentNodePanel.setOpaque(false);
		statusLabel.setOpaque(false);
		statusLabel.setForeground(UIManager.getColor("titledpanel.foreground"));
		reportTitledPanel.setRightDecoration(currentNodePanel);

		retVal.add(reportTitledPanel, BorderLayout.CENTER);

		return retVal;
	}

	@Override
	public void gotoStep(int step) {
		super.gotoStep(step);

		if(!inInit && getCurrentStep() == reportDataStep) {
			if(bufferPanel.getBufferNames().size() > 0) {
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setTitle("Re-run " + getNoun().getObj1());
				props.setHeader("Re-run " + getNoun().getObj1());
				props.setMessage("Clear results and re-run " + getNoun().getObj1() + ".");
				props.setOptions(MessageDialogProperties.yesNoOptions);
				props.setRunAsync(true);
				props.setParentWindow(this);

				props.setListener( (e) -> {
					final int retVal = e.getDialogResult();

					if(retVal == 1) return;

					SwingUtilities.invokeLater( () -> {
						bufferPanel.closeAllBuffers();
						PhonWorker.getInstance().invokeLater( () -> executeGraph() );
					});

				});
				NativeDialogs.showMessageDialog(props);
			} else {
				PhonWorker.getInstance().invokeLater( () -> executeGraph() );
			}
		}
	}

	public void gotoReport() {
		int lastStep = -1;
		do {
			lastStep = getCurrentStepIndex();
			next();
		} while(getCurrentStepIndex() != lastStep && getCurrentStep() != reportDataStep);
	}

	@Override
	protected void next() {
		if(getCurrentStep() == optionalsStep) {
			// remove all current optionals
			for(OpNode node:optionalSteps.keySet()) {
				final WizardStep step = optionalSteps.get(node);
				removeWizardStep(step);
			}
			removeWizardStep(reportDataStep);

			// add settings nodes, excluding those
			// disabled in the optionals stage
			final WizardExtension nodeWizardList = getWizardExtension();
			int stepIdx = super.numberOfSteps();
			for(OpNode node:nodeWizardList) {
				// create tree path for optionals tree
				final List<OpNode> graphPath = getGraph().getNodePath(node.getId());

				while(graphPath.size() > 0) {
					final TreePath treePath = optionalsTree.graphPathToTreePath(graphPath);

					if(treePath == null) {
						// path not found, try parent
						graphPath.remove(graphPath.size()-1);
					} else {
						boolean isEnabled = optionalsTree.isPathChecked(treePath);

						if(nodeWizardList.isNodeForced(node) && isEnabled) {
							final WizardStep step = createStep(nodeWizardList, node);
							step.setPrevStep(stepIdx-1);
							step.setNextStep(stepIdx+1);
							addWizardStep(step);
							++stepIdx;
							optionalSteps.put(node, step);
						}

						// found but not enabled
						break;
					}
				}
			}

			addWizardStep(reportDataStep);
		}
		super.next();
	}

	@Override
	protected void cancel() {
		if(running) {
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setParentWindow(this);
			props.setTitle("Close");
			props.setHeader("Stop execution");
			props.setMessage("Stop execution and close?");
			props.setOptions(new String[] { "Cancel", "Stop", "Stop and Close"});
			props.setDefaultOption("Cancel");
			props.setRunAsync(true);
			props.setListener( (e) -> {
				final int retVal = e.getDialogResult();

				if(retVal == 0) return;
				stopExecution();

				if(retVal == 2) {
					SwingUtilities.invokeLater( () -> _cancel() );
				}
			});

			NativeDialogs.showMessageDialog(props);
		} else {
			super.cancel();
		}
	}

	private void _cancel() {
		super.cancel();
	}

	public class WebViewInterface {
		
		public void log(String log) {
			java.lang.System.out.println(log);
		}

		public void openSession(String sessionName) {
			final SessionPath sessionPath = new SessionPath(sessionName);

			// call open session module
			final EntryPointArgs args = new EntryPointArgs();
			args.put(EntryPointArgs.PROJECT_OBJECT, getExtension(Project.class));
			args.put(EntryPointArgs.CORPUS_NAME, sessionPath.getCorpus());
			args.put(EntryPointArgs.SESSION_NAME, sessionPath.getSession());

			PluginEntryPointRunner.executePluginInBackground(SessionEditorEP.EP_NAME, args);
		}

		public void openSessionAtRecord(String sessionName, int recordIndex) {
			final SessionPath sessionPath = new SessionPath(sessionName);

			// call open session module
			final EntryPointArgs args = new EntryPointArgs();
			args.put(EntryPointArgs.PROJECT_OBJECT, getExtension(Project.class));
			args.put(EntryPointArgs.CORPUS_NAME, sessionPath.getCorpus());
			args.put(EntryPointArgs.SESSION_NAME, sessionPath.getSession());
			args.put(SessionEditorEP.RECORD_INDEX_PROPERY, recordIndex);

			PluginEntryPointRunner.executePluginInBackground(SessionEditorEP.EP_NAME, args);
		}
		
		public void createTableBuffer(String tableId, DefaultTableDataSource table) {
			final PrintBufferNode printBuffer = new PrintBufferNode();
			printBuffer.setShowTable(true);
			printBuffer.setShowBuffer(false);
			
			final OpContext ctx = new OpContext();
			ctx.put("_buffers", bufferPanel);
			ctx.put("buffer", tableId);
			ctx.put("data", table);
			
			try {
				printBuffer.operate(ctx);
			} catch (ProcessingException pe) {
				LogUtil.severe(pe);
			}
		}
		
		public void showTable(String tableId, DefaultTableDataSource table) {
			SwingUtilities.invokeLater( () -> {
				if(!bufferPanel.getBufferNames().contains(tableId)) {
					createTableBuffer(tableId, table);
				}
				bufferPanel.selectBuffer(tableId);
			});
		}
		
		public void saveTableAsCSV(String tableId, DefaultTableDataSource table) {
			SwingUtilities.invokeLater(() -> {
				if(!bufferPanel.getBufferNames().contains(tableId)) {
					createTableBuffer(tableId, table);
				}
				bufferPanel.saveAsCSV(tableId);
			});
		}
		
		public void saveTableAsWorkbook(String tableId, DefaultTableDataSource table) {
			SwingUtilities.invokeLater( () -> {
				if(!bufferPanel.getBufferNames().contains(tableId)) {
					createTableBuffer(tableId, table);
				}
				bufferPanel.saveAsWorkbook(tableId);
			});
		}

		public void onHighlightResultValue(DefaultTableDataSource tableModel, int row, String columnName) {
			if(row < 0 || row >= tableModel.getRowCount()) return;

			final Object[] rowData = tableModel.getRow(row);

			final int sessionCol = tableModel.getColumnIndex("Session");
			if(sessionCol < 0) return;
			final String sessionName = rowData[sessionCol].toString();

			final int resultCol = tableModel.getColumnIndex("Result");
			if(resultCol < 0) return;

			final int dataCol = tableModel.getColumnIndex(columnName);
			if(dataCol < 0) return;

			final Result result = (Result) rowData[resultCol];
			if(result == null) return;

			// create a temporary result object
			final QueryFactory factory = new XMLQueryFactory();
			final Result tempResult = factory.createResult();
			tempResult.setRecordIndex(result.getRecordIndex());
			tempResult.setExcluded(result.isExcluded());
			tempResult.setSchema(result.getSchema());

			// find result value using columnName - which should be the tier name
			for(int i = 0; i < result.getNumberOfResultValues(); i++) {
				final ResultValue rv = result.getResultValue(i);
				if(rv.getName().equals(columnName)) {
					tempResult.addResultValue(rv);
				}
			}

			final SessionPath sessionPath = new SessionPath(sessionName);

			// call open session module
			final EntryPointArgs args = new EntryPointArgs();
			args.put(EntryPointArgs.PROJECT_OBJECT, getExtension(Project.class));
			args.put(EntryPointArgs.CORPUS_NAME, sessionPath.getCorpus());
			args.put(EntryPointArgs.SESSION_NAME, sessionPath.getSession());
			args.put(SessionEditorEP.RECORD_INDEX_PROPERY, result.getRecordIndex());
			args.put(SessionEditorEP.RESULT_VALUES_PROPERTY, new Result[] { tempResult });

			PluginEntryPointRunner.executePluginInBackground(SessionEditorEP.EP_NAME, args);
		}

	}

	private class ReportReader extends SwingWorker<String, String> {

		private BufferPanel panel;

		private File file;

		private PrintWriter printer;

		public ReportReader(BufferPanel panel, File reportFile) {
			super();
			this.panel = panel;
			this.file = reportFile;
		}

		@Override
		protected String doInBackground() throws Exception {
			final BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = null;
			final StringBuffer sb = new StringBuffer();
			while((line = in.readLine()) != null) {
				publish(line, "\n");
				sb.append(line).append("\n");
			}
			in.close();
			return sb.toString();
		}

		@Override
		protected void process(List<String> chunks) {
			if(this.printer == null) {
				this.printer = new PrintWriter(panel.getLogBuffer().getStdOutStream());
			}
			for(String chunk:chunks) {
				printer.write(chunk);
			}
			printer.flush();
		}

		@Override
		protected void done() {
			panel.showHtml();
			reportTimer.stop();
		}

	}

	private class OptionalsContextHandler extends MouseInputAdapter {

		@Override
		public void mousePressed(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showContextMenu(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if(e.isPopupTrigger()) {
				showContextMenu(e);
			}
		}

		private void showContextMenu(MouseEvent e) {
			int row = optionalsTree.getRowForLocation(e.getX(), e.getY());
			if(row < 0) return;
			final JPopupMenu menu = new JPopupMenu();
			final MenuBuilder menuBuilder = new MenuBuilder(menu);

			final TreePath path = optionalsTree.getPathForRow(row);
			if(!(path.getLastPathComponent() instanceof CheckedOpNode)) return;
			final CheckedOpNode node = (CheckedOpNode)path.getLastPathComponent();
			final OpNode opNode = node.getNode();

			final PhonUIAction checkNodeAction =
					new PhonUIAction(optionalsTree,
							(optionalsTree.isPathChecked(path) ? "removeCheckingPath" : "addCheckingPath"),
							path);
			String name = (optionalsTree.isPathChecked(path) ? "Uncheck " : "Check ") +  opNode.getName();
			checkNodeAction.putValue(PhonUIAction.NAME, name);
			menuBuilder.addItem(".", checkNodeAction);

			final PhonUIAction showOptionsAction =
					new PhonUIAction(NodeWizard.this, "showAdvancedSettings", path);
			showOptionsAction.putValue(PhonUIAction.NAME, "Show settings");
			showOptionsAction.putValue(PhonUIAction.SMALL_ICON,
					IconManager.getInstance().getIcon("actions/settings-black", IconSize.SMALL));
			menuBuilder.addItem(".", showOptionsAction);

			menu.show(optionalsTree, e.getX(), e.getY());
		}

	}

}
