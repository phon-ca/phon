/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.TreePath;

import org.apache.velocity.tools.generic.MathTool;
import org.jdesktop.swingx.JXBusyLabel;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.ProcessorEvent;
import ca.gedge.opgraph.ProcessorListener;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.phon.app.log.BufferPanel;
import ca.phon.app.log.MultiBufferPanel;
import ca.phon.app.log.actions.SaveAllBuffersAction;
import ca.phon.app.opgraph.nodes.log.PrintBufferNode;
import ca.phon.app.opgraph.wizard.WizardOptionalsCheckboxTree.CheckedOpNode;
import ca.phon.app.query.ScriptPanel;
import ca.phon.formatter.FormatterUtil;
import ca.phon.project.ParticipantHistory;
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.fonts.FontPreferences;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;

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

	private final static Logger LOGGER = Logger.getLogger(NodeWizard.class.getName());
	
	private final Processor processor;
	
	private final OpGraph graph;
	
	private MultiBufferPanel bufferPanel;
	
	private JXBusyLabel busyLabel;
	
	private JLabel statusLabel;
	
	protected WizardStep reportDataStep;
	
	protected WizardStep optionalsStep;
	
	private WizardOptionalsCheckboxTree optionalsTree;
	
	private Map<OpNode, WizardStep> optionalSteps;
	
	private WizardGlobalOptionsPanel globalOptionsPanel;
	public final static String CASE_SENSITIVE_GLOBAL_OPTION = "__caseSensitive";
	public final static String IGNORE_DIACRITICS_GLOBAL_OPTION = "__ignoreDiacritics";
	
	private JPanel centerPanel;
	private CardLayout cardLayout;
	private AbstractButton advancedSettingsButton;
	
	private AdvancedSettingsPanel advancedSettingsPanel;
	
	private final static String WIZARD_LIST = "_wizard_list_";
	private final static String SETTINGS = "_settings_";
	
	boolean inInit = true;
	
	private volatile boolean running = false;
	
	private boolean modified = false;
	
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
		builder.addSeparator("File@1", "report");
		
		final PhonUIAction saveAllAct = new PhonUIAction(this, "onSaveAll");
		saveAllAct.putValue(PhonUIAction.NAME, "Save all buffers to folder...");
		saveAllAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save all buffers to a folder.");
		saveAllAct.putValue(PhonUIAction.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.ALT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		saveAllAct.putValue(PhonUIAction.SMALL_ICON, 
				IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL));
		builder.addItem("File@report", saveAllAct);

		final PhonUIAction saveAct = new PhonUIAction(this, "onSaveBuffer");
		saveAct.putValue(PhonUIAction.NAME, "Save buffer...");
		saveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save selected buffer to file.");
		saveAct.putValue(PhonUIAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		saveAct.putValue(PhonUIAction.SMALL_ICON, 
				IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL));
		builder.addItem("File@report", saveAct);
		
		builder.addSeparator("File@Save all buffers to folder...", "other");
	}
	
	@Override
	public void close() {
		boolean okToClose = true;

		if(running) {
			// ask to cancel current analysis
			final MessageDialogProperties props = new MessageDialogProperties();
			props.setRunAsync(false);
			props.setTitle("Close Window");
			props.setHeader(props.getTitle());
			props.setMessage("Cancel running analyses and close window?");
			props.setOptions(MessageDialogProperties.yesNoOptions);
			props.setParentWindow(this);
			
			okToClose = (NativeDialogs.showMessageDialog(props) == 0);
		}
		
		if(okToClose) {
			if(running) {
				stopExecution();
			}
			super.close();
		}
	}
	
	private void init() {
		// turn off parent navigation controls
		super.btnBack.setVisible(false);
		super.btnCancel.setVisible(false);
		super.btnFinish.setVisible(false);
		super.btnNext.setVisible(false);
		
		bufferPanel = new MultiBufferPanel();
		
		final JButton btnNext = new NodeWizardBreadcrumbButton();
		btnNext.setFont(FontPreferences.getTitleFont());
		btnNext.setText("Next");
		btnNext.setBackground(Color.yellow);
		btnNext.addActionListener( (e) -> next() );
		
		btnCancel = new NodeWizardBreadcrumbButton();
		btnCancel.setFont(FontPreferences.getTitleFont().deriveFont(Font.BOLD));
		btnCancel.setText("Stop");
		btnCancel.setBackground(Color.red);
		btnCancel.setForeground(Color.white);
		btnCancel.addActionListener( (e) -> cancel() );
		
//		final NodeWizardBreadcrumbButton gotoReportBtn = new NodeWizardBreadcrumbButton();
//		gotoReportBtn.setBackground(Color.green);
//		gotoReportBtn.setText("Use defaults and goto Report");
//		gotoReportBtn.setFont(FontPreferences.getTitleFont());
//		gotoReportBtn.addActionListener( (e) -> gotoReport() );
		
		final Runnable updateBreadcrumbButtons = () -> {
			JButton endBtn = btnNext;
			if(breadCrumbViewer.getBreadcrumb().getCurrentState() == reportDataStep) {
				breadCrumbViewer.remove(btnNext);
				breadCrumbViewer.add(btnCancel);
				endBtn = btnCancel;
			} else {
				breadCrumbViewer.remove(btnCancel);
				breadCrumbViewer.add(btnNext);
			}
			
			final Rectangle bounds = 
					new Rectangle(breadCrumbViewer.getBreadcrumbViewerUI().getPreferredSize().width-endBtn.getInsets().left/2-1, 
							0, endBtn.getPreferredSize().width, breadCrumbViewer.getHeight());
			endBtn.setBounds(bounds);
			
//			if(breadCrumbViewer.getBreadcrumb().size() == 1
//					&& breadCrumbViewer.getBreadcrumb().getCurrentState() != reportDataStep) {
//				// show goto report button
//				breadCrumbViewer.add(gotoReportBtn);
//				
//				final Rectangle gotoBounds = new Rectangle(
//						bounds.x + bounds.width, 0, gotoReportBtn.getPreferredSize().width, breadCrumbViewer.getHeight());
//				gotoReportBtn.setBounds(gotoBounds);
//			} else {
//				breadCrumbViewer.remove(gotoReportBtn);
//			}
			
			getRootPane().setDefaultButton(endBtn);
		
			breadCrumbViewer.revalidate();
			breadCrumbViewer.scrollRectToVisible(bounds);
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
		statusLabel = new JLabel();
		
		final WizardExtension nodeWizardList = 
				graph.getExtension(WizardExtension.class);
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
		cardLayout = new CardLayout();
		centerPanel = new JPanel(cardLayout);
		centerPanel.add(stepPanel, WIZARD_LIST);
		advancedSettingsPanel = new AdvancedSettingsPanel(nodeWizardList);
		final TitledPanel advPanel = new TitledPanel("Advanced Settings", advancedSettingsPanel);
		
		ImageIcon closeIcon = IconManager.getInstance().getIcon("misc/x-bold-white", IconSize.XSMALL);
		JButton closeBtn = new JButton(closeIcon);
		closeBtn.setBorderPainted(false);
		closeBtn.setToolTipText("Close advanced settings");
		closeBtn.addActionListener( (e) -> cardLayout.show(centerPanel, WIZARD_LIST) );
		advPanel.setRightDecoration(closeBtn);
		
		centerPanel.add(advPanel, SETTINGS);
		add(centerPanel, BorderLayout.CENTER);

		final JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		settingsPanel.setOpaque(false);
		globalOptionsPanel = new WizardGlobalOptionsPanel();
		
		final ImageIcon icn = 
				IconManager.getInstance().getIcon("actions/settings-black", IconSize.SMALL);
		advancedSettingsButton = new JButton();
//		advancedSettingsButton.setBorder(
//				BorderFactory.createMatteBorder(0, 0, 0, 1, Color.black));
//		advancedSettingsButton.setMargin(new Insets(0, 5, 0, 25));
		advancedSettingsButton.setIcon(icn);
		advancedSettingsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		advancedSettingsButton.setToolTipText("Show advanced settings");
		advancedSettingsButton.setVisible(nodeWizardList.size() > 0);
		advancedSettingsButton.addActionListener( (e) -> {
			cardLayout.show(centerPanel, SETTINGS);
		});
		
		settingsPanel.add(advancedSettingsButton);
		settingsPanel.add(globalOptionsPanel);
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridwidth = 1;
		
		buttonPanel.add(settingsPanel, gbc);
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
	
	public WizardExtension getWizardExtension() {
		return this.graph.getExtension(WizardExtension.class);
	}
	
	final ProcessorListener processorListener =  (ProcessorEvent pe) -> {
		if(pe.getType() == ProcessorEvent.Type.BEGIN_NODE) {
			final String nodeName = pe.getNode().getName();
			SwingUtilities.invokeLater( () -> {
				if(!busyLabel.isBusy()) {
					busyLabel.setBusy(true);
					
					setModified(false);
				}
				statusLabel.setText(nodeName + "...");
				btnBack.setEnabled(false);
			});
			executionStarted(pe);
		} else if(pe.getType() == ProcessorEvent.Type.FINISH_NODE) {
		} else if(pe.getType() == ProcessorEvent.Type.COMPLETE) {
			
			executionEnded(pe);
		}
	};
	
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
		btnBack.setEnabled(true);
	}
	
	public void stopExecution() {
		if(processor != null) {
			processor.stop();
		}
	}
	
	public void executeGraph() throws ProcessingException {
		setupContext(processor.getContext());
		processor.reset();
		setupOptionals(processor.getContext());
		setupGlobalOptions(processor.getContext());
		processor.addProcessorListener(processorListener);
		try {
			SwingUtilities.invokeLater( () -> {
				btnCancel.setVisible(true);
				breadCrumbViewer.setEnabled(false);
			});
			
			processor.stepAll();
			
			final WizardExtension ext = processor.getGraph().getExtension(WizardExtension.class);
			for(String reportName:ext.getReportTemplateNames()) {
				// create temp file
				File tempFile = null;
				try {
					tempFile = File.createTempFile("phon", reportName);
				} catch (IOException e) {
					continue;
				}
				tempFile.deleteOnExit();
				
				try(final FileOutputStream fout = new FileOutputStream(tempFile)) {
					final NodeWizardReportGenerator reportGenerator = 
							new NodeWizardReportGenerator(this, reportName, fout);
					
					SwingUtilities.invokeLater(() -> {
						statusLabel.setText("Generating report...");
					});
					try {
						reportGenerator.generateReport();
					} catch (NodeWizardReportException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
					
					// create buffer
					final AtomicReference<BufferPanel> bufferPanelRef = new AtomicReference<BufferPanel>();
					try {
						SwingUtilities.invokeAndWait( () -> bufferPanelRef.getAndSet(bufferPanel.createBuffer(reportName)));
					} catch (InterruptedException | InvocationTargetException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
						continue;
					}
					final BufferPanel reportBufferPanel = bufferPanelRef.get();
					final ReportReader reader = new ReportReader(reportBufferPanel, tempFile);
					reader.execute();
				} catch (IOException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
			
			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(false);
				statusLabel.setText("");
				btnBack.setEnabled(true);
				
				breadCrumbViewer.setEnabled(true);
				
				setModified(true);
				modified = true;
			});
		} catch (ProcessingException pe) {
			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(false);
				statusLabel.setText(pe.getLocalizedMessage());
				
				final BufferPanel errPanel = bufferPanel.createBuffer("Error");
				errPanel.getLogBuffer().setForeground(Color.red);
				final PrintWriter writer = new PrintWriter(errPanel.getLogBuffer().getStdErrStream());
				
				if(pe.getContext().getCurrentNode() != null) {
					writer.println(pe.getContext().getCurrentNode().getName() + " (" + pe.getContext().getCurrentNode().getId() + ")");
					// reset canceled status for node
					pe.getContext().getCurrentNode().setCanceled(false);
				}
				
				pe.printStackTrace(writer);
				writer.flush();
				writer.close();
				
				executionEnded(new ProcessorEvent());
			});
			throw pe;
		}
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
		for(String bufferName:bufferPanel.getBufferNames()) {
			final String data = 
					bufferPanel.getBuffer(bufferName).getLogBuffer().getText();
			buffers.put(bufferName, data);
			
			final DefaultTableDataSource table = 
					bufferPanel.getBuffer(bufferName).getExtension(DefaultTableDataSource.class);
			if(table != null) {
				tables.put(bufferName, table);
			}
		}
		
		ctx.put("Class", Class.class);
		ctx.put("FormatterUtil", FormatterUtil.class);
		ctx.put("Math", new MathTool());
		ctx.put("ParticipantHistory", ParticipantHistory.class);
		
		ctx.put("graph", getGraph());
		ctx.put("bufferNames", bufferPanel.getBufferNames());
		ctx.put("buffers", buffers);
		ctx.put("tables", tables);
	}
	
	public WizardOptionalsCheckboxTree getOptionalsTree() {
		return this.optionalsTree;
	}

	protected void setupContext(OpContext ctx) {
		ctx.put(PrintBufferNode.BUFFERS_KEY, bufferPanel);
	}
	
	protected void setupOptionals(OpContext ctx) {
		for(OpNode node:getWizardExtension().getOptionalNodes()) {
			final TreePath nodePath = optionalsTree.getNodePath(node);
			boolean enabled = optionalsTree.isPathChecked(nodePath)
					|| optionalsTree.isPathPartiallyChecked(nodePath);
			
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
		
		for(WizardGlobalOption pluginGlobalOption:globalOptionsPanel.getPluginGlobalOptions()) {
			ctx.put(pluginGlobalOption.getName(), pluginGlobalOption.getValue());
		}
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
				
				final TitledPanel panel = new TitledPanel(ext.getNodeTitle(node), new JScrollPane(comp));
				
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
		SwingUtilities.invokeLater(() -> bufferPanel.getSplitPane().setDividerLocation(400) );
		final TitledPanel panel = new TitledPanel("Report", bufferPanel);
		panel.setLeftDecoration(busyLabel);
		
		final JPanel currentNodePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		currentNodePanel.add(statusLabel);
		currentNodePanel.setOpaque(false);
		statusLabel.setOpaque(false);
		statusLabel.setForeground(UIManager.getColor("titledpanel.foreground"));
		panel.setRightDecoration(currentNodePanel);
		
		retVal.add(panel, BorderLayout.CENTER);
		
		return retVal;
	}
	
	@Override
	public void gotoStep(int step) {		
		super.gotoStep(step);
		
		if(cardLayout != null)
			cardLayout.show(centerPanel, WIZARD_LIST);
		
		if(!inInit && getCurrentStep() == reportDataStep) {
			if(bufferPanel.getBufferNames().size() > 0) {
				final MessageDialogProperties props = new MessageDialogProperties();
				props.setTitle("Re-run " + getNoun().getObj1());
				props.setHeader("Re-run " + getNoun().getObj1());
				props.setMessage("Clear results and re-run " + getNoun().getObj1() + ".");
				props.setOptions(MessageDialogProperties.okCancelOptions);
				props.setRunAsync(false);
				props.setParentWindow(this);
	
				int retVal = NativeDialogs.showMessageDialog(props);
				if(retVal == 1) return;
				bufferPanel.closeAllBuffers();
			}
			
			PhonWorker.getInstance().invokeLater( () -> executeGraph() );
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
			props.setRunAsync(false);
			
			int retVal = NativeDialogs.showMessageDialog(props);
			if(retVal == 0) return;
			stopExecution();
			if(retVal == 2)
				super.cancel();
		} else {
			super.cancel();
		}
	}
	
	@Override
	public boolean saveData() throws IOException {
		// save all buffers to folder
		final SaveAllBuffersAction saveAct = new SaveAllBuffersAction(bufferPanel);
		saveAct.actionPerformed(new ActionEvent(this, 0, "saveData"));
		
		modified = saveAct.wasCanceled();
		
		return !saveAct.wasCanceled();
	}
	
	public void setModified(boolean modified) {
		this.modified = modified;
		
		super.getRootPane().putClientProperty("Window.documentModified", hasUnsavedChanges());
	}
	
	@Override
	public boolean hasUnsavedChanges() {
		return modified;
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
