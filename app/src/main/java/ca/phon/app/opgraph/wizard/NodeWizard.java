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
package ca.phon.app.opgraph.wizard;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.regex.*;
import java.util.stream.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.text.html.*;
import javax.swing.tree.*;

import ca.phon.app.script.ScriptPanel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;
import org.apache.velocity.tools.generic.*;
import org.jdesktop.swingx.*;

import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.dom.*;
import com.teamdev.jxbrowser.chromium.events.*;
import com.teamdev.jxbrowser.chromium.swing.*;

import ca.phon.app.html.*;
import ca.phon.app.log.*;
import ca.phon.app.log.actions.*;
import ca.phon.app.modules.*;
import ca.phon.app.opgraph.*;
import ca.phon.app.opgraph.nodes.log.*;
import ca.phon.app.opgraph.nodes.query.*;
import ca.phon.app.opgraph.nodes.report.*;
import ca.phon.app.opgraph.report.tree.*;
import ca.phon.app.opgraph.wizard.WizardOptionalsCheckboxTree.*;
import ca.phon.app.opgraph.wizard.actions.*;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToFolderAction.*;
import ca.phon.app.opgraph.wizard.actions.SaveTablesToWorkbookAction;
import ca.phon.app.session.editor.*;
import ca.phon.formatter.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.app.extensions.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.nodes.general.*;
import ca.phon.plugin.*;
import ca.phon.project.*;
import ca.phon.query.db.*;
import ca.phon.query.db.xml.*;
import ca.phon.query.report.datasource.*;
import ca.phon.query.script.*;
import ca.phon.session.*;
import ca.phon.ui.*;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.*;
import ca.phon.ui.fonts.*;
import ca.phon.ui.jbreadcrumb.*;
import ca.phon.ui.menu.*;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.wizard.*;
import ca.phon.util.*;
import ca.phon.util.icons.*;
import ca.phon.worker.*;
import ca.phon.worker.PhonTask.*;

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
public class NodeWizard extends BreadcrumbWizardFrame {

	private static final long serialVersionUID = -652423592288338133L;

	private final static String DEFAULT_REPORT_FILE = "ca/phon/app/opgraph/wizard/DefaultReport.vm";

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(NodeWizard.class.getName());

	private Processor processor;

	private OpGraph graph;

	private WizardMultiBufferPanel bufferPanel;

	private JXBusyLabel loadingLabel;
	private JXBusyLabel busyLabel;

	private JLabel statusLabel;

	protected TitledPanel reportTitledPanel;
	protected WizardStep reportDataStep;
	private Timer reportTimer;
	private long reportStartTime;

	protected WizardStep optionalsStep;

	private WizardOptionalsCheckboxTree optionalsTree;

	private Map<OpNode, WizardStep> optionalSteps;

	protected OverrideParameterPanel globalOptionsPanel;
	public final static String CASE_SENSITIVE_GLOBAL_OPTION = GlobalParameter.CASE_SENSITIVE.getParamId();
	public final static String INVENTORY_GROUPING_GLOBAL_OPTION = GlobalParameter.INVENTORY_GROUPING_COLUMN.getParamId();
	public final static String IGNORE_DIACRITICS_GLOBAL_OPTION = GlobalParameter.IGNORE_DIACRITICS.getParamId();
	public final static String ONLYOREXCEPT_GLOBAL_OPTION = GlobalParameter.ONLY_OR_EXCEPT.getParamId();
	public final static String SELECTED_DIACRITICS_GLOBAL_OPTION = GlobalParameter.SELECTED_DIACRITICS.getParamId();
	protected DropDownButton overridesButton;
	
	protected boolean inInit = true;

	private volatile boolean running = false;
	private boolean reportSaved = false;

	protected BreadcrumbButton btnStop;
	protected BreadcrumbButton btnRunAgain;

	private final WebViewInterface webViewInterface = new WebViewInterface();

	public NodeWizard(String title) {
		super(title);
		setWindowName(title);
		
		init();
		inInit = false;
	}
	
	public NodeWizard(String title, Processor processor, OpGraph graph) {
		super(title);
		setWindowName(title);

		this.processor = processor;
		this.graph = graph;
		init();
		setupWizardSteps();
		inInit = false;
	}
	
	public void loadGraph(OpGraph graph) {
		this.graph = graph;
		processor = new Processor(graph);
		
		SwingUtilities.invokeLater( () -> {
			setupWizardSteps();
			updateBreadcrumbButtons();
		});
	}
	
	public void loadGraph(URL graphURL) throws IOException {
		OpGraph graph = OpgraphIO.read(graphURL.openStream());
		loadGraph(graph);
	}
	
	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);

		final MenuBuilder builder = new MenuBuilder(menuBar);
		
		final JMenu reportMenu = builder.addMenu(".@Analysis", "Report");
		reportMenu.addMenuListener(new MenuListener() {
			
			@Override
			public void menuSelected(MenuEvent e) {
				reportMenu.removeAll();
				setupReportMenu(new MenuBuilder(reportMenu));
			}
			
			@Override
			public void menuDeselected(MenuEvent e) {
			}
			
			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
		
		if(PrefHelper.getBoolean("phon.debug", false)) {
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
	}
	
	public void setupReportMenu(MenuBuilder builder) {
		final boolean hasReport = reportBufferAvailable();		
		final BufferPanel reportBuffer = bufferPanel.getBuffer("Report");
		
		if(hasReport) {
			if(PrefHelper.getBoolean("phon.debug", false)) {
				final PhonUIAction<Void> debugAct = PhonUIAction.runnable(
					(reportBuffer.isShowingHtmlDebug() ? reportBuffer::hideHtmlDebug : reportBuffer::showHtmlDebug)
				);
				debugAct.putValue(PhonUIAction.NAME, "Debug");
				debugAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show html debug frame");
				debugAct.putValue(PhonUIAction.SELECTED_KEY, reportBuffer.isShowingHtmlDebug());
				builder.addItem(".", new JCheckBoxMenuItem(debugAct));
				
				final PhonUIAction<Void> reloadAct = PhonUIAction.runnable(reportBuffer.getBrowser()::reload);
				reloadAct.putValue(PhonUIAction.NAME, "Reload");
				reloadAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reload report");
				builder.addItem(".", reloadAct);
				builder.addSeparator(".", "debug_sep");
			}
			
			final PhonUIAction<Void> zoomInAct = PhonUIAction.runnable(this::onZoomIn);
			zoomInAct.putValue(PhonUIAction.NAME, "Zoom in");
			zoomInAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Increase zoom level");
			builder.addItem(".", zoomInAct);
			
			final PhonUIAction<Void> zoomOutAct = PhonUIAction.runnable(this::onZoomOut);
			zoomOutAct.putValue(PhonUIAction.NAME, "Zoom out");
			zoomOutAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Decrease zoom level");
			builder.addItem(".", zoomOutAct);
			
			final PhonUIAction<Void> resetZoomAct = PhonUIAction.runnable(this::onZoomReset);
			resetZoomAct.putValue(PhonUIAction.NAME, "Reset zoom");
			resetZoomAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset zoom level");
			builder.addItem(".", resetZoomAct);
			
			builder.addSeparator(".", "zoom_actions");
			
			if(Desktop.isDesktopSupported() && hasReport) {
				// bug on macos using Browser.getURL() for some reason when opening window menu
				// use javascript interface to get URL instead
				final AtomicReference<String> reportTmpURLRef = new AtomicReference<>();
				if(SwingUtilities.isEventDispatchThread()) {
					CountDownLatch latch = new CountDownLatch(1);
					PhonWorker.getInstance().invokeLater( () -> {
						JSValue urlVal = reportBuffer.getBrowser().executeJavaScriptAndReturnValue("window.location.href");
						reportTmpURLRef.set(urlVal.getStringValue());
						latch.countDown();
					});
					try {
						latch.await(2, TimeUnit.SECONDS);
					} catch (InterruptedException e1) {
						LogUtil.severe(e1);
					}
				} else {
					reportTmpURLRef.set(reportBuffer.getBrowser().getURL());
				}
				URI uri = URI.create(reportTmpURLRef.get());
				
				final PhonUIAction<Void> openInBrowserAct = PhonUIAction.runnable(() -> {
					try {
						Desktop.getDesktop().browse(uri);
					} catch (IOException e) {
						Toolkit.getDefaultToolkit().beep();
						LogUtil.warning(e);
					}
				});
				openInBrowserAct.putValue(PhonUIAction.NAME, "Open report in browser");
				openInBrowserAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open report in system web browser");
				builder.addItem(".", openInBrowserAct);
				builder.addSeparator(".", "open_sep");
			}
			
			final Optional<ReportTree> reportTree = getCurrentReportTree();
			if(reportTree.isPresent()) {
				JMenu reportTreeMenu = builder.addMenu(".", "Go to");
				reportTreeMenu.addMenuListener(new MenuListener() {
					
					@Override
					public void menuSelected(MenuEvent e) {
						reportTreeMenu.removeAll();
						setupReportTreeMenu(new MenuBuilder(reportTreeMenu), reportTree.get());
					}
					
					@Override
					public void menuDeselected(MenuEvent e) {
					}
					
					@Override
					public void menuCanceled(MenuEvent e) {
					}
					
				});
				builder.addSeparator(".", "report_tree");
			}
		}
		
		// add global options
		if(globalOptionsPanel != null) {
			globalOptionsPanel.setupMenu(builder);
			
			builder.addSeparator(".", "_globalOptions");
		}
				
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
		saveAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
		saveAct.putValue(PhonUIAction.SMALL_ICON,
				IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL));
		
		
		final PhonUIAction<Void> printReportAct = PhonUIAction.runnable(this::onPrintReport);
		printReportAct.putValue(PhonUIAction.NAME, "Print");
		printReportAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Print report");

		builder.addItem(".", runAgainItem);
		builder.addSeparator(".", "_run");
		builder.addItem(".", saveAct).setEnabled(hasReport);
		builder.addSeparator(".", "_save");
		builder.addItem(".", saveTablesToWorkbookAct).setEnabled(hasReport);
		builder.addItem(".", saveTablesExcelAct).setEnabled(hasReport);
		builder.addItem(".", saveTablesCSVAct).setEnabled(hasReport);
		builder.addSeparator(".", "_export");
		builder.addItem(".", printReportAct).setEnabled(hasReport);
	}
	
	public void onZoomIn() {
		if(reportBufferAvailable()) {
			bufferPanel.getCurrentBuffer().onZoomIn();
		}
	}
	
	public void onZoomOut() {
		if(reportBufferAvailable()) {
			bufferPanel.getCurrentBuffer().onZoomOut();
		}
	}
	
	public void onZoomReset() {
		if(reportBufferAvailable()) {
			bufferPanel.getCurrentBuffer().onZoomReset();
		}
	}
	
	private void setupReportTreeMenu(MenuBuilder builder, ReportTree reportTree) {
		int idx = 0;
		for(ReportTreeNode node:reportTree.getRoot()) {
			if(idx++ > 0) builder.addSeparator(".", node.getTitle() + "_separator");
			appendReportTreeNode(builder, node, 1);
		}
	}
	
	private void appendReportTreeNode(MenuBuilder builder, ReportTreeNode node, int headerLevel) {
		if(headerLevel == 1 || node.getChildren().size() == 0) {
			final PhonUIAction<String> act = PhonUIAction.consumer(this::gotoReportSection, node.getPath().toString());
			act.putValue(PhonUIAction.NAME, node.getTitle());
			act.putValue(PhonUIAction.SHORT_DESCRIPTION, String.format("Goto section %s", node.getTitle()));
			JMenuItem itm = new JMenuItem(act);
			if(headerLevel == 1)
				itm.setFont(itm.getFont().deriveFont(Font.BOLD));
			builder.addItem(".", itm);
		} else {
			JMenu subMenu = builder.addMenu(".", node.getTitle());
			MenuBuilder subMenuBuilder = new MenuBuilder(subMenu);
			
			subMenu.addMouseListener(new MouseInputAdapter() {
				
				public void mouseClicked(MouseEvent me) {
					gotoReportSection(node.getPath().toString());
				}
				
			});
			
			builder = subMenuBuilder;
		}
		for(ReportTreeNode cnode:node) {
			appendReportTreeNode(builder, cnode, headerLevel+1);
		}
	}
	
	public void gotoReportSection(String htmlId) {
		if(reportBufferAvailable()) {
			final Browser browser = bufferPanel.getBuffer("Report").getBrowser();
			browser.executeJavaScript(String.format("document.getElementById('%s').scrollIntoView(true)", htmlId));
		}
	}
 
	public void onStop() {
		if(getCurrentStep() == reportDataStep) {
			// logic for stopping query and closing is in the close() method
			close();
		}
	}
	
	@Override
	public void close() {
		if(running) {
			cancel();
		} else {
			if(reportBufferAvailable() && !reportSaved) {
				// ask to save report
				final MessageDialogProperties props = new MessageDialogProperties();
				final String[] options = MessageDialogProperties.okCancelOptions;
				props.setTitle("Close");
				props.setHeader("Close window");
				props.setMessage("Discard results and close window?");
				props.setRunAsync(true);
				props.setOptions(options);
				props.setDefaultOption(options[0]);
				props.setParentWindow(this);
				props.setListener( (e) -> {
					final int result = e.getDialogResult();
					if(result == 0) {
						SwingUtilities.invokeLater( () -> super.close() );
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
		return (bufferPanel != null ? bufferPanel.getBufferNames().contains("Report") : false);
	}
	
	public Optional<ReportTree> getCurrentReportTree() {
		if(reportBufferAvailable()) {
			final Object userObj = bufferPanel.getBuffer("Report").getUserObject();
			if(userObj != null && userObj instanceof ReportTree) {
				return Optional.of((ReportTree)userObj);
			}
		}
		return Optional.empty();
	}
	
	public void onPrintReport() {
		if(reportBufferAvailable()) {
			bufferPanel.getBuffer("Report").getBrowser().print();
		}
	}
	
	private void init() {
		globalOptionsPanel = new OverrideParameterPanel();
		
		bufferPanel = new WizardMultiBufferPanel(this);
		
		btnCancel.setVisible(false);

		btnStop = new BreadcrumbButton();
		btnStop.setFont(FontPreferences.getTitleFont().deriveFont(Font.BOLD));
		btnStop.setText("Stop");
		btnStop.setBackground(Color.red);
		btnStop.setForeground(Color.white);
		btnStop.addActionListener( (e) -> close() );

		btnRunAgain = new BreadcrumbButton();
		btnRunAgain.setFont(FontPreferences.getTitleFont().deriveFont(Font.BOLD));
		btnRunAgain.setText("Run again");
		btnRunAgain.addActionListener( (e) -> gotoStep(super.getStepIndex(reportDataStep)) );

		breadCrumbViewer.setFont(FontPreferences.getTitleFont().deriveFont(Font.BOLD));
		breadCrumbViewer.setBackground(Color.white);
		breadCrumbViewer.setStateBackground(btnNext.getBackground().darker());
		updateBreadcrumbButtons();

		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		busyLabel.getBusyPainter().setHighlightColor(Color.white);
		statusLabel = new JLabel();
		
		final JPopupMenu overridesMenu = new JPopupMenu("Overrides");
		overridesMenu.addPopupMenuListener(new PopupMenuListener() {
			
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				overridesMenu.removeAll();
				globalOptionsPanel.setupMenu(new MenuBuilder(overridesMenu));
			}
			
			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			
			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
			
		});
		
		PhonUIAction<Void> overridesMenuAct = PhonUIAction.runnable(() -> {});
		overridesMenuAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show overrides menu");
		overridesMenuAct.putValue(PhonUIAction.SMALL_ICON, IconManager.getInstance().getIcon("actions/settings-black", IconSize.SMALL));
		overridesMenuAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		overridesMenuAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
		overridesMenuAct.putValue(DropDownButton.BUTTON_POPUP, overridesMenu);
		
		overridesButton = new DropDownButton(overridesMenuAct);
		overridesButton.setOnlyPopup(true);
		overridesButton.setBorderPainted(true);
		overridesButton.setBackground(Color.white);
		overridesButton.setOpaque(true);
		overridesButton.setBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.black),
						BorderFactory.createEmptyBorder(0, 5, 0, 5)));
				
		JPanel topPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		topPanel.add(super.breadcrumbScroller, gbc);
		
		++gbc.gridx;
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.VERTICAL;
		topPanel.add(overridesButton, gbc);
		
		add(topPanel, BorderLayout.NORTH);
		
		loadingLabel = new JXBusyLabel(new Dimension(20, 20));
	}
	
	protected void setupWizardSteps() {
		final WizardExtension nodeWizardList =
				(graph.getExtensionClasses().contains(WizardExtension.class)
				? graph.getExtension(WizardExtension.class)
				: new WizardExtension(getGraph()));
		int stepIdx = numberOfSteps();

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
		//add(stepPanel, BorderLayout.CENTER);
		JPanel stepWithOverrides = new JPanel(new BorderLayout());
		stepWithOverrides.add(globalOptionsPanel, BorderLayout.NORTH);
		stepWithOverrides.add(stepPanel, BorderLayout.CENTER);
		add(stepWithOverrides, BorderLayout.CENTER);
	}
	
	/**
	 * Button state diagram:
	 * <pre>
+-----------------+  Yes   +-------------+  Yes   +----------------------+
| Is report step? +------->+ Is running? +------->+ Button State         |
+-------+---------+        +------+------+        +----------------------+
        |                         |               |                      |
        |                         |               | btnStop: visible     |
        |                         |               | btnNext: hidden      |
        | No                      | No            | btnRunAgain: hidden  |
        |                         |               |                      |
        |                         |               +----------------------+
        v                         v
+-------+--------------+   +------+-------+  Yes  +----------------------+
| Button State         |   | Has report?  +------>+ Button State         |
+----------------------+   +------+-------+       +----------------------+
|                      |          |               |                      |
| btnStop: hidden      |          |               | btnStop: visible     |
| btnNext: visible     |          |               | btnNext:  hidden     |
| btnRunAgain: hidden  |          | No            | btnRunAgain: hidden  |
|                      |          |               |                      |
+----------------------+          |               +----------------------+
                                  |
                                  v
                           +------+---------------+
                           | Button State         |
                           +----------------------+
                           |                      |
                           | btnStop: hidden      |
                           | btnNext: hidden      |
                           | btnRunAgain: visible |
                           |                      |
                           +----------------------+
</pre>
	 */
	@Override
	public void updateBreadcrumbButtons() {
		JButton endBtn = nextButton;
		
		// remove all buttons from breadcrumb
		breadCrumbViewer.remove(nextButton);
		if(btnStop != null)
			breadCrumbViewer.remove(btnStop);
		if(btnRunAgain != null)
			breadCrumbViewer.remove(btnRunAgain);
		if(loadingLabel != null)
			breadCrumbViewer.remove(loadingLabel);
	
		if(breadCrumbViewer.getBreadcrumb().getCurrentState() == reportDataStep) {
			if(running) {
				btnStop.setText("Stop");
				btnStop.setBackground(Color.red);
				btnStop.setForeground(Color.white);
				
				breadCrumbViewer.add(btnStop);
				setBounds(btnStop);
				endBtn = btnStop;
			} else {
				if(reportBufferAvailable()) {
					btnStop.setText("Close window");
					btnStop.setBackground(btnRunAgain.getBackground());
					btnStop.setForeground(Color.black);
					
					breadCrumbViewer.add(btnStop);
					setBounds(btnStop);
					endBtn = btnStop;
				} else if(processor != null && processor.getError() != null) {
					breadCrumbViewer.add(btnRunAgain);
					setBounds(btnRunAgain);
					endBtn = btnRunAgain;
				}
			}
		} else {
			breadCrumbViewer.add(nextButton);
			setBounds(nextButton);
			endBtn = nextButton;
		}
		
		if(numberOfSteps() == 0 || getCurrentStepIndex() < 0
				|| getCurrentStep().getNextStep() < 0) {
			nextButton.setVisible(false);
		} else {
			nextButton.setVisible(true);
		}

		if(getCurrentStep() != reportDataStep)
			getRootPane().setDefaultButton(endBtn);
		else
			getRootPane().setDefaultButton(null);
		
		if(loadingLabel != null) {
			if(graph == null) {
				breadCrumbViewer.add(loadingLabel);
				setBounds(loadingLabel);
				loadingLabel.setBusy(true);
			} else {
				loadingLabel.setBusy(false);
			}
		}

		breadCrumbViewer.revalidate();
		breadCrumbViewer.scrollRectToVisible(endBtn.getBounds());
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
				if(!running) executionStarted();
				
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
	protected void executionStarted() {
		running = true;
		SwingUtilities.invokeLater( this::updateBreadcrumbButtons );
	}

	/**
	 * Called when the processor ends
	 */
	protected void executionEnded() {
		running = false;
		breadCrumbViewer.setEnabled(true);
		
		busyLabel.setBusy(false);
		reportTimer.stop();
		
		SwingUtilities.invokeLater( 
				this::updateBreadcrumbButtons 
		);

		breadCrumbViewer.setEnabled(true);
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

		reportStartTime = System.currentTimeMillis();
		reportTimer = new Timer(500, (e) -> {
			final long currentTime = System.currentTimeMillis();
			final long elapsedTime = currentTime - reportStartTime;

			final String title = String.format("Generating Report (%s)", MsFormatter.msToDisplayString(elapsedTime).substring(1, 6));
			reportTitledPanel.setTitle(title);
		});
		reportTimer.start();

		try {
			SwingUtilities.invokeLater( () -> {
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
							new NodeWizardReportGenerator(this, reportTree, reportTree.getReportTemplate(), fout);

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
					final HashMap<String, DefaultTableDataSource> tableMap = new HashMap<>();
					searchForTables(reportTree.getRoot(), tableMap);
					reportBufferPanel.setUserObject(reportTree);
					
					final Browser browser = reportBufferPanel.getBrowser();
					browser.addScriptContextListener(new ScriptContextListener() {
						
						@Override
						public void onScriptContextDestroyed(ScriptContextEvent arg0) {}
						
						@Override
						public void onScriptContextCreated(ScriptContextEvent arg0) {
							final JSValue windowObj = browser.executeJavaScriptAndReturnValue("window");
							windowObj.asObject().setProperty("project", getExtension(Project.class));
							windowObj.asObject().setProperty("buffers", bufferPanel);
							windowObj.asObject().setProperty("reportTree", reportTree);
							windowObj.asObject().setProperty("tableMap", tableMap);
							
							// phon general bridge
							windowObj.asObject().setProperty("jsbridge", new JavaScriptBridge(getExtension(Project.class)));
							// wizard-specific bridge
							windowObj.asObject().setProperty("app", webViewInterface);
						}
						
					});
					browser.addLoadListener(new LoadListener() {
						
						@Override
						public void onStartLoadingFrame(StartLoadingEvent arg0) {}
						
						@Override
						public void onProvisionalLoadingFrame(ProvisionalLoadingEvent arg0) {}
						
						@Override
						public void onFinishLoadingFrame(FinishLoadingEvent arg0) {
							int idx = 0;
							for(String tableId:tableMap.keySet()) {
								if(tableMap.get(tableId).getRowCount() == 0) continue;
								browser.executeJavaScript(
										String.format("addMenuButtons(document.getElementById('%s'), %d)", tableId, idx));
								browser.executeJavaScript(
										String.format("$(\"#table_menu_\" + (%d+1)).menu()", idx));
								++idx;
							}
							
							browser.setContextMenuHandler(new WebViewContextHandler(reportBufferPanel.getWebView(), reportTree, tableMap));							
						}
						
						@Override
						public void onFailLoadingFrame(FailLoadingEvent arg0) {}
						
						@Override
						public void onDocumentLoadedInMainFrame(LoadEvent arg0) {}
						
						@Override
						public void onDocumentLoadedInFrame(FrameLoadEvent arg0) {}
					});

					reportBufferPanel.getBrowser().loadURL(reportURL);
					reportBufferPanel.requestFocusInWindow();
				} catch (InterruptedException | InvocationTargetException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
				}
			} catch (IOException e) {
				LOGGER.error( e.getLocalizedMessage(), e);

				final BufferPanel errPanel = getLogBuffer();
				errPanel.getLogBuffer().setForeground(Color.red);
				final PrintWriter writer = new PrintWriter(errPanel.getLogBuffer().getStdErrStream());
				e.printStackTrace(writer);
				writer.flush();
				writer.close();
				
				final String title = String.format("Report Generation Failed (%s)", e.getLocalizedMessage());
				reportTitledPanel.setTitle(title);
			}

			SwingUtilities.invokeLater( () -> {

				statusLabel.setText("");
				
				final long currentTime = System.currentTimeMillis();
				final long elapsedTime = currentTime - reportStartTime;

				final String title = String.format("Report Completed (%s)", MsFormatter.msToDisplayString(elapsedTime).substring(1, 6));
				reportTitledPanel.setTitle(title);
			});
		} catch (ProcessingException pe) {
			SwingUtilities.invokeLater( () -> {
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

				final long currentTime = System.currentTimeMillis();
				final long elapsedTime = currentTime - reportStartTime;
				final String title = String.format("Report Generation Failed (%s)", MsFormatter.msToDisplayString(elapsedTime).substring(1, 6));
				reportTitledPanel.setTitle(title);
				
			});
			throw pe;
		} finally {
			SwingUtilities.invokeLater( this::executionEnded );
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
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		} else {
			LOGGER.warn( "Not found " + DEFAULT_REPORT_FILE, new FileNotFoundException(DEFAULT_REPORT_FILE));
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
		if(globalOptionsPanel.isOverrideCaseSensitive())
			ctx.put(CASE_SENSITIVE_GLOBAL_OPTION, globalOptionsPanel.isCaseSensitive());
		if(globalOptionsPanel.isOverrideIgnoreDiacritics()) {
			ctx.put(IGNORE_DIACRITICS_GLOBAL_OPTION, globalOptionsPanel.isIgnoreDiacritics());
			ctx.put(ONLYOREXCEPT_GLOBAL_OPTION, globalOptionsPanel.isOnlyOrExcept());
			ctx.put(SELECTED_DIACRITICS_GLOBAL_OPTION, globalOptionsPanel.getSelectedDiacritics());
		}
		if(globalOptionsPanel.isOverrideInventoryGroupingColumn())
			ctx.put(INVENTORY_GROUPING_GLOBAL_OPTION, globalOptionsPanel.getInventoryGroupingColumn());
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
						} else if(comp instanceof QueryNode.QueryNodeSettingsPanel) {
							return ((QueryNode.QueryNodeSettingsPanel)comp).getScriptPanel().checkParams();
						} else {
							return super.validateStep();
						}
					}

				};
				final BorderLayout layout = new BorderLayout();
				step.setLayout(layout);

				final JScrollPane scroller = new JScrollPane(comp);
				final TitledPanel panel = new TitledPanel(ext.getNodeTitle(node), scroller);
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
				LOGGER.error( e.getLocalizedMessage(), e);
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
			if(reportBufferAvailable()) {
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
		} else {
			getRootPane().setDefaultButton(nextButton);
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
					SwingUtilities.invokeLater( () -> super.cancel() );
				}
			});

			NativeDialogs.showMessageDialog(props);
		} else {
			super.cancel();
		}
	}
	
	private class WebViewContextHandler implements ContextMenuHandler {

		private BrowserView browserView;
		
		private ReportTree reportTree;
		
		private Map<String, DefaultTableDataSource> tableMap;
		
		public WebViewContextHandler(BrowserView browserView, ReportTree reportTree, Map<String, DefaultTableDataSource> tableMap) {
			this.browserView = browserView;
			this.reportTree = reportTree;
			this.tableMap = tableMap;
		}
		
		@Override
		public void showContextMenu(ContextMenuParams params) {
			// add report save and export items
			JPopupMenu menu = new JPopupMenu();
			final MenuBuilder builder = new MenuBuilder(menu);
			
			// element selection commands
			DOMNodeAtPoint nodeAtPoint = params.getBrowser().getNodeAtPoint(
					params.getLocation().x, params.getLocation().y);
			if(nodeAtPoint != null) {
				// look for a parent table
				DOMNode node = nodeAtPoint.getNode();
				while(node.getParent() != null && !node.getNodeName().equalsIgnoreCase("table")) {
					node = node.getParent();
				}
				
				if(node.getNodeName().equalsIgnoreCase("table")) {
					// add 'Open link' if table cell has an 'onclick' value
					// check for a span with an 'onclick'
					DOMElement linkEle = null;
					DOMNode n = nodeAtPoint.getNode();
					while(n.getParent() != null 
							&& !n.getParent().getXPath().equals(node.getXPath())
							&& !n.getNodeName().equalsIgnoreCase("span")) {
						n = n.getParent();
					}
					if(n.getNodeName().equalsIgnoreCase("span") && n.getParent().getNodeName().equalsIgnoreCase("td")) {
						DOMElement divEle = params.getBrowser().getDocument().findElement(By.xpath(n.getXPath()));
						String onclick = divEle.getAttribute("onclick");
						if(onclick != null && onclick.length() > 0) {
							linkEle = divEle;
						}
					}
					if(linkEle != null) {
						final PhonUIAction<Void> clickAct = PhonUIAction.runnable(linkEle::click);
						clickAct.putValue(PhonUIAction.NAME, "Open link");
						builder.addItem(".", clickAct);
					}
					
					DOMElement tableEle = params.getBrowser().getDocument().findElement(By.xpath(node.getXPath()));
					String tableId = tableEle.getAttribute("id");
					
					if(tableId != null && tableMap.containsKey(tableId)) {
						// look for the tr element
						n = nodeAtPoint.getNode();
						while(n.getParent() != null && !n.getNodeName().equalsIgnoreCase("tr")) {
							n = n.getParent();
						}
						if(n.getNodeName().equalsIgnoreCase("tr")) {
							String trXpath = n.getXPath();
							int trIdx = -1;
							Pattern p = Pattern.compile("\\[([0-9]+)\\]");
							Matcher m = p.matcher(trXpath);
							while(m.find()) {
								trIdx = Integer.parseInt(m.group(1))-2;
							}
							
							if(trIdx >= 0) {
								DefaultTableDataSource table = tableMap.get(tableId);
								Result r = (Result)table.getValueAt(trIdx, "Result");
								if(r != null) {
									final PluginAction openResultAct = new PluginAction(SessionEditorEP.EP_NAME);
									EntryPointArgs args = new EntryPointArgs();
									args.put(EntryPointArgs.PROJECT_OBJECT, getExtension(Project.class));
									args.put(EntryPointArgs.SESSION_NAME, table.getValueAt(trIdx, "Session").toString());
									args.put(SessionEditorEP.RECORD_INDEX_PROPERY, r.getRecordIndex());
									args.put(SessionEditorEP.RESULT_VALUES_PROPERTY, new Result[] {r});
									openResultAct.putArgs(args);
									openResultAct.putValue(PhonUIAction.NAME, "Open session and highlight result");
									openResultAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open session and highlight result");
									builder.addItem(".@Open Link", openResultAct);
								}
							}
						}

						if(menu.getComponentCount() > 0) {
							builder.addSeparator(".", "link_sep");
						}
						
						// add table menu items
						final PhonUIAction<String> saveTableAsCSV = PhonUIAction.consumer(params.getBrowser()::executeJavaScript,
								String.format("saveTableAsCSV('%s')", tableId));
						saveTableAsCSV.putValue(PhonUIAction.NAME, "Save table as CSV...");
						builder.addItem(".", saveTableAsCSV);
						
						final PhonUIAction<String> saveTableAsExcelAct = PhonUIAction.consumer(params.getBrowser()::executeJavaScript,
								String.format("saveTableAsExcel('%s')", tableId));
						saveTableAsExcelAct.putValue(PhonUIAction.NAME, "Save table as Excel (XLS)...");
						builder.addItem(".", saveTableAsExcelAct);
						
						final PhonUIAction<String> showTableAct = PhonUIAction.consumer(params.getBrowser()::executeJavaScript,
								String.format("showTable('%s')", tableId));
						showTableAct.putValue(PhonUIAction.NAME, "Open table in new buffer");
						builder.addItem(".", showTableAct);
						
						builder.addSeparator(".", "table_actions_sep");

						final PhonUIAction<String> copyTableAct = PhonUIAction.consumer(params.getBrowser()::executeJavaScript,
								String.format("onCopyTableData(document.getElementById('%s'), '%s')", tableId, tableId));
						copyTableAct.putValue(PhonUIAction.NAME, "Copy table");
						copyTableAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Copy table to clipboard as CSV");
						builder.addItem(".", copyTableAct);
					}
				}
			}
			
			// editor commands
			final PhonUIAction<EditorCommand> copyAct = PhonUIAction.consumer(params.getBrowser()::executeCommand, EditorCommand.COPY);
			copyAct.putValue(PhonUIAction.NAME, "Copy");
			copyAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Copy selection to clipboard");
			builder.addItem(".", copyAct);
			builder.addSeparator(".", "editor_commands");
			
			final PhonUIAction<Void> zoomInAct = PhonUIAction.runnable(params.getBrowser()::zoomIn);
			zoomInAct.putValue(PhonUIAction.NAME, "Zoom in");
			zoomInAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Increase zoom level");
			zoomInAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
			builder.addItem(".", zoomInAct);
			
			final PhonUIAction<Void> zoomOutAct = PhonUIAction.runnable(params.getBrowser()::zoomOut);
			zoomOutAct.putValue(PhonUIAction.NAME, "Zoom out");
			zoomOutAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Decrease zoom level");
			zoomOutAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_9, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
			builder.addItem(".", zoomOutAct);
			
			final PhonUIAction<Void> zoomResetAct = PhonUIAction.runnable(params.getBrowser()::zoomReset);
			zoomResetAct.putValue(PhonUIAction.NAME, "Reset zoom");
			zoomResetAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Reset zoom level to default");
			builder.addItem(".", zoomResetAct);
			
			builder.addSeparator(".", "zoom_actions");
			
			setupReportMenu(builder);
			
			menu.show(browserView, params.getLocation().x, params.getLocation().y);
		}
		
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
		
		public void showTable(String tableId, JSObject tableObj) {
			showTable(tableId, (DefaultTableDataSource)tableObj.asJavaObject());
		}
		
		public void showTable(String tableId, DefaultTableDataSource table) {
			SwingUtilities.invokeLater( () -> {
				if(!bufferPanel.getBufferNames().contains(tableId)) {
					createTableBuffer(tableId, table);
				}
				bufferPanel.selectBuffer(tableId);
			});
		}
		
		public void saveTableAsCSV(String tableId, JSObject tableObj) {
			saveTableAsCSV(tableId, (DefaultTableDataSource)tableObj.asJavaObject());
		}
		
		public void saveTableAsCSV(String tableId, DefaultTableDataSource table) {
			SwingUtilities.invokeLater(() -> {
				if(!bufferPanel.getBufferNames().contains(tableId)) {
					createTableBuffer(tableId, table);
				}
				bufferPanel.saveAsCSV(tableId);
			});
		}
		
		public void saveTableAsWorkbook(String tableId, JSObject tableObj) {
			saveTableAsWorkbook(tableId, (DefaultTableDataSource)tableObj.asJavaObject());
		}
		
		public void saveTableAsWorkbook(String tableId, DefaultTableDataSource table) {
			SwingUtilities.invokeLater( () -> {
				if(!bufferPanel.getBufferNames().contains(tableId)) {
					createTableBuffer(tableId, table);
				}
				bufferPanel.saveAsWorkbook(tableId);
			});
		}
		
		public void onHighlightResultValue(JSObject tableModelObj, int row, String columnName) {
			onHighlightResultValue((DefaultTableDataSource)tableModelObj.asJavaObject(), row, columnName);
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

			final PhonUIAction<Void> checkNodeAction = PhonUIAction.runnable(() -> {
				if(optionalsTree.isPathChecked(path))
					optionalsTree.setCheckingStateForPath(path, TristateCheckBoxState.UNCHECKED);
				else
					optionalsTree.setCheckingStateForPath(path, TristateCheckBoxState.CHECKED);
			});
			String name = (optionalsTree.isPathChecked(path) ? "Uncheck " : "Check ") +  opNode.getName();
			checkNodeAction.putValue(PhonUIAction.NAME, name);
			menuBuilder.addItem(".", checkNodeAction);

			menu.show(optionalsTree, e.getX(), e.getY());
		}

	}

}
