package ca.phon.app.opgraph.analysis;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.JXStatusBar.Constraint.ResizeBehavior;
import org.jdesktop.swingx.JXTable;

import com.sun.glass.events.KeyEvent;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.edits.graph.AddNodeEdit;
import ca.gedge.opgraph.app.edits.graph.DeleteNodesEdit;
import ca.gedge.opgraph.app.edits.graph.MoveNodesEdit;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.extensions.NodeMetadata;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.phon.app.opgraph.editor.EditorModelInstantiator;
import ca.phon.app.opgraph.editor.OpgraphFileFilter;
import ca.phon.app.opgraph.nodes.PhonScriptNode;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.opgraph.wizard.edits.NodeWizardOptionalsEdit;
import ca.phon.app.project.ProjectPathTransferable;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.project.Project;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.dnd.FileTransferHandler;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.NativeDialogEvent;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.util.resources.ResourceLoader;
import ca.phon.worker.PhonWorker;

public class AnalysisGeneratorFrame extends CommonModuleFrame {

	private static final long serialVersionUID = -7170934782795308487L;

	private final static Logger LOGGER = Logger.getLogger(AnalysisGeneratorFrame.class.getName());

	private final static int Y_START = 50;
	private final static int X_START = 400;
	private final static int Y_SEP = 150;

	private JToolBar toolbar;
	private JButton saveButton;
	private JButton addButton;
	private JButton removeButton;
	private JButton settingsButton;
	private JButton renameButton;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton runButton;
	private JButton openInComposerButton;

	private JXTable analysisNodeTable;
	private List<OpNode> analysisNodes;

	private JXStatusBar statusBar;
	private JXBusyLabel busyLabel;
	private JLabel statusLabel;

	private JMenuBar menuBar;

	private final AnalysisOpGraphEditorModel model;

	/**
	 * Constructor
	 *
	 * @param project if <code>null</code> project graphs will not be displayed
	 */
	public AnalysisGeneratorFrame(Project project) {
		super();

		final EditorModelInstantiator instantiator = new AnalysisEditorModelInstantiator();
		model = (AnalysisOpGraphEditorModel)instantiator.createModel(new OpGraph());
		model.getDocument().getUndoSupport().addUndoableEditListener( (e) -> updateTitle() );

//		putExtension(UndoManager.class, model.getDocument().getUndoManager());
		putExtension(Project.class, project);

		init();
		updateTitle();
	}

	private void init() {
		// create components for popup window selection
		final ImageIcon saveIcn =
				IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		final PhonUIAction saveAct = new PhonUIAction(this, "saveData");
		saveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save analysis");
		saveAct.putValue(PhonUIAction.SMALL_ICON, saveIcn);
		saveButton = new JButton(saveAct);

		final ImageIcon addIcn =
				IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final PhonUIAction addAct = new PhonUIAction(this, "onAdd");
		addAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add analyses");
		addAct.putValue(PhonUIAction.SMALL_ICON, addIcn);
		addButton = new JButton(addAct);

		final ImageIcon removeIcn =
				IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction removeAct = new PhonUIAction(this, "onRemove");
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected analysis");
		removeAct.putValue(PhonUIAction.SMALL_ICON, removeIcn);
		final KeyStroke removeKs = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		removeButton = new JButton(removeAct);

		final ImageIcon settingsIcn =
				IconManager.getInstance().getIcon("actions/settings-black", IconSize.SMALL);
		final PhonUIAction settingsAct = new PhonUIAction(this, "onShowSettings");
		settingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show settings for selected analysis");
		settingsAct.putValue(PhonUIAction.SMALL_ICON, settingsIcn);
		settingsButton = new JButton(settingsAct);

		final ImageIcon renameIcn =
				IconManager.getInstance().getIcon("actions/edit-rename", IconSize.SMALL);
		final PhonUIAction renameAct = new PhonUIAction(this, "onRename");
		renameAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Rename selected analysis");
		renameAct.putValue(PhonUIAction.SMALL_ICON, renameIcn);
		renameButton = new JButton(renameAct);

		final ImageIcon upIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-up", IconSize.SMALL);
		final PhonUIAction upAct = new PhonUIAction(this, "onMoveUp");
		upAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected analysis up");
		upAct.putValue(PhonUIAction.SMALL_ICON, upIcn);
		final KeyStroke upKs = KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		moveUpButton = new JButton(upAct);

		final ImageIcon downIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-down", IconSize.SMALL);
		final PhonUIAction downAct = new PhonUIAction(this, "onMoveDown");
		downAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected analysis down");
		downAct.putValue(PhonUIAction.SMALL_ICON, downIcn);
		final KeyStroke downKs = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		moveDownButton = new JButton(downAct);

		final ImageIcon runIcn =
				IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL);
		final PhonUIAction runAct = new PhonUIAction(this, "onRun");
		runAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Run analysis");
		runAct.putValue(PhonUIAction.SMALL_ICON, runIcn);
		runButton = new JButton(runAct);

		final ImageIcon graphIcn =
				IconManager.getInstance().getIcon("opgraph/graph", IconSize.SMALL);
		final PhonUIAction openInComposerAct = new PhonUIAction(this, "onOpenInComposer");
		openInComposerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open in Composer (advanced)");
		openInComposerAct.putValue(PhonUIAction.SMALL_ICON, graphIcn);
		openInComposerButton = new JButton(openInComposerAct);

		analysisNodes = Collections.synchronizedList(new ArrayList<>());
		analysisNodeTable = new JXTable(new AnalysisNodeTableModel());
		analysisNodeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		analysisNodeTable.setSortable(false);
		analysisNodeTable.setDragEnabled(true);
		analysisNodeTable.setTransferHandler(new AnalysisNodeTableTransferHandler());
		analysisNodeTable.setDropMode(DropMode.INSERT);
		analysisNodeTable.setVisibleRowCount(10);
		analysisNodeTable.setTableHeader(null);

		final ActionMap am = analysisNodeTable.getActionMap();
		final InputMap inputMap = analysisNodeTable.getInputMap(JComponent.WHEN_FOCUSED);

		inputMap.put(upKs, "moveUp");
		am.put("moveUp", upAct);

		inputMap.put(downKs, "moveDown");
		am.put("moveDown", downAct);

		inputMap.put(removeKs, "delete");
		am.put("delete", removeAct);

		// setup settings column
		final JScrollPane analysisNodeScroller = new JScrollPane(analysisNodeTable);

		toolbar = new JToolBar();
		toolbar.add(saveButton);
		toolbar.addSeparator();

		toolbar.add(addButton);
		toolbar.add(removeButton);
		toolbar.addSeparator();

		toolbar.add(settingsButton);
		toolbar.add(renameButton);
		toolbar.addSeparator();

		toolbar.add(moveUpButton);
		toolbar.add(moveDownButton);
		toolbar.addSeparator();

		toolbar.add(runButton);
		toolbar.addSeparator();

		toolbar.add(openInComposerButton);

		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		statusLabel = new JLabel();

		statusBar = new JXStatusBar();
		statusBar.add(busyLabel, new JXStatusBar.Constraint(16));
		statusBar.add(statusLabel, new JXStatusBar.Constraint(ResizeBehavior.FILL));

		setLayout(new BorderLayout());
		add(analysisNodeScroller, BorderLayout.CENTER);
		add(toolbar, BorderLayout.NORTH);
		add(statusBar, BorderLayout.SOUTH);
	}

	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		this.menuBar = menuBar;
		setupMenu();
	}

	protected void setupMenu() {
		final ImageIcon saveIcn =
				IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		final PhonUIAction saveAct = new PhonUIAction(this, "saveData");
		saveAct.putValue(PhonUIAction.NAME, "Save");
		saveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save analysis");
		saveAct.putValue(PhonUIAction.SMALL_ICON, saveIcn);
		final KeyStroke saveKs = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		saveAct.putValue(PhonUIAction.ACCELERATOR_KEY, saveKs);
		final JMenuItem saveItem = new JMenuItem(saveAct);

		final ImageIcon addIcn =
				IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final PhonUIAction addAct = new PhonUIAction(this, "onAdd");
		addAct.putValue(PhonUIAction.NAME, "Add analysis...");
		addAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add analysis...");
		addAct.putValue(PhonUIAction.SMALL_ICON, addIcn);
		final JMenuItem addItem = new JMenuItem(addAct);

		final ImageIcon removeIcn =
				IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction removeAct = new PhonUIAction(this, "onRemove");
		removeAct.putValue(PhonUIAction.NAME, "Remove analysis");
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected analysis");
		removeAct.putValue(PhonUIAction.SMALL_ICON, removeIcn);
		final KeyStroke removeKs = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		removeAct.putValue(PhonUIAction.ACCELERATOR_KEY, removeKs);
		final JMenuItem removeItem = new JMenuItem(removeAct);

		final ImageIcon settingsIcn =
				IconManager.getInstance().getIcon("actions/settings-black", IconSize.SMALL);
		final PhonUIAction settingsAct = new PhonUIAction(this, "onShowSettings");
		settingsAct.putValue(PhonUIAction.NAME, "Settings...");
		settingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show settings for selected analysis");
		settingsAct.putValue(PhonUIAction.SMALL_ICON, settingsIcn);
		final KeyStroke settingsKs = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		settingsAct.putValue(PhonUIAction.ACCELERATOR_KEY, settingsKs);
		final JMenuItem settingsItem = new JMenuItem(settingsAct);

		final ImageIcon renameIcn =
				IconManager.getInstance().getIcon("actions/edit-rename", IconSize.SMALL);
		final PhonUIAction renameAct = new PhonUIAction(this, "onRename");
		renameAct.putValue(PhonUIAction.NAME, "Rename");
		renameAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Rename selected analysis");
		renameAct.putValue(PhonUIAction.SMALL_ICON, renameIcn);
		final JMenuItem renameItem = new JMenuItem(renameAct);

		final ImageIcon upIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-up", IconSize.SMALL);
		final PhonUIAction upAct = new PhonUIAction(this, "onMoveUp");
		upAct.putValue(PhonUIAction.NAME, "Move up");
		upAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected analysis up");
		upAct.putValue(PhonUIAction.SMALL_ICON, upIcn);
		final KeyStroke upKs = KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		upAct.putValue(PhonUIAction.ACCELERATOR_KEY, upKs);
		final JMenuItem upItem = new JMenuItem(upAct);

		final ImageIcon downIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-down", IconSize.SMALL);
		final PhonUIAction downAct = new PhonUIAction(this, "onMoveDown");
		downAct.putValue(PhonUIAction.NAME, "Move down");
		downAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected analysis down");
		downAct.putValue(PhonUIAction.SMALL_ICON, downIcn);
		final KeyStroke downKs = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		downAct.putValue(PhonUIAction.ACCELERATOR_KEY, downKs);
		final JMenuItem downItem = new JMenuItem(downAct);

		final ImageIcon runIcn =
				IconManager.getInstance().getIcon("actions/media-playback-start", IconSize.SMALL);
		final PhonUIAction runAct = new PhonUIAction(this, "onRun");
		runAct.putValue(PhonUIAction.NAME, "Run analysis...");
		runAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Run analysis");
		runAct.putValue(PhonUIAction.SMALL_ICON, runIcn);
		final KeyStroke runKs = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
		runAct.putValue(PhonUIAction.ACCELERATOR_KEY, runKs);
		final JMenuItem runItem = new JMenuItem(runAct);

		final ImageIcon graphIcn =
				IconManager.getInstance().getIcon("opgraph/graph", IconSize.SMALL);
		final PhonUIAction openInComposerAct = new PhonUIAction(this, "onOpenInComposer");
		openInComposerAct.putValue(PhonUIAction.NAME, "Open in Composer (advanced)");
		openInComposerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open in Composer (advanced)");
		openInComposerAct.putValue(PhonUIAction.SMALL_ICON, graphIcn);
		final JMenuItem openInComposerItem = new JMenuItem(openInComposerAct);

		final MenuBuilder builder = new MenuBuilder(this.menuBar);

		builder.addItem("File@^", saveItem);
		builder.addSeparator("File@Save", "composer");
		builder.addItem("File@composer", runItem);
		builder.addItem("File@Run analysis...", openInComposerItem);
		builder.addSeparator("File@Open in Composer (advanced)", "remainder");

		builder.addMenu(".@Edit", "List");
		builder.addItem("List", addItem);
		builder.addItem("List", removeItem);
		builder.addSeparator("List", "settings");
		builder.addItem("List", settingsItem);
		builder.addItem("List", renameItem);
		builder.addSeparator("List", "move");
		builder.addItem("List", upItem);
		builder.addItem("List", downItem);
	}

	@Override
	public boolean hasUnsavedChanges() {
		return getModel().getDocument().hasModifications();
	}

	public File getCurrentFile() {
		return getModel().getDocument().getSource();
	}

	public void setCurrentFile(File source) {
		getModel().getDocument().setSource(source);
		updateTitle();
	}

	protected void updateTitle() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Composer (Analysis-simple)");
		sb.append(" : ");
		if(getCurrentFile() != null)
			sb.append(getCurrentFile().getAbsolutePath());
		else
			sb.append("Untitled");
		if(hasUnsavedChanges()) {
			sb.append("*");
		}
		setWindowName(sb.toString());

		// also update modification status
		setModified(hasUnsavedChanges());
	}

	public void onAdd(PhonActionEvent pae) {
		final ActionEvent ae = pae.getActionEvent();
		final JComponent comp =
				(ae.getSource() instanceof JComponent ? (JComponent)ae.getSource() : null);

		final JTree tree = new JTree(createTreeModel());
		tree.setVisibleRowCount(20);
		tree.setCellRenderer(new TreeNodeRenderer());

		// expand
		tree.expandRow(1);

		final JScrollPane scroller = new JScrollPane(tree);
		scroller.setPreferredSize(new Dimension(300, tree.getPreferredScrollableViewportSize().height));

		final Point p = new Point(0, comp.getHeight());
		SwingUtilities.convertPointToScreen(p, comp);

		final JFrame popup = new JFrame("Add analysis");
		popup.setUndecorated(true);
		popup.addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowLostFocus(WindowEvent e) {
				destroyPopup(popup);
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
			}

		});

		final PhonUIAction cancelAct = new PhonUIAction(this, "destroyPopup", popup);
		cancelAct.putValue(PhonUIAction.NAME, "Cancel");
		final JButton cancelBtn = new JButton(cancelAct);

		final PhonUIAction okAct = new PhonUIAction(this, "addSelectedAnalyses", tree);
		okAct.putValue(PhonUIAction.NAME, "Add selected");
		final JButton okBtn = new JButton(okAct);
		okBtn.addActionListener( (e) -> destroyPopup(popup) );

		final PhonUIAction browseAct = new PhonUIAction(this, "onBrowse");
		browseAct.putValue(PhonUIAction.NAME, "Browse...");
		final JButton browseBtn = new JButton(browseAct);
		browseBtn.addActionListener( (e) -> destroyPopup(popup) );

		final JComponent btnBar = ButtonBarBuilder.buildOkCancelBar(okBtn, cancelBtn, browseBtn);

		popup.setLayout(new BorderLayout());
		popup.add(scroller, BorderLayout.CENTER);
		popup.add(btnBar, BorderLayout.SOUTH);

		popup.pack();
		popup.setLocation(p.x, p.y);
		popup.setVisible(true);

		popup.getRootPane().setDefaultButton(okBtn);
	}

	public void destroyPopup(JFrame popup) {
		popup.setVisible(false);
		popup.dispose();
	}

	public void addSelectedAnalyses(JTree tree) {
		final TreePath selectedPath = tree.getSelectionPath();
		if(selectedPath != null) {
			addAnalyses((DefaultMutableTreeNode)selectedPath.getLastPathComponent());
		}
	}

	private void addAnalyses(List<File> fileList) {
		for(File f:fileList) {
			if(f.isFile()
					&& f.getName().endsWith(".xml")) {
				PhonWorker.getInstance().invokeLater( () -> {
					try {
						addAnalysis(f);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				});
			}
		}
	}

	private void addAnalyses(DefaultMutableTreeNode node) {
		if(node.isLeaf()) {
			if(node.getUserObject() instanceof URL) {
				final URL analysisURL = (URL)node.getUserObject();
				PhonWorker.getInstance().invokeLater( () -> {
					try {
						addAnalysis(analysisURL);
					} catch (IOException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				} );
			}
		} else {
			for(int i = 0; i < node.getChildCount(); i++) {
				final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
				addAnalyses(childNode);
			}
		}
	}

	private void addAnalysis(File analysisFile) throws IOException {
		addAnalysis(analysisFile.toURI().toURL());
	}

	/*
	 * This method should be executed on a background thread
	 */
	private void addAnalysis(URL analysisURL) throws IOException {
		// create analysis node
		try(InputStream is = analysisURL.openStream()) {
			final String analysisFile = URLDecoder.decode(analysisURL.toString(), "UTF-8");
			final String analysisName = FilenameUtils.getBaseName(analysisFile);
			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(true);
				statusLabel.setText("Adding " + analysisName + "...");
			});

			final OpGraph analysisGraph = OpgraphIO.read(is);

			final MacroNode analysisNode = new MacroNode(analysisGraph);
			analysisNode.setName(analysisName);
			final NodeMetadata nodeMeta = new NodeMetadata(X_START, Y_START + analysisNodes.size() * Y_SEP);
			analysisNode.putExtension(NodeMetadata.class, nodeMeta);

			// find input nodes and publish fields
			final OpNode projectNode = analysisGraph.getNodesByName("Project").stream().findFirst().orElse(null);
			final OpNode sessionsNode = analysisGraph.getNodesByName("Selected Sessions").stream().findFirst().orElse(null);
			final OpNode participantsNode = analysisGraph.getNodesByName("Selected Participants").stream().findFirst().orElse(null);

			analysisNode.publish("project", projectNode, projectNode.getInputFieldWithKey("obj"));
			analysisNode.publish("selectedSessions", sessionsNode, sessionsNode.getInputFieldWithKey("obj"));
			analysisNode.publish("selectedParticipants", participantsNode, participantsNode.getInputFieldWithKey("obj"));

			SwingUtilities.invokeLater( () -> {
				final AddNodeEdit addEdit = new AddNodeEdit(getGraph(), analysisNode);
				getModel().getDocument().getUndoSupport().postEdit(addEdit);

				final NodeWizardOptionalsEdit optEdit =
						new NodeWizardOptionalsEdit(getGraph(), getGraph().getExtension(WizardExtension.class), analysisNode, true, true);
				getModel().getDocument().getUndoSupport().postEdit(optEdit);

				analysisNodes.add(analysisNode);
				((AnalysisNodeTableModel)analysisNodeTable.getModel()).fireTableRowsInserted(analysisNodes.size()-1, analysisNodes.size()-1);
				analysisNodeTable.setRowSelectionInterval(analysisNodes.size()-1, analysisNodes.size()-1);
			});
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw e;
		} finally {
			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(false);
				statusLabel.setText("");
			});
		}
	}

	public void onRemove() {
		final int selectedRow = analysisNodeTable.getSelectedRow();
		if(selectedRow >= 0 && selectedRow < analysisNodes.size()) {
			final OpNode selectedNode = analysisNodes.get(selectedRow);

			getModel().getDocument().getUndoSupport().beginUpdate();

			final NodeWizardOptionalsEdit optEdit =
					new NodeWizardOptionalsEdit(getGraph(), getGraph().getExtension(WizardExtension.class), selectedNode, false, false);
			getModel().getDocument().getUndoSupport().postEdit(optEdit);

			final DeleteNodesEdit removeEdit =
					new DeleteNodesEdit(getGraph(), Collections.singleton(selectedNode));
			getModel().getDocument().getUndoSupport().postEdit(removeEdit);

			getModel().getDocument().getUndoSupport().endUpdate();

			analysisNodes.remove(selectedRow);
			((AnalysisNodeTableModel)analysisNodeTable.getModel()).fireTableRowsDeleted(selectedRow, selectedRow);

			updateNodeLocations();

			if(analysisNodes.size() > 0) {
				if(selectedRow < analysisNodes.size()) {
					analysisNodeTable.setRowSelectionInterval(selectedRow, selectedRow);
				} else {
					analysisNodeTable.setRowSelectionInterval(analysisNodes.size()-1, analysisNodes.size()-1);
				}
			}
		}
	}

	public void onRename() {
		final int selectedRow = analysisNodeTable.getSelectedRow();
		if(selectedRow >= 0 && selectedRow < analysisNodes.size()) {
			analysisNodeTable.editCellAt(selectedRow, 1);
			analysisNodeTable.requestFocusInWindow();
		}
	}

	public void onMoveUp() {
		final int selectedRow = analysisNodeTable.getSelectedRow();
		if(selectedRow > 0 && selectedRow < analysisNodes.size()) {
			final OpNode selectedNode = analysisNodes.get(selectedRow);

			int newLocation = selectedRow - 1;
			analysisNodes.remove(selectedRow);
			analysisNodes.add(newLocation, selectedNode);

			((AnalysisNodeTableModel)analysisNodeTable.getModel()).fireTableRowsDeleted(selectedRow, selectedRow);
			((AnalysisNodeTableModel)analysisNodeTable.getModel()).fireTableRowsInserted(newLocation, newLocation);
			analysisNodeTable.getSelectionModel().setSelectionInterval(newLocation, newLocation);

			updateNodeLocations();
		}
	}

	public void onMoveDown() {
		final int selectedRow = analysisNodeTable.getSelectedRow();
		if(selectedRow >= 0 && selectedRow < analysisNodes.size()-1) {
			final OpNode selectedNode = analysisNodes.get(selectedRow);

			int newLocation = selectedRow + 1;
			analysisNodes.remove(selectedRow);
			analysisNodes.add(newLocation, selectedNode);

			((AnalysisNodeTableModel)analysisNodeTable.getModel()).fireTableRowsDeleted(selectedRow, selectedRow);
			((AnalysisNodeTableModel)analysisNodeTable.getModel()).fireTableRowsInserted(newLocation, newLocation);
			analysisNodeTable.getSelectionModel().setSelectionInterval(newLocation, newLocation);

			updateNodeLocations();
		}
	}

	public void onShowSettings() {
		final int selectedRow = analysisNodeTable.getSelectedRow();
		if(selectedRow >= 0 && selectedRow < analysisNodes.size()) {
			final MacroNode selectedNode = (MacroNode)analysisNodes.get(selectedRow);
			showAnalysisSettings(selectedNode);
		}
	}

	public void onRun() {

	}

	public void onOpenInComposer() {

	}

	public void onBrowse() {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(this);
		props.setAllowMultipleSelection(true);
		props.setCanChooseDirectories(false);
		props.setCanChooseFiles(true);
		props.setFileFilter(FileFilter.xmlFilter);
		props.setInitialFolder(getModel().getDefaultFolder());
		props.setTitle("Add Analysis");
		props.setPrompt("Add");
		props.setRunAsync(false);

		List<String> selectedFiles = NativeDialogs.showOpenDialog(props);
		final List<File> fileList =
				selectedFiles.stream().map( (s) -> new File(s) ).collect(Collectors.toList());
		addAnalyses(fileList);
	}

	public void showAnalysisSettings(MacroNode analysisNode) {
		final AnalysisSettingsPanel settingsPanel = new AnalysisSettingsPanel(analysisNode);
		final JDialog settingsDialog = new JDialog(CommonModuleFrame.getCurrentFrame(), "Settings : " + analysisNode.getName(), true);

		final DialogHeader header = new DialogHeader("Settings : " + analysisNode.getName(), "Edit settings for the " + analysisNode.getName() + " analysis.");
		settingsDialog.getContentPane().setLayout(new BorderLayout());
		settingsDialog.getContentPane().add(header, BorderLayout.NORTH);
		settingsDialog.getContentPane().add(settingsPanel, BorderLayout.CENTER);

		settingsDialog.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				updateNodeName(analysisNode);
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}

		});

		settingsDialog.pack();
		settingsDialog.setSize(900, 700);
		settingsDialog.setLocationRelativeTo(CommonModuleFrame.getCurrentFrame());
		settingsDialog.setVisible(true);

	}

	/**
	 * If the given analysis node has a settings node 'Parameters' which
	 * is a {@link PhonScriptNode} and has a parameter 'reportTitle' this
	 * method will change that parameter value to be the name of the
	 * analysis node.
	 */
	private void updateReportTitle(MacroNode analysisNode) {
		// find the 'Parameters' settings node
		final OpGraph graph = analysisNode.getGraph();
		final WizardExtension wizardExtension = graph.getExtension(WizardExtension.class);
		OpNode parametersNode = null;
		for(OpNode node:wizardExtension) {
			if(node.getName().equals("Parameters") && node instanceof PhonScriptNode) {
				parametersNode = node;
				break;
			}
		}
		if(parametersNode != null) {
			final PhonScriptNode scriptNode = (PhonScriptNode)parametersNode;
			final PhonScript script = scriptNode.getScript();

			try {
				final ScriptParameters scriptParams = script.getContext().getScriptParameters(script.getContext().getEvaluatedScope());
				for(ScriptParam sp:scriptParams) {
					if(sp.getParamIds().contains("reportTitle")) {
						sp.setValue("reportTitle", analysisNode.getName());
						break;
					}
				}
			} catch (PhonScriptException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}

	/**
	 * After modifying analysis settings, there may be a change to the report title.
	 * Update the node name to match.
	 *
	 */
	private void updateNodeName(MacroNode analysisNode) {
		final OpGraph graph = analysisNode.getGraph();
		final WizardExtension wizardExtension = graph.getExtension(WizardExtension.class);
		OpNode parametersNode = null;
		for(OpNode node:wizardExtension) {
			if(node.getName().equals("Parameters") && node instanceof PhonScriptNode) {
				parametersNode = node;
				break;
			}
		}
		if(parametersNode != null) {
			final PhonScriptNode scriptNode = (PhonScriptNode)parametersNode;
			final PhonScript script = scriptNode.getScript();

			try {
				final ScriptParameters scriptParams = script.getContext().getScriptParameters(script.getContext().getEvaluatedScope());
				for(ScriptParam sp:scriptParams) {
					if(sp.getParamIds().contains("reportTitle")) {
						final String name = sp.getValue("reportTitle").toString();
						if(name.trim().length() > 0) {
							analysisNode.setName(sp.getValue("reportTitle").toString());
							((AnalysisNodeTableModel)analysisNodeTable.getModel()).fireTableRowsUpdated(analysisNodes.indexOf(analysisNode), analysisNodes.indexOf(analysisNode));
						}
					}
				}
			} catch (PhonScriptException e) {
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}

	private void updateNodeLocations() {
		getModel().getDocument().getUndoSupport().beginUpdate();
		for(int i = 0; i < analysisNodes.size(); i++) {
			final OpNode node = analysisNodes.get(i);

			final NodeMetadata nodeMeta = node.getExtension(NodeMetadata.class);

			final int newX = X_START;
			final int newY = Y_START + (i * Y_SEP);
			final int deltaX = newX - nodeMeta.getX();
			final int deltaY = newY - nodeMeta.getY();

			final MoveNodesEdit moveEdit = new MoveNodesEdit(Collections.singleton(node),
					deltaX, deltaY);
			getModel().getDocument().getUndoSupport().postEdit(moveEdit);
		}
		getModel().getDocument().getUndoSupport().endUpdate();
	}

	private TreeModel createTreeModel() {
		final DefaultMutableTreeNode root =
				new DefaultMutableTreeNode("All Analyses", true);

		final AnalysisLibrary library = new AnalysisLibrary();
		final ResourceLoader<URL> stockAnalysisLoader = library.getStockGraphs();
		final Iterator<URL> stockItr = stockAnalysisLoader.iterator();
		if(stockItr.hasNext()) {
			final DefaultMutableTreeNode stockNode =
					new DefaultMutableTreeNode("Stock Analyses", true);
			while(stockItr.hasNext()) {
				final URL analysisURL = stockItr.next();

				try {
					final String fullPath = URLDecoder.decode(analysisURL.getPath(), "UTF-8");
					String relativePath =
							fullPath.substring(fullPath.indexOf(AnalysisLibrary.ANALYSIS_FOLDER + "/")+AnalysisLibrary.ANALYSIS_FOLDER.length()+1);

					DefaultMutableTreeNode parentNode = stockNode;
					int splitIdx = -1;
					while((splitIdx = relativePath.indexOf('/')) >= 0) {
						final String nodeName = relativePath.substring(0, splitIdx);

						DefaultMutableTreeNode node = null;
						for(int i = 0; i < parentNode.getChildCount(); i++) {
							final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)parentNode.getChildAt(i);
							if(childNode.getUserObject().equals(nodeName)) {
								node = childNode;
								break;
							}
						}
						if(node == null) {
							node = new DefaultMutableTreeNode(nodeName, true);
							parentNode.add(node);
						}
						parentNode = node;
					}

					final DefaultMutableTreeNode analysisNode =
							new DefaultMutableTreeNode(analysisURL, true);
					parentNode.add(analysisNode);
				} catch (UnsupportedEncodingException e) {

				}
			}
			root.add(stockNode);
		}

		// TODO user graphs

		// TODO project graphs

		return new DefaultTreeModel(root);
	}

	public AnalysisOpGraphEditorModel getModel() {
		return this.model;
	}

	public OpGraph getGraph() {
		return this.model.getDocument().getGraph();
	}

	public boolean chooseFile() {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(this);
		props.setCanCreateDirectories(true);
		props.setFileFilter(new OpgraphFileFilter());
		props.setRunAsync(false);
		props.setTitle("Save analysis");

		if(getCurrentFile() != null) {
			final File parentFolder = getCurrentFile().getParentFile();
			final String name = getCurrentFile().getName();

			props.setInitialFolder(parentFolder.getAbsolutePath());
			props.setInitialFile(name);
		} else {
			props.setInitialFolder(getModel().getDefaultFolder());
			props.setInitialFile("Untitled.xml");
		}

		final String saveAs = NativeDialogs.showSaveDialog(props);
		if(saveAs != null) {
			setCurrentFile(new File(saveAs));
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean saveData() throws IOException {
		if(!getModel().validate()) return false;
		if(getCurrentFile() == null) {
			if(!chooseFile()) return false;
		}
		OpgraphIO.write(getModel().getDocument().getRootGraph(), getCurrentFile());
		getModel().getDocument().markAsUnmodified();
		updateTitle();
		return true;
	}

	private class AnalysisListCellRenderer extends DefaultListCellRenderer {

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			final JLabel retVal = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if(value instanceof OpNode) {
				retVal.setText(((OpNode)value).getName());
			}

			return retVal;
		}

	}

	private class AnalysisNodeTableTransferHandler extends TransferHandler {

		@Override
		public int getSourceActions(JComponent c) {
			return MOVE;
		}

		@Override
		public boolean importData(TransferSupport support) {
			boolean retVal = false;
			final JTable.DropLocation dropLocation =
					(JTable.DropLocation)support.getDropLocation();
			if(support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				try {
					@SuppressWarnings("unchecked")
					final List<File> fileList =
							(List<File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					addAnalyses(fileList);
					retVal = true;
				} catch (IOException | UnsupportedFlavorException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			} else {
				try {
	                // convert data to string
	                String s = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);

	                int idx = dropLocation.getRow();
	                int origIdx = Integer.parseInt(s);
	                OpNode srcNode = analysisNodes.remove(origIdx);

	                if(idx < 0) {
	                	idx = analysisNodes.size();
	                } else if(idx > origIdx) {
	                	--idx;
	                }
	                analysisNodes.add(idx, srcNode);

	                ((AnalysisNodeTableModel)analysisNodeTable.getModel()).fireTableDataChanged();
	                updateNodeLocations();

	                retVal = true;
	            } catch (IOException | UnsupportedFlavorException e) {
	            	LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
	            }
			}
			if(!retVal) {
				Toolkit.getDefaultToolkit().beep();
			}
            return retVal;
		}

		@Override
		public boolean canImport(TransferSupport support) {
			return support.isDataFlavorSupported(DataFlavor.stringFlavor)
					|| support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}

		@Override
		protected Transferable createTransferable(JComponent c) {
			int selectedRow = analysisNodeTable.getSelectedRow();
			return new StringSelection(""+selectedRow);
		}

	}

	private class AnalysisNodeTableModel extends AbstractTableModel {

		@Override
		public int getRowCount() {
			return analysisNodes.size();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			final OpNode node = analysisNodes.get(rowIndex);

			switch(columnIndex) {
			case 0:
				return node.getName();

			default:
				return null;
			}
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return "Analysis Name";

			default:
				return super.getColumnName(columnIndex);
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 1:
				return String.class;

			default:
				return super.getColumnClass(columnIndex);
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if(columnIndex == 0) {
				if(aValue.toString().trim().length() == 0) return;

				final OpNode node = analysisNodes.get(rowIndex);
				node.setName(aValue.toString());

				updateReportTitle((MacroNode)node);
			}
		}

	}

	private class TreeNodeRenderer extends DefaultTreeCellRenderer {

		public TreeNodeRenderer() {
			super();

			final ImageIcon folderIcon =
					(OSInfo.isMacOs() ? IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.GenericFolderIcon, IconSize.SMALL)
							: (OSInfo.isWindows() ? IconManager.getInstance().getSystemStockIcon(WindowsStockIcon.FOLDER, IconSize.SMALL)
									: IconManager.getInstance().getIcon("actions/open", IconSize.SMALL)));
			super.setClosedIcon(folderIcon);

			final ImageIcon folderOpenIcon =
					(OSInfo.isMacOs() ? IconManager.getInstance().getSystemStockIcon(MacOSStockIcon.OpenFolderIcon, IconSize.SMALL)
							: (OSInfo.isWindows() ? IconManager.getInstance().getSystemStockIcon(WindowsStockIcon.FOLDEROPEN, IconSize.SMALL)
									: IconManager.getInstance().getIcon("actions/open", IconSize.SMALL)));
			super.setOpenIcon(folderOpenIcon);

			final ImageIcon xmlIcon =
					IconManager.getInstance().getSystemIconForFileType("xml", "mimetypes/document", IconSize.SMALL);
			super.setLeafIcon(xmlIcon);
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			JLabel retVal = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			if(value instanceof DefaultMutableTreeNode) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
				if(node.getUserObject() instanceof URL) {
					final URL analysisURL = (URL)node.getUserObject();
					try {
						final String analysisFile = URLDecoder.decode(analysisURL.toString(), "UTF-8");
						final String analysisName = FilenameUtils.getBaseName(analysisFile);
						retVal.setText(analysisName);
					} catch (UnsupportedEncodingException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				}
			}

			return retVal;
		}

	}

	private class AnalysisSettingsPanel extends JPanel {

		private final MacroNode analysisNode;

		private JComboBox<OpNode> settingsNodeBox;
		private CardLayout settingsLayout;
		private JPanel settingsPanel;

		public AnalysisSettingsPanel(MacroNode analysisNode) {
			super();

			this.analysisNode = analysisNode;

			init();
			update();
		}

		private void init() {
			setLayout(new BorderLayout());

			this.settingsNodeBox = new JComboBox<>();
			this.settingsNodeBox.setRenderer(new AnalysisListCellRenderer());
			settingsNodeBox.addItemListener( (e) -> {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					settingsPanel.removeAll();

					final OpNode node = (OpNode)e.getItem();
					settingsLayout.show(settingsPanel, Integer.toHexString(node.hashCode()));
				}
			});
			this.settingsLayout = new CardLayout();
			this.settingsPanel = new JPanel(settingsLayout);

			add(settingsNodeBox, BorderLayout.NORTH);
			add(settingsPanel, BorderLayout.CENTER);
		}

		private void update() {
			final OpGraph analysisGraph = analysisNode.getGraph();
			final WizardExtension analysisExt = analysisGraph.getExtension(WizardExtension.class);

			settingsPanel.removeAll();
			final OpNode[] settingsNodes = new OpNode[analysisExt.size()];
			for(int i = 0; i < analysisExt.size(); i++) {
				final OpNode node = analysisExt.getNode(i);
				final NodeSettings nodeSettings = node.getExtension(NodeSettings.class);
				if(nodeSettings != null) {
					settingsNodes[i] = node;

					settingsPanel.add(nodeSettings.getComponent(getModel().getDocument()),
							Integer.toHexString(node.hashCode()));
				}
			}

			final DefaultComboBoxModel<OpNode> boxModel = new DefaultComboBoxModel<>(settingsNodes);
			settingsNodeBox.setModel(boxModel);

			settingsNodeBox.setSelectedIndex(0);
			settingsLayout.show(settingsPanel, Integer.toHexString(settingsNodes[0].hashCode()));
		}

	}

}
