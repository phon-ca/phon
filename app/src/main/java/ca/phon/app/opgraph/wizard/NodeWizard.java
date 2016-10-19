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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.tree.TreePath;

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
import ca.phon.query.report.datasource.DefaultTableDataSource;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.wizard.WizardFrame;
import ca.phon.ui.wizard.WizardStep;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.worker.PhonWorker;

public class NodeWizard extends WizardFrame {
	
	private final static Logger LOGGER = Logger.getLogger(NodeWizard.class.getName());
	
	private final Processor processor;
	
	private final OpGraph graph;
	
	private MultiBufferPanel bufferPanel;
	
	private JXBusyLabel busyLabel;
	
	private JLabel statusLabel;
	
	protected WizardStep reportStep;
	
	protected WizardStep optionalsStep;
	
	private WizardOptionalsCheckboxTree optionalsTree;
	
	private WizardGlobalOptionsPanel optionsPanel;
	public final static String CASE_SENSITIVE_GLOBAL_OPTION = "__caseSensitive";
	public final static String IGNORE_DIACRITICS_GLOBAL_OPTION = "__ignoreDiacritics";
	public final static String PARTICIPANT_ROLE_GLOBAL_OPTION = "__participantRole";
	
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
//		builder.addItem("File@report", new CreateReportAction(this));
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
		bufferPanel = new MultiBufferPanel();
		
		final DialogHeader header = new DialogHeader(super.getTitle(), "");
		add(header, BorderLayout.NORTH);
		
		final JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		busyLabel = new JXBusyLabel(new Dimension(22, 22));
		statusLabel = new JLabel();
		
		statusPanel.setOpaque(false);
		statusPanel.add(busyLabel);
		statusPanel.add(statusLabel);
		
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.insets = new Insets(0, 24, 5, 2);
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridwidth = 1;
		
		header.add(statusPanel, gbc);
		
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
		
		for(OpNode node:nodeWizardList) {
			if(nodeWizardList.isNodeForced(node)) {
				final WizardStep step = createStep(nodeWizardList, node);
				step.setPrevStep(stepIdx-1);
				step.setNextStep(stepIdx+1);
				addWizardStep(step);
				++stepIdx;
			}
		}
		
		reportStep = createReportStep();
		reportStep.setPrevStep(stepIdx-1);
		reportStep.setNextStep(-1);
		addWizardStep(reportStep);
		
		final WizardStepList stepList = new WizardStepList(this);
		stepList.setMinimumSize(new Dimension(250, 20));
		stepList.setPreferredSize(new Dimension(250, 0));
		stepList.setMaximumSize(new Dimension(250, Integer.MAX_VALUE));
		
		final JPanel leftPanel = new JPanel(new GridBagLayout());
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(2, 2, 2, 2);
		
		final TitledPanel panel = new TitledPanel("Steps", new JScrollPane(stepList));
		leftPanel.add(panel, gbc);
		
		++gbc.gridy;
		gbc.weighty = 0.0;
		optionsPanel = new WizardGlobalOptionsPanel();
		final TitledPanel panel1 = new TitledPanel("Settings", optionsPanel);
		leftPanel.add(panel1, gbc);
		
		add(leftPanel, BorderLayout.WEST);
		
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
		
		final ImageIcon icn = 
				IconManager.getInstance().getIcon("actions/settings-white", IconSize.XSMALL);
		advancedSettingsButton = new JButton();
		advancedSettingsButton.setIcon(icn);
		advancedSettingsButton.setBorderPainted(false);
		advancedSettingsButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		advancedSettingsButton.setToolTipText("Show advanced settings");
		advancedSettingsButton.setVisible(nodeWizardList.size() > 0);
		advancedSettingsButton.addActionListener( (e) -> {
			cardLayout.show(centerPanel, SETTINGS);
		});
		panel1.setRightDecoration(advancedSettingsButton);
		
		super.btnFinish.setVisible(false);
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
				statusLabel.setText(nodeName);
				btnBack.setEnabled(false);
			});
			executionStarted(pe);
		} else if(pe.getType() == ProcessorEvent.Type.FINISH_NODE) {
		} else if(pe.getType() == ProcessorEvent.Type.COMPLETE) {
			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(false);
				statusLabel.setText("");
				btnBack.setEnabled(true);
				
				setModified(true);
				modified = true;
			});
			executionEnded(pe);
		}
	};
	
	/**
	 * Called when the processors begins
	 */
	public void executionStarted(ProcessorEvent pe) {
		running = true;
		btnCancel.setText("Stop Analysis");
	}
	
	/**
	 * Called when the processor ends
	 */
	public void executionEnded(ProcessorEvent pe) {
		running = false;
		btnCancel.setText("Close");
	}
	
	public void stopExecution() {
		if(processor != null) {
			processor.stop();
		}
	}
	
	public void executeGraph() throws ProcessingException {
		setupContext(processor.getContext());
		if(!processor.hasNext()) {
			processor.reset();
		}
		setupOptionals(processor.getContext());
		setupGlobalOptions(processor.getContext());
		processor.addProcessorListener(processorListener);
		try {
			processor.stepAll();
		} catch (ProcessingException pe) {
			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(false);
				statusLabel.setText(pe.getLocalizedMessage());
				
				final BufferPanel errPanel = bufferPanel.createBuffer("Error");
				final PrintWriter writer = new PrintWriter(errPanel.getLogBuffer().getStdErrStream());
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
		ctx.put(CASE_SENSITIVE_GLOBAL_OPTION, optionsPanel.isCaseSensitive());
		ctx.put(IGNORE_DIACRITICS_GLOBAL_OPTION, optionsPanel.isIgnoreDiacritics());
		ctx.put(PARTICIPANT_ROLE_GLOBAL_OPTION, optionsPanel.getSelectedParticipantRole());
		
		for(WizardGlobalOption pluginGlobalOption:optionsPanel.getPluginGlobalOptions()) {
			ctx.put(pluginGlobalOption.getName(), pluginGlobalOption.getValue());
		}
	}
	
	protected WizardStep createStep(WizardExtension ext, OpNode node) {
		final NodeSettings settings = node.getExtension(NodeSettings.class);
		if(settings != null) {
			try {
				final Component comp = settings.getComponent(null);
			
				final WizardStep step = new WizardStep();
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
	
	protected WizardStep createReportStep() {
		final WizardStep retVal = new WizardStep();
		retVal.setTitle("Generate report");
		
		retVal.setLayout(new BorderLayout());
		
		final TitledPanel panel = new TitledPanel("Generate report", getBufferPanel());
		retVal.add(panel, BorderLayout.CENTER);
		
		return retVal;
	}
	
	protected WizardStep createOptionalsStep() {
		final WizardStep retVal = new WizardStep();
		retVal.setTitle("Select analyses");
		
		retVal.setLayout(new BorderLayout());
		
		optionalsTree = new WizardOptionalsCheckboxTree(getWizardExtension());
		optionalsTree.addMouseListener(new OptionalsContextHandler());
		for(OpNode optionalNode:getWizardExtension().getOptionalNodes()) {
			if(getWizardExtension().getOptionalNodeDefault(optionalNode)) {
				optionalsTree.checkNode(optionalNode);
			}
		}
		
		final TitledPanel panel = new TitledPanel("Select analyses", new JScrollPane(optionalsTree));
		retVal.add(panel, BorderLayout.CENTER);
		
		return retVal;
	}
	
	protected WizardStep createIntroStep(String title, String message) {
		final WizardStep retVal = new WizardStep();
		
		retVal.setLayout(new BorderLayout());
		
		
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

		final JEditorPane editorPane = new JEditorPane("text/html", message);
		editorPane.setEditorKit(editorKit);
		editorPane.setEditable(false);
		editorPane.setText(message);
		
		editorPane.scrollRectToVisible(new Rectangle(0, 0, 0, 0));
		
		final TitledPanel stepTitle = 
				new TitledPanel(title, new JScrollPane(editorPane));
		retVal.add(stepTitle, BorderLayout.CENTER);
		
		return retVal;
	}
	
	@Override
	public void gotoStep(int step) {
		super.gotoStep(step);
		
		if(cardLayout != null)
			cardLayout.show(centerPanel, WIZARD_LIST);
		
		if(!inInit && getCurrentStep() == reportStep) {

			final Runnable inBg = () -> {
				try {
					executeGraph();
				} catch (ProcessingException e) {
					e.printStackTrace();
				}
			};
			
			final Runnable onEDT = () -> {
				if(bufferPanel.getBufferNames().size() > 0) {
					final MessageDialogProperties props = new MessageDialogProperties();
					props.setTitle("Re-run analysis");
					props.setHeader("Re-run analysis");
					props.setMessage("Clear results and re-run analysis.");
					props.setOptions(MessageDialogProperties.okCancelOptions);
					props.setRunAsync(false);
					props.setParentWindow(this);
		
					int retVal = NativeDialogs.showMessageDialog(props);
					if(retVal == 1) return;
					bufferPanel.closeAllBuffers();
				}
				
				PhonWorker.getInstance().invokeLater(inBg);
			};
			
			SwingUtilities.invokeLater(onEDT);
		}
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
