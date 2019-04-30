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
package ca.phon.app.opgraph.editor;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
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
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.util.ClasspathUtils;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.hedlund.desktopicons.MacOSStockIcon;
import ca.hedlund.desktopicons.WindowsStockIcon;
import ca.hedlund.tst.TernaryTree;
import ca.phon.app.log.LogUtil;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.opgraph.nodes.PhonScriptNode;
import ca.phon.app.opgraph.nodes.query.QueryNode;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.opgraph.wizard.edits.NodeWizardOptionalsEdit;
import ca.phon.extensions.ExtensionSupport;
import ca.phon.extensions.IExtendable;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.app.OpgraphIO;
import ca.phon.opgraph.app.edits.graph.AddNodeEdit;
import ca.phon.opgraph.app.edits.graph.DeleteNodesEdit;
import ca.phon.opgraph.app.edits.graph.MoveNodesEdit;
import ca.phon.opgraph.app.extensions.NodeSettings;
import ca.phon.opgraph.extensions.NodeMetadata;
import ca.phon.opgraph.library.instantiators.Instantiator;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.opgraph.nodes.general.MacroNodeData;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.Project;
import ca.phon.query.script.QueryName;
import ca.phon.query.script.QueryScript;
import ca.phon.query.script.QueryScriptLibrary;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;
import ca.phon.ui.ButtonPopup;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.DropDownButton;
import ca.phon.ui.action.PhonActionEvent;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.decorations.TitledPanel;
import ca.phon.ui.jbreadcrumb.BreadcrumbEvent;
import ca.phon.ui.jbreadcrumb.BreadcrumbListener;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.ui.nativedialogs.MessageDialogProperties;
import ca.phon.ui.nativedialogs.NativeDialogs;
import ca.phon.ui.nativedialogs.OpenDialogProperties;
import ca.phon.ui.nativedialogs.SaveDialogProperties;
import ca.phon.util.OSInfo;
import ca.phon.util.Tuple;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;
import ca.phon.util.resources.ResourceLoader;
import ca.phon.worker.PhonWorker;

public class SimpleEditorPanel extends JPanel implements IExtendable {

	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(SimpleEditorPanel.class.getName());

	private final static int Y_START = 50;
	private final static int X_START = 400;
	private final static int Y_SEP = 150;
	
	/**
	 * The library of items which will be displayed in the
	 * selection tree.
	 */
	private final OpGraphLibrary library;

	/**
	 * {@link Instantiator} for new {@link OpGraph} {@link MacroNode}s
	 */
	private final Instantiator<MacroNode> nodeInstantiator;

	private BiFunction<QueryScript, OpGraph, MacroNode> queryNodeInstantiator;
	
	private final BiFunction<OpGraph, Project, Runnable> runFactory;
	
	private JToolBar toolbar;
	private DropDownButton saveButton;
	private JButton browseButton;
	private DropDownButton addButton;
	private JButton removeButton;
	private JButton moveUpButton;
	private JButton moveDownButton;
	private JButton runButton;

	private TitledPanel leftTitledPanel;
	private JTree documentTree;
	
	private TitledPanel listTitledPanel;
	private JPanel listTopPanel;
	private JXTable nodeTable;
	private List<MacroNode> macroNodes = Collections.synchronizedList(new ArrayList<>());
	
	private TitledPanel settingsTitledPanel;
	private CardLayout cardLayout;
	private Map<String, DocumentSettingsPanel> panelMap = new HashMap<>();
	private JPanel nodePanel;
	
	private JXBusyLabel busyLabel;
	private JLabel statusLabel;
	
	private EditorModelInstantiator modelInstantiator;
	
	private OpgraphEditorModel model;
	
	private final Project project;

	private boolean includeQueries = false;
	
	private final List<Consumer<DocumentError>> documentErrorListeners = new ArrayList<>();
	
	private final ExtensionSupport extSupport = new ExtensionSupport(SimpleEditorPanel.class, this);
	
	private boolean modified = false;
	
	/**
	 * Constructor
	 *
	 * @param project if <code>null</code> project graphs will not be displayed
	 * @param library library display in add item dialog
	 * @param graph
	 * @param modelInstantiator the editor model instantiator
	 * @param nodeInstantiator instantiator for nodes created by adding documents from the library
	 * @param queryNodeInstantiator instantiator for nodes created by adding queries to the doucment
	 * @param runFactory factory for runnables used to execute graphs
	 */
	public SimpleEditorPanel(Project project, OpGraphLibrary library, 
			EditorModelInstantiator modelInstantiator, Instantiator<MacroNode> nodeInstantiator,
			BiFunction<QueryScript, OpGraph, MacroNode> queryNodeInstantiator,
			BiFunction<OpGraph, Project, Runnable> runFactory) {
		this(project, library, new OpGraph(), modelInstantiator, nodeInstantiator, queryNodeInstantiator, runFactory);
	}
	
	/**
	 * Constructor
	 *
	 * @param project if <code>null</code> project graphs will not be displayed
	 * @param library library display in add item dialog
	 * @param graph
	 * @param modelInstantiator the editor model instantiator
	 * @param nodeInstantiator instantiator for nodes created by adding documents from the library
	 * @param queryNodeInstantiator instantiator for nodes created by adding queries to the doucment
	 * @param runFactory factory for runnables used to execute graphs
	 */
	public SimpleEditorPanel(Project project, OpGraphLibrary library, OpGraph graph,
			EditorModelInstantiator modelInstantiator, Instantiator<MacroNode> nodeInstantiator,
			BiFunction<QueryScript, OpGraph, MacroNode> queryNodeInstantiator,
			BiFunction<OpGraph, Project, Runnable> runFactory) {
		super();

		this.project = project;
		this.library = library;
		this.nodeInstantiator = nodeInstantiator;
		this.queryNodeInstantiator = queryNodeInstantiator;
		this.runFactory = runFactory;

		this.modelInstantiator = modelInstantiator;
		
		model = modelInstantiator.createModel(graph);
		resetAndUpdate();
		
		init();
		extSupport.initExtensions();
	}
	
	private UndoableEditListener editListener = new UndoableEditListener() {

		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			setModified(true);
		}
		
	};
	
	private BreadcrumbListener<OpGraph, String> breadcrumbListener = new BreadcrumbListener<OpGraph, String>() {
		
		@Override
		public void breadCrumbEvent(BreadcrumbEvent<OpGraph, String> evt) {
			if(evt.getValue().equals("root")) {
				// root graph has changed - reset and update
				resetAndUpdate();
			}
		}
		
	};
	
	private void resetAndUpdate() {
		// read macro nodes from graph extension
		macroNodes.clear();
		model.getDocument().getUndoSupport().addUndoableEditListener( editListener );
		
		SimpleEditorExtension simpleEditorExt = getGraph().getExtension(SimpleEditorExtension.class);
		if(simpleEditorExt != null)
			this.macroNodes = simpleEditorExt.getMacroNodes();
		else {
			simpleEditorExt = new SimpleEditorExtension(macroNodes);
			getGraph().putExtension(SimpleEditorExtension.class, simpleEditorExt);
		}
		if(nodeTable != null)
			((NodeTableModel)nodeTable.getModel()).fireTableDataChanged();
		
		panelMap.clear();
	}
	
	private void expandAllDocuments(TreePath path) {
		final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode)path.getLastPathComponent();
		for(int i = 0; i < treeNode.getChildCount(); i++) {
			final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)treeNode.getChildAt(i);
			final TreePath tp = path.pathByAddingChild(childNode);
			documentTree.expandPath(tp);
			
			if(childNode.getChildCount() > 0) {
				expandAllDocuments(tp);
			}
		}
	}
	
	private void init() {
		// document tree
		TreeModel treeModel = createTreeModel();
		documentTree = new JTree(treeModel);
		documentTree.setRootVisible(false);
		documentTree.setVisibleRowCount(20);
		documentTree.setCellRenderer(new TreeNodeRenderer());
		documentTree.setDragEnabled(true);
		documentTree.setTransferHandler(new NodeTreeTransferHandler());
		documentTree.addMouseListener(new MouseInputAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					final int clickedRow = documentTree.getRowForLocation(e.getX(), e.getY());
					if(clickedRow >= 0 && clickedRow < documentTree.getRowCount()) {
						TreePath clickedPath = documentTree.getPathForRow(clickedRow);
						if(((DefaultMutableTreeNode)clickedPath.getLastPathComponent()).isLeaf())
							addSelectedDocuments(documentTree);
					}
				}
			}

		});
		final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
		final TreePath rootPath = new TreePath(rootNode);
		expandAllDocuments(rootPath);
		
		// create components for popup window selection
		final ImageIcon saveIcn =
				IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		JPopupMenu saveMenu = new JPopupMenu();
		var saveAct = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(getCurrentFile() != null) {
					try {
						saveData();
					} catch (IOException e1) {
						Toolkit.getDefaultToolkit().beep();
						LogUtil.severe(e1);
					}
				}
			}
		};
		saveAct.putValue(Action.SHORT_DESCRIPTION, "Save");
		saveAct.putValue(Action.SMALL_ICON, saveIcn);
//		saveAct.putValue(DropDownButton.ARROW_ICON_GAP, 2);
//		saveAct.putValue(DropDownButton.ARROW_ICON_POSITION, SwingConstants.BOTTOM);
		saveAct.putValue(DropDownButton.BUTTON_POPUP, saveMenu);
		
		saveButton = new DropDownButton(saveAct);
		saveButton.setOnlyPopup(true);
		saveButton.getButtonPopup().addPropertyChangeListener(ButtonPopup.POPUP_VISIBLE, (e) -> {
			if(Boolean.parseBoolean(e.getNewValue().toString())) {
				setupSaveMenu(saveMenu);
			}
		});
		addPropertyChangeListener("currentFile", (e) -> {
			saveButton.setOnlyPopup(e.getNewValue() == null);
		});
		
		final PhonUIAction browseAct = new PhonUIAction(this, "onBrowse");
		final ImageIcon openIcn =
				IconManager.getInstance().getIcon("actions/document-open", IconSize.SMALL);
		browseAct.putValue(PhonUIAction.SMALL_ICON, openIcn);
		browseAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open " + getModel().getNoun().getObj1() + "file...");
		browseButton = new JButton(browseAct);

		final ImageIcon addIcn =
				IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final PhonUIAction addAct = new PhonUIAction(this, "addSelectedDocuments", documentTree);
		addAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add selected items to " + getModel().getNoun().getObj1());
		addAct.putValue(PhonUIAction.SMALL_ICON, addIcn);
		final JPopupMenu addMenu = new JPopupMenu();
		
		final PhonUIAction addWithText = new PhonUIAction(this, "addSelectedDocuments", documentTree);
		addWithText.putValue(PhonUIAction.NAME, addAct.getValue(PhonUIAction.SHORT_DESCRIPTION));
		addWithText.putValue(PhonUIAction.SMALL_ICON, addAct.getValue(PhonUIAction.SMALL_ICON));
		addMenu.add(new JMenuItem(addWithText));
		
		final PhonUIAction appendAct = new PhonUIAction(this, "onAppend");
		appendAct.putValue(PhonUIAction.NAME, "Append " + getModel().getNoun().getObj1() + " from disk...");
		appendAct.putValue(PhonUIAction.SMALL_ICON, browseAct.getValue(PhonUIAction.SMALL_ICON));
		addMenu.add(new JMenuItem(appendAct));
		
		addAct.putValue(DropDownButton.BUTTON_POPUP, addMenu);
		addButton = new DropDownButton(addAct);

		final ImageIcon removeIcn =
				IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction removeAct = new PhonUIAction(this, "onRemove");
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected " + getModel().getNoun().getObj1());
		removeAct.putValue(PhonUIAction.SMALL_ICON, removeIcn);
		final KeyStroke removeKs = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		removeButton = new JButton(removeAct);

		final ImageIcon upIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-up", IconSize.SMALL);
		final PhonUIAction upAct = new PhonUIAction(this, "onMoveUp");
		upAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected " + getModel().getNoun().getObj1() + " up");
		upAct.putValue(PhonUIAction.SMALL_ICON, upIcn);
		final KeyStroke upKs = KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		moveUpButton = new JButton(upAct);

		final ImageIcon downIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-down", IconSize.SMALL);
		final PhonUIAction downAct = new PhonUIAction(this, "onMoveDown");
		downAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected " + getModel().getNoun().getObj1() + " down");
		downAct.putValue(PhonUIAction.SMALL_ICON, downIcn);
		final KeyStroke downKs = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx());
		moveDownButton = new JButton(downAct);

		final ImageIcon runIcn =
				IconManager.getInstance().getIcon("actions/media-playback-start-7", IconSize.SMALL);
		final PhonUIAction runAct = new PhonUIAction(this, "onRun");
		runAct.putValue(PhonUIAction.NAME, "Run " + getModel().getNoun().getObj1());
		runAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Run " + getModel().getNoun().getObj1());
		runAct.putValue(PhonUIAction.SMALL_ICON, runIcn);
		runButton = new JButton(runAct);

		nodeTable = new JXTable(new NodeTableModel());
		nodeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nodeTable.getSelectionModel().addListSelectionListener( (e) -> {
			int selectedIdx = nodeTable.getSelectedRow();
			if(selectedIdx >= 0 && selectedIdx < macroNodes.size()) {
				showDocumentSettings(macroNodes.get(selectedIdx));
			} else {
				settingsTitledPanel.setTitle("Settings");
				cardLayout.show(nodePanel, "none");
			}
		});
		nodeTable.setSortable(false);
		nodeTable.setDragEnabled(true);
		nodeTable.setTransferHandler(new NodeTableTransferHandler());
		nodeTable.setDropMode(DropMode.INSERT);
		nodeTable.setVisibleRowCount(10);

		final ActionMap am = nodeTable.getActionMap();
		final InputMap inputMap = nodeTable.getInputMap(JComponent.WHEN_FOCUSED);

		inputMap.put(upKs, "moveUp");
		am.put("moveUp", upAct);

		inputMap.put(downKs, "moveDown");
		am.put("moveDown", downAct);

		inputMap.put(removeKs, "delete");
		am.put("delete", removeAct);
		
		final JScrollPane documentScroller = new JScrollPane(documentTree);
		documentScroller.setPreferredSize(new Dimension(350, 0));
		
		cardLayout = new CardLayout();
		nodePanel = new JPanel(cardLayout);
		
		final ImageIcon settingsIcn = IconManager.getInstance().getIcon("actions/settings-white", IconSize.SMALL);
		settingsTitledPanel = new TitledPanel("Settings");
		settingsTitledPanel.getContentContainer().setLayout(new BorderLayout());
		settingsTitledPanel.getContentContainer().add(nodePanel, BorderLayout.CENTER);
		settingsTitledPanel.setLeftDecoration(new JLabel(settingsIcn));
		nodePanel.add(new JPanel(), "none");
		
		// setup settings column
		final JScrollPane nodeScroller = new JScrollPane(nodeTable);

		toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.add(saveButton);
		toolbar.add(browseButton);
		toolbar.addSeparator();

		toolbar.add(addButton);
		toolbar.add(removeButton);
		toolbar.addSeparator();
		
		toolbar.add(moveUpButton);
		toolbar.add(moveDownButton);
		
		if(this.runFactory != null) {
			toolbar.addSeparator();
			toolbar.add(runButton);
		}

		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		busyLabel.getBusyPainter().setHighlightColor(Color.white);
		statusLabel = new JLabel();
		
		final String listTitle = StringUtils.capitalize(getModel().getNoun().getObj1()) + " List";
		
		listTopPanel = new JPanel(new VerticalLayout(0));
		
		leftTitledPanel = new TitledPanel(StringUtils.capitalize(getModel().getNoun().getObj2()), documentScroller);
		listTitledPanel = new TitledPanel(listTitle);
		listTitledPanel.getContentContainer().setLayout(new BorderLayout());
		listTitledPanel.getContentContainer().add(listTopPanel, BorderLayout.NORTH);
		listTitledPanel.getContentContainer().add(nodeScroller, BorderLayout.CENTER);
		listTitledPanel.setLeftDecoration(busyLabel);
		
		final JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		rightSplit.setLeftComponent(listTitledPanel);
		rightSplit.setRightComponent(settingsTitledPanel);
		SwingUtilities.invokeLater( () -> rightSplit.setDividerLocation(0.33f) );
		
		final JSplitPane splitPane = new JSplitPane();
		splitPane.setLeftComponent(leftTitledPanel);
		splitPane.setRightComponent(rightSplit);
		
		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		add(splitPane, BorderLayout.CENTER);
	}
		
	public OpgraphEditorModel getModel() {
		return this.model;
	}
	
	public List<MacroNode> getMacroNodes() {
		return Collections.unmodifiableList(this.macroNodes);
	}
	
	public OpGraph getGraph() {
		return this.model.getDocument().getGraph();
	}
	
	public Project getProject() {
		return this.project;
	}
	
	public JButton getRunButton() {
		return this.runButton;
	}
	
	public JToolBar getToolbar() {
		return this.toolbar;
	}
	
	public TitledPanel getDocumentsPanel() {
		return this.leftTitledPanel;
	}
	
	public TitledPanel getListPanel() {
		return this.listTitledPanel;
	}
	
	public JPanel getListTopPanel() {
		return this.listTopPanel;
	}
	
	public TitledPanel getSettingsPanel() {
		return this.settingsTitledPanel;
	}
	
	public void addSelectedDocuments(JTree tree) {
		addSelectedDocuments(tree, -1);
	}
	
	public void addSelectedDocuments(JTree tree, int idx) {
		final TreePath[] selectedPaths = tree.getSelectionPaths();
		
		List<Object> selectedDocuments = new ArrayList<>();
		for(TreePath path:selectedPaths) {
			querySelectedDocuments((DefaultMutableTreeNode)path.getLastPathComponent(), selectedDocuments);
		}
		
		final AddDocumentsWorker worker = new AddDocumentsWorker(selectedDocuments, idx);
		worker.execute();
	}
	
	private void querySelectedDocuments(DefaultMutableTreeNode node, List<Object> documents) {
		if(node.isLeaf()) {
			documents.add(node.getUserObject());
		} else {
			for(int i = 0; i < node.getChildCount(); i++) {
				final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
				querySelectedDocuments(childNode, documents);
			}
		}
	}
	
	private void addDocuments(List<File> fileList) {
		addDocuments(fileList, -1);
	}

	private void addDocuments(List<File> fileList, int idx) {
		busyLabel.setBusy(true);
		PhonWorker.getInstance().invokeLater( () -> {
			List<Object> documents = new ArrayList<>();
			for(File f:fileList) {
				if(f.isFile()
						&& f.getName().endsWith(".xml")) {
					try {
						OpgraphIO.read(f);
						documents.add(f.toURI().toURL());
					} catch (IOException e) {
						try {
							final QueryScript queryScript = new QueryScript(f.toURI().toURL());
							documents.add(queryScript);
						} catch (IOException e2) {
							final MessageDialogProperties props = new MessageDialogProperties();
							props.setParentWindow(CommonModuleFrame.getCurrentFrame());
							props.setHeader("Add Analysis");
							props.setTitle("Unable to add analysis");
							props.setMessage("Document is not an analysis or query");
							props.setOptions(MessageDialogProperties.okOptions);
							NativeDialogs.showMessageDialog(props);
						}
					}
				}
			}
			final AddDocumentsWorker worker = new AddDocumentsWorker(documents, idx);
			worker.execute();
		});
	}
	
	public void addGraph(OpGraph graph) {
		addGraph(graph, -1);
	}
	
	public void addGraph(OpGraph graph, int idx) {
		final AddDocumentsWorker worker = new AddDocumentsWorker(List.of(graph), idx);
		worker.execute();
	}

	public void addNode(MacroNode node, int idx) {
		getModel().getDocument().getUndoSupport().beginUpdate();
		
		final AddNodeEdit addNodeEdit =
				new AddNodeEdit(getGraph(), node, X_START, Y_START + macroNodes.size() * Y_SEP);
		model.getDocument().getUndoSupport().postEdit(addNodeEdit);

		final NodeWizardOptionalsEdit optEdit =
				new NodeWizardOptionalsEdit(getGraph(), getGraph().getExtension(WizardExtension.class), node, true, true);
		model.getDocument().getUndoSupport().postEdit(optEdit);
		
		updateReportTitle(node);

		if(idx >= 0 && idx < macroNodes.size())
			macroNodes.add(idx, node);
		else
			macroNodes.add(node);
		
		updateNodeLocations();
		
		getModel().getDocument().getUndoSupport().endUpdate();
		
		int rowIdx = (idx >= 0 ? idx : macroNodes.size()-1);
		((NodeTableModel)nodeTable.getModel()).fireTableRowsInserted(rowIdx, rowIdx);
	}
	
	public MacroNode addQuery(QueryScript queryScript) {
		return addQuery(queryScript, new OpGraph());
	}
	
	public MacroNode addQuery(QueryScript queryScript, OpGraph reportGraph) {
		return addQuery(queryScript, reportGraph, -1);
	}
	
	public MacroNode addQuery(QueryScript queryScript, OpGraph reportGraph, int idx) {
		final MacroNode node = queryNodeInstantiator.apply(queryScript, reportGraph);
		setupQueryNameListener(node);
		final AddDocumentsWorker worker = new AddDocumentsWorker(List.of(node), idx);
		worker.execute();
		
		return node;
	}
	
	private void setupQueryNameListener(MacroNode node) {
		// listen for changes to the query name combo box
		Optional<OpNode> opNode = 
				node.getGraph().getVertices().stream().filter( (v) -> v instanceof QueryNode ).findFirst();
		if(opNode.isPresent()) {
			QueryNode queryNode = (QueryNode)opNode.get();
			queryNode.getComponent(null);
			final JComboBox<String> queryNameBox = queryNode.getQueryHistoryAndNameToolbar().getQueryNameBox();
			queryNameBox.addItemListener( (e) -> {
				Object selectedItem = queryNameBox.getSelectedItem();
				if(selectedItem != null && node.getExtension(QueryName.class) == null) {
					String queryName = selectedItem.toString();					
					node.setName(queryName);
				} else {
					// default query name
					QueryName qn = queryNode.getQueryScript().getExtension(QueryName.class);
					String queryName = "Query";
					
					if(qn != null) {
						queryName = qn.getName();
						if(qn.getLocation() != null) {
							try {
								String path = URLDecoder.decode(qn.getLocation().getPath(), "UTF-8");
								File file = new File(path);
								String filename = file.getName();
								if(filename.lastIndexOf('.') > 0) {
									filename = filename.substring(0, filename.lastIndexOf('.'));
								}
								queryName = filename;
							} catch (UnsupportedEncodingException e1) {
								LogUtil.warning(e1);
							}
						}
					}
					
					if(node.getExtension(QueryName.class) != null) {
						node.setName(node.getExtension(QueryName.class).getName());
					} else {
						node.setName(queryName);
					}
				}
				updateReportTitle(node);
				nodeTable.repaint();
			});
		}
	}
	
	public void addDocument(File file) throws IOException {
		addDocument(file.toURI().toURL(), -1);
	}

	public void addDocument(File file, int idx) throws IOException {
		addDocument(file.toURI().toURL(), idx);
	}
	
	public void addDocument(URL documentURL) {
		addDocument(documentURL, -1);
	}

	/*
	 * This method should be executed on a background thread
	 */
	public void addDocument(URL documentURL, int idx) {
		final AddDocumentsWorker worker = new AddDocumentsWorker(List.of(documentURL), idx);
		worker.execute();
	}

	public void onRemove() {
		final int selectedRow = nodeTable.getSelectedRow();
		if(selectedRow >= 0 && selectedRow < macroNodes.size()) {
			final OpNode selectedNode = macroNodes.get(selectedRow);
			
			if(panelMap.containsKey(selectedNode.getId())) {
				cardLayout.removeLayoutComponent(panelMap.get(selectedNode.getId()));
				panelMap.remove(selectedNode.getId());
			}

			getModel().getDocument().getUndoSupport().beginUpdate();
			
			final DeleteNodesEdit removeEdit =
					new DeleteNodesEdit(getGraph(), Collections.singleton(selectedNode));
			getModel().getDocument().getUndoSupport().postEdit(removeEdit);

			macroNodes.remove(selectedRow);
			((NodeTableModel)nodeTable.getModel()).fireTableRowsDeleted(selectedRow, selectedRow);

			updateNodeLocations();

			getModel().getDocument().getUndoSupport().endUpdate();
			
			if(macroNodes.size() > 0) {
				if(selectedRow < macroNodes.size()) {
					nodeTable.setRowSelectionInterval(selectedRow, selectedRow);
				} else {
					nodeTable.setRowSelectionInterval(macroNodes.size()-1, macroNodes.size()-1);
				}
			}
		}
	}

	public void onRename() {
		final int selectedRow = nodeTable.getSelectedRow();
		if(selectedRow >= 0 && selectedRow < macroNodes.size()) {
			nodeTable.editCellAt(selectedRow, 0);
			nodeTable.requestFocusInWindow();
		}
	}

	public void onMoveUp() {
		final int selectedRow = nodeTable.getSelectedRow();
		if(selectedRow > 0 && selectedRow < macroNodes.size()) {
			final MacroNode selectedNode = macroNodes.get(selectedRow);

			int newLocation = selectedRow - 1;
			macroNodes.remove(selectedRow);
			macroNodes.add(newLocation, selectedNode);

			((NodeTableModel)nodeTable.getModel()).fireTableRowsDeleted(selectedRow, selectedRow);
			((NodeTableModel)nodeTable.getModel()).fireTableRowsInserted(newLocation, newLocation);
			nodeTable.getSelectionModel().setSelectionInterval(newLocation, newLocation);

			getModel().getDocument().getUndoSupport().beginUpdate();
            updateNodeLocations();
            getModel().getDocument().getUndoSupport().endUpdate();
		}
	}

	public void onMoveDown() {
		final int selectedRow = nodeTable.getSelectedRow();
		if(selectedRow >= 0 && selectedRow < macroNodes.size()-1) {
			final MacroNode selectedNode = macroNodes.get(selectedRow);

			int newLocation = selectedRow + 1;
			macroNodes.remove(selectedRow);
			macroNodes.add(newLocation, selectedNode);

			((NodeTableModel)nodeTable.getModel()).fireTableRowsDeleted(selectedRow, selectedRow);
			((NodeTableModel)nodeTable.getModel()).fireTableRowsInserted(newLocation, newLocation);
			nodeTable.getSelectionModel().setSelectionInterval(newLocation, newLocation);

			getModel().getDocument().getUndoSupport().beginUpdate();
            updateNodeLocations();
            getModel().getDocument().getUndoSupport().endUpdate();
		}
	}

	public void onShowSettings() {
		final int selectedRow = nodeTable.getSelectedRow();
		if(selectedRow >= 0 && selectedRow < macroNodes.size()) {
			final MacroNode selectedNode = (MacroNode)macroNodes.get(selectedRow);
			showDocumentSettings(selectedNode);
		}
	}

	public void onRun() {
		getModel().validate();
		final Runnable toRun = runFactory.apply(getGraph(), getProject());
		PhonWorker.getInstance().invokeLater(toRun);
	}

	public void onOpenInComposer() {
		final EntryPointArgs epArgs = new EntryPointArgs();
		epArgs.put(OpgraphEditorEP.OPGRAPH_MODEL_KEY, getModel());
		PluginEntryPointRunner.executePluginInBackground(OpgraphEditorEP.EP_NAME, epArgs);
	}
	
	public void onAppend() {
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setAllowMultipleSelection(true);
		props.setCanChooseDirectories(false);
		props.setCanChooseFiles(true);
		props.setFileFilter(FileFilter.xmlFilter);
		props.setInitialFolder(getModel().getDefaultFolder());
		props.setTitle("Add documents");
		props.setPrompt("Add");
		props.setRunAsync(false);

		List<String> selectedFiles = NativeDialogs.showOpenDialog(props);
		final List<File> fileList =
				(selectedFiles != null ? selectedFiles.stream().map( (s) -> new File(s) ).collect(Collectors.toList()) : new ArrayList<>());
		if(fileList.size() > 0) {
			addDocuments(fileList);
		}
	}

	public void onBrowse() {
		if(isModified() && getMacroNodes().size() > 0) {
			final MessageDialogProperties msgProps = new MessageDialogProperties();
			msgProps.setRunAsync(false);
			msgProps.setParentWindow(CommonModuleFrame.getCurrentFrame());
			msgProps.setTitle("Composer");
			msgProps.setHeader("Save Changes");
			msgProps.setMessage("Save current changes before opening new document?");
			msgProps.setOptions(MessageDialogProperties.yesNoCancelOptions);
			
			int retVal = NativeDialogs.showMessageDialog(msgProps);
			if(retVal == 0) {
				try {
					saveData();
				} catch (IOException e) {
					Toolkit.getDefaultToolkit().beep();
					LogUtil.severe(e);
					return;
				}
			} else if(retVal == 2) {
				return;
			}
		}
		
		final OpenDialogProperties props = new OpenDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setAllowMultipleSelection(false);
		props.setCanChooseDirectories(false);
		props.setCanChooseFiles(true);
		props.setFileFilter(FileFilter.xmlFilter);
		props.setInitialFolder(getModel().getDefaultFolder());
		props.setTitle("Open " + getModel().getNoun().getObj1());
		props.setPrompt("Open");
		props.setRunAsync(false);

		List<String> selectedFiles = NativeDialogs.showOpenDialog(props);
		final List<File> fileList =
				(selectedFiles != null ? selectedFiles.stream().map( (s) -> new File(s) ).collect(Collectors.toList()) : new ArrayList<>());
		if(fileList.size() > 0) {
			this.model.getDocument().getUndoSupport().removeUndoableEditListener(editListener);
			
			File selectedFile = fileList.get(0);
			OpGraph graph;
			try {
				graph = OpgraphIO.read(selectedFile);
				
				// make sure the graph was created using the simple composer!
				SimpleEditorExtension ext = graph.getExtension(SimpleEditorExtension.class);
				if(ext == null) {
					throw new IOException("Selected graph was not created using the simple composer, use Composer (advanced) instead");
				}
				
				// ensure that our wizard extension types match!
				if(graph.getExtension(WizardExtension.class).getClass() != 
						getGraph().getExtension(WizardExtension.class).getClass()) {
					throw new IOException("Selected file is not a valid " + getModel().getNoun().getObj1() + " document");
				}
				
				this.model = modelInstantiator.createModel(graph);
				resetAndUpdate();
				
				setCurrentFile(fileList.get(0));
				getModel().getDocument().markAsUnmodified();
				getModel().getDocument().getUndoManager().discardAllEdits();
				setModified(false);
			} catch (IOException e) {
				LogUtil.warning(e);
				Toolkit.getDefaultToolkit().beep();
				
				CommonModuleFrame cmf = CommonModuleFrame.getCurrentFrame();
				if(cmf != null) {
					cmf.showMessageDialog("Unable to open file", e.getLocalizedMessage(), new String[] {"Ok"});
				}
			}
		}
	}
	
	public boolean isIncludeQueries() {
		return this.includeQueries;
	}

	public void setIncludeQueries(boolean includeQueries) {
		this.includeQueries = includeQueries;
		
		documentTree.setModel(createTreeModel());
		expandAllDocuments(new TreePath(documentTree.getModel().getRoot()));
	}
	
	public void onShowList() {
		cardLayout.show(nodePanel, "node_table");
		listTitledPanel.setLeftDecoration(busyLabel);
		listTitledPanel.setTitle(StringUtils.capitalize(getModel().getNoun().getObj1()) + " List");
	}
	
	public void showDocumentSettings(MacroNode documentNode) {
		final String id = documentNode.getId();
		if(!panelMap.containsKey(id)) {
			final DocumentSettingsPanel settingsPanel = new DocumentSettingsPanel(documentNode);
			nodePanel.add(settingsPanel, id);
			panelMap.put(id, settingsPanel);
		}
		cardLayout.show(nodePanel, id);
		settingsTitledPanel.setTitle(documentNode.getName() + " Settings");
	}

	public void onCloseSettings(JDialog dialog) {
		dialog.setVisible(false);
		dialog.dispose();
	}
	
	public File getCurrentFile() {
		return getModel().getDocument().getSource();
	}
	
	public boolean isModified() {
		return this.modified;
	}
	
	public void setModified(boolean modified) {
		boolean wasModified = this.modified;
		this.modified = modified;
		firePropertyChange("modified", wasModified, modified);
	}

	public void setCurrentFile(File source) {
		File oldSource = getCurrentFile();
		getModel().getDocument().setSource(source);

		super.firePropertyChange("currentFile", oldSource, source);
		
		// update node title for wizard
		final WizardExtension ext = getModel().getDocument().getRootGraph().getExtension(WizardExtension.class);
		if(ext != null) {
			final String name = FilenameUtils.getBaseName(source.getAbsolutePath());
			ext.setWizardTitle(name);
		}
	}

	public boolean chooseFile(String initialFolder) {
		final SaveDialogProperties props = new SaveDialogProperties();
		props.setParentWindow(CommonModuleFrame.getCurrentFrame());
		props.setCanCreateDirectories(true);
		props.setFileFilter(new OpgraphFileFilter());
		props.setRunAsync(false);
		props.setTitle("Save " + getModel().getNoun().getObj1());

		if(getCurrentFile() != null) {
			final File parentFolder = getCurrentFile().getParentFile();
			final String name = getCurrentFile().getName();

			if(initialFolder == null)
				props.setInitialFolder(parentFolder.getAbsolutePath());
			props.setInitialFile(name);
		} else {
			props.setInitialFolder(initialFolder);
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
	
	public void saveInFolder(PhonActionEvent pae) {
		final String path = (String)pae.getData();
		if(path != null) {
			File folder = new File(path);
			if(!folder.exists()) {
				folder.mkdirs();
			}
		}
		if(chooseFile(path)) {
			try {
				saveData();
			} catch (IOException e) {
				Toolkit.getDefaultToolkit().beep();
				LogUtil.severe(e);
			}
		}
	}

	protected void setupSaveMenu(JPopupMenu menu) {
		menu.removeAll();
		
		final ImageIcon saveAsIcn = IconManager.getInstance().getIcon("actions/document-save-as", IconSize.SMALL);
		
		PhonUIAction saveUserAct = new PhonUIAction(this, "saveInFolder", library.getUserFolderPath());
		saveUserAct.putValue(PhonUIAction.NAME, "Save in user library...");
		saveUserAct.putValue(PhonUIAction.SMALL_ICON, saveAsIcn);
		menu.add(saveUserAct);
		
		PhonUIAction saveProjectAct = new PhonUIAction(this, "saveInFolder", library.getProjectFolderPath(getProject()));
		saveProjectAct.putValue(PhonUIAction.NAME, "Save in project library...");
		saveProjectAct.putValue(PhonUIAction.SMALL_ICON, saveAsIcn);
		menu.add(saveProjectAct);
		
		PhonUIAction saveAsAct = new PhonUIAction(this, "saveInFolder", library.getProjectFolderPath(getProject()));
		saveAsAct.putValue(PhonUIAction.NAME, "Save as...");
		saveAsAct.putValue(PhonUIAction.SMALL_ICON, saveAsIcn);
		menu.add(saveAsAct);
	}
	
	public boolean saveData() throws IOException {
		if(!getModel().validate()) return false;
		if(getCurrentFile() == null) {
			if(!chooseFile(null)) return false;
		}
		
		OpgraphIO.write(getModel().getDocument().getRootGraph(), getCurrentFile());
		setModified(false);
		
		return true;
	}
	
	private JPanel createDocumentsPanel(TreeModel treeModel) {
		JPanel retVal = new JPanel(new VerticalLayout());
		
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
		for(int i = 0; i < rootNode.getChildCount(); i++) {
			TreeNode cnode = rootNode.getChildAt(i);
			setupDocumentsPanel(cnode, retVal);
		}
		
		return retVal;
	}
	
	private void setupDocumentsPanel(TreeNode treeNode, JPanel panel) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)treeNode;

		JPanel docPanel = new JPanel(new GridLayout(0, 3));
		
		for(int i = 0; i < node.getChildCount(); i++) {
			TreeNode cnode = node.getChildAt(i);
			
			if(cnode.isLeaf()) {
				DocumentLabel docLabel = new DocumentLabel(((DefaultMutableTreeNode)cnode).getUserObject());
				docPanel.add(docLabel);
			} else {
				setupDocumentsPanel(cnode, panel);
			}
		}
		
		panel.add(docPanel);
	}
	
	/**
	 * If the given analysis/report node has a settings node 'Parameters' which
	 * is a {@link PhonScriptNode} and has a parameter 'reportTitle' this
	 * method will change that parameter value to be the name of the
	 * analysis node.
	 */
	private void updateReportTitle(MacroNode documentNode) {
		// find the 'Parameters' settings node
		final OpGraph graph = documentNode.getGraph();
		final WizardExtension wizardExtension = graph.getExtension(WizardExtension.class);
		OpNode parametersNode = null;
		for(OpNode node:graph.getVertices()) {
			if(node.getName().equals("Parameters") && node instanceof PhonScriptNode
					&& graph.getNodePath(node.getId()).size() == 1) {
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
						sp.setValue("reportTitle", documentNode.getName());
						break;
					}
				}
			} catch (PhonScriptException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
	}
	
	/**
	 * After modifying analysis settings, there may be a change to the report title.
	 * Update the node name to match.
	 *
	 */
	private void updateNodeName(MacroNode documentNode) {
		final OpGraph graph = documentNode.getGraph();
		final WizardExtension wizardExtension = graph.getExtension(WizardExtension.class);
		OpNode parametersNode = null;
		for(OpNode node:wizardExtension) {
			if(node.getName().equals("Parameters") && node instanceof PhonScriptNode
					&& graph.getNodePath(node.getId()).size() == 1) {
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
							documentNode.setName(sp.getValue("reportTitle").toString());
							((NodeTableModel)nodeTable.getModel()).fireTableRowsUpdated(macroNodes.indexOf(documentNode), macroNodes.indexOf(documentNode));
						}
					}
				}
			} catch (PhonScriptException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
	}

	private void updateNodeLocations() {
		for(int i = 0; i < macroNodes.size(); i++) {
			final OpNode node = macroNodes.get(i);

			final NodeMetadata nodeMeta = node.getExtension(NodeMetadata.class);

			final int newX = X_START;
			final int newY = Y_START + (i * Y_SEP);
			final int deltaX = newX - nodeMeta.getX();
			final int deltaY = newY - nodeMeta.getY();

			final MoveNodesEdit moveEdit = new MoveNodesEdit(Collections.singleton(node),
					deltaX, deltaY);
			getModel().getDocument().getUndoSupport().postEdit(moveEdit);
		}
	}

	private TreeModel createTreeModel() {
		final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Add-able Items", true);

		final DefaultMutableTreeNode doucmentRoot =
				new DefaultMutableTreeNode("All " + getModel().getNoun().getObj2(), true);
		setupDocumentLibraryTree(doucmentRoot);
		root.add(doucmentRoot);

		if(isIncludeQueries()) {
			final DefaultMutableTreeNode queryRoot =
					new DefaultMutableTreeNode("Queries", true);
			setupQueryLibraryTree(queryRoot);
			root.add(queryRoot);
		}

		return new DefaultTreeModel(root);
	}

	private void setupQueryLibraryTree(DefaultMutableTreeNode root) {
		final QueryScriptLibrary scriptLibrary = new QueryScriptLibrary();
		final ResourceLoader<QueryScript> stockScriptLoader = scriptLibrary.stockScriptFiles();
		final DefaultMutableTreeNode stockRootNode = new DefaultMutableTreeNode("Stock Queries", true);
		for(QueryScript stockScript:stockScriptLoader) {
			final DefaultMutableTreeNode queryScriptNode = new DefaultMutableTreeNode(stockScript, false);
			stockRootNode.add(queryScriptNode);
		}
		root.add(stockRootNode);

		final ResourceLoader<QueryScript> userScriptLoader = scriptLibrary.userScriptFiles();
		if(userScriptLoader.iterator().hasNext()) {
			final DefaultMutableTreeNode userScriptRoot = new DefaultMutableTreeNode("User Queries", true);
			TernaryTree<DefaultMutableTreeNode> queryScripts = new TernaryTree<>();
			for(QueryScript userScript:userScriptLoader) {
				final DefaultMutableTreeNode userScriptNode = new DefaultMutableTreeNode(userScript, false);
				final QueryName qn = userScript.getExtension(QueryName.class);
				queryScripts.put(qn.getName().toLowerCase(), userScriptNode);
			}
			for(var userScriptName:queryScripts.keySet()) {
				userScriptRoot.add(queryScripts.get(userScriptName));
			}
			root.add(userScriptRoot);
		}

		if(getProject() != null) {
			final ResourceLoader<QueryScript> projectScriptLoader = scriptLibrary.projectScriptFiles(getProject());
			if(projectScriptLoader.iterator().hasNext()) {
				final DefaultMutableTreeNode projectScriptRoot = new DefaultMutableTreeNode("Project Queries");
				TernaryTree<DefaultMutableTreeNode> queryScripts = new TernaryTree<>();
				for(QueryScript projectScript:projectScriptLoader) {
					final DefaultMutableTreeNode projectScriptNode = new DefaultMutableTreeNode(projectScript, false);
					final QueryName qn = projectScript.getExtension(QueryName.class);
					queryScripts.put(qn.getName().toLowerCase(), projectScriptNode);
				}
				for(var projectScriptName:queryScripts.keySet()) {
					projectScriptRoot.add(queryScripts.get(projectScriptName));
				}
				root.add(projectScriptRoot);
			}
		}
	}

	private void setupDocumentLibraryTree(DefaultMutableTreeNode root) {
		final ResourceLoader<URL> stockLoader = library.getStockGraphs();
		final Iterator<URL> stockItr = stockLoader.iterator();
		if(stockItr.hasNext()) {
			final DefaultMutableTreeNode stockNode =
					new DefaultMutableTreeNode("Stock " + getModel().getNoun().getObj2(), true);
			while(stockItr.hasNext()) {
				final URL documentURL = stockItr.next();

				try {
					final String fullPath = URLDecoder.decode(documentURL.getPath(), "UTF-8");
					String relativePath =
							fullPath.substring(fullPath.indexOf(library.getFolderName() + "/")+library.getFolderName().length()+1);

					DefaultMutableTreeNode parentNode = stockNode;
					int splitIdx = -1;
					String tempPath = relativePath;
					while((splitIdx = tempPath.indexOf('/')) >= 0) {
						final String nodeName = tempPath.substring(0, splitIdx);

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
						tempPath = tempPath.substring(splitIdx+1);
					}

					final DefaultMutableTreeNode treeNode =
							new DefaultMutableTreeNode(new StockItemTuple(library.getFolderName() + "/" + relativePath, documentURL), true);
					parentNode.add(treeNode);
				} catch (UnsupportedEncodingException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
				}
			}
			root.add(stockNode);
		}

		// user library
		final ResourceLoader<URL> userLoader = library.getUserGraphs();
		final Iterator<URL> userIterator = userLoader.iterator();
		if(userIterator.hasNext()) {
			final DefaultMutableTreeNode userNode = new DefaultMutableTreeNode("User " + getModel().getNoun().getObj2(), true);
			List<URL> documentURLS = new ArrayList<>();
			while(userIterator.hasNext()) {
				final URL documentURL = userIterator.next();
				documentURLS.add(documentURL);
			}
			documentURLS.sort( (url1, url2) -> url1.toString().toLowerCase().compareTo(url2.toString().toLowerCase()) );

			for(URL documentURL:documentURLS) {
				try {
					final URI relativeURI = new File(library.getUserFolderPath()).toURI().relativize(documentURL.toURI());

					String relativePath = URLDecoder.decode(relativeURI.getPath(), "UTF-8");

					DefaultMutableTreeNode parentNode = userNode;
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
						relativePath = relativePath.substring(splitIdx+1);
					}

					final DefaultMutableTreeNode treeNode =
							new DefaultMutableTreeNode(documentURL, true);
					parentNode.add(treeNode);
				} catch (UnsupportedEncodingException | URISyntaxException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
				}
				
			}
			root.add(userNode);
		}

		if(getProject() != null) {
			final ResourceLoader<URL> projectLoader = library.getProjectGraphs(getProject());
			final Iterator<URL> projectIterator = projectLoader.iterator();
			if(projectIterator.hasNext()) {
				final DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode("Project " + getModel().getNoun().getObj2(), true);
				List<URL> documentURLS = new ArrayList<>();
				while(projectIterator.hasNext()) {
					final URL documentURL = projectIterator.next();
					documentURLS.add(documentURL);
				}
				documentURLS.sort( (url1, url2) -> url1.toString().toLowerCase().compareTo(url2.toString().toLowerCase()) );
				
				for(URL documentURL:documentURLS) {
					try {
						final URI relativeURI = new File(library.getProjectFolderPath(getProject())).toURI().relativize(documentURL.toURI());

						String relativePath = URLDecoder.decode(relativeURI.getPath(), "UTF-8");

						DefaultMutableTreeNode parentNode = projectNode;
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
							relativePath = relativePath.substring(splitIdx+1);
						}

						final DefaultMutableTreeNode treeNode =
								new DefaultMutableTreeNode(documentURL, true);
						parentNode.add(treeNode);
					} catch (UnsupportedEncodingException | URISyntaxException e) {
						LOGGER.error( e.getLocalizedMessage(), e);
					}
				}

				root.add(projectNode);
			}
		}
	}
	
	public void addDocumentErrorListener(Consumer<DocumentError> listener) {
		if(!documentErrorListeners.contains(listener))
			documentErrorListeners.add(listener);
	}
	
	public void removeDocumentErrorListener(Consumer<DocumentError> listener) {
		documentErrorListeners.remove(listener);
	}
	
	public List<Consumer<DocumentError>> getDocumentErrorListeners() {
		return Collections.unmodifiableList(documentErrorListeners);
	}
	
	public void fireDocumentError(Object document, IOException exception) {
		final DocumentError err = new DocumentError(document, exception);
		getDocumentErrorListeners().stream().forEach( (l) -> l.accept(err) );
	}
	
	
	private class DocumentLabel extends JLabel {
		
		private Object documentObj;
		
		public DocumentLabel(Object document) {
			super();
			
			this.documentObj = document;
			init();
			update();
			
			setTransferHandler(new DocumentLabelTransferHandler(this));
			
			addMouseListener(new MouseInputAdapter() {
				public void mousePressed(MouseEvent evt) {
					TransferHandler th = DocumentLabel.this.getTransferHandler();
			        th.exportAsDrag(DocumentLabel.this, evt, TransferHandler.MOVE);
				}
			});
			
			setSize(new Dimension(IconSize.LARGE.getWidth()+10, IconSize.LARGE.getHeight()+10));
			setMaximumSize(new Dimension(IconSize.LARGE.getWidth()+10, IconSize.LARGE.getHeight()+10));
		}
		
		private void init() {
			setHorizontalTextPosition(SwingConstants.CENTER);
			setVerticalTextPosition(SwingConstants.BOTTOM);
		
			final String type = (OSInfo.isNix() ? "text-xml" : "xml");
			final ImageIcon xmlIcon =
					IconManager.getInstance().getSystemIconForFileType(type, "mimetypes/text-xml", IconSize.LARGE);
			setIcon(xmlIcon);
		}
		
		public Object getDocumentObject() {
			return this.documentObj;
		}
		
		private void update() {
			if(getDocumentObject() instanceof StockItemTuple) {
				StockItemTuple tuple = (StockItemTuple)getDocumentObject();
				final URL analysisURL = tuple.getObj2();
				try {
					final String analysisFile = URLDecoder.decode(analysisURL.toString(), "UTF-8");
					final String analysisName = FilenameUtils.getBaseName(analysisFile);
					setText(analysisName);
				} catch (UnsupportedEncodingException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
				}
			} else if(getDocumentObject() instanceof URL) {
				final URL analysisURL = (URL)getDocumentObject();
				try {
					final String analysisFile = URLDecoder.decode(analysisURL.toString(), "UTF-8");
					final String analysisName = FilenameUtils.getBaseName(analysisFile);
					setText(analysisName);
				} catch (UnsupportedEncodingException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
				}
			} else if(getDocumentObject() instanceof QueryScript) {
				final QueryScript queryScript = (QueryScript)getDocumentObject();
				final QueryName queryName = queryScript.getExtension(QueryName.class);
				if(queryName != null) {
					if(queryName.getLocation() != null) {
						try {
							String decodedPath = URLDecoder.decode(queryName.getLocation().getPath(), "UTF-8");
							File file = new File(decodedPath);
							String filename = 
									(file.getName().indexOf('.') > 0 
											? file.getName().substring(0, file.getName().lastIndexOf('.'))
											: file.getName() );
							setText(filename);
						} catch (UnsupportedEncodingException e) {
							LogUtil.warning(e);
						}
					} else {
						setText(queryName.getName());
					}
				}
			}
		}
		
	}
	
	private class DocumentLabelTransferHandler extends TransferHandler {
		
		private final DocumentLabel documentLabel;
		
		public DocumentLabelTransferHandler(DocumentLabel documentLabel) {
			super();
			
			this.documentLabel = documentLabel;
		}
		
		@Override
		public int getSourceActions(JComponent c) {
			return MOVE;
		}
	
		@Override
		public boolean canImport(TransferSupport support) {
			return false;
		}
	
		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			return false;
		}
	
		@Override
		protected Transferable createTransferable(JComponent c) {
			return new DocumentLabelTransferrable(documentLabel);
		}
		
	}
	
	private final DataFlavor documentLabelFlavor = new DataFlavor(DocumentLabel.class, "DocumentLabel");
	
	private class DocumentLabelTransferrable implements Transferable {
		
		public final DocumentLabel label;
		
		public DocumentLabelTransferrable(DocumentLabel label) {
			this.label = label;
		}
	
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] {documentLabelFlavor};
		}
	
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return (flavor.equals(documentLabelFlavor));
		}
	
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			return this.label;
		}
		
	}
	
	public class DocumentError extends Tuple<Object, IOException> {

		public DocumentError() {
			super();
		}

		public DocumentError(Object document, IOException obj2) {
			super(document, obj2);
		}
		
		public Object getDocument() {
			return super.getObj1();
		}
		
		public IOException getError() {
			return super.getObj2();
		}
		
	}
	
	public class StockItemTuple extends Tuple<String, URL> {

		public StockItemTuple() {
			super();
		}

		public StockItemTuple(String obj1, URL obj2) {
			super(obj1, obj2);
		}
		
	}
	
	private class AddDocumentsWorker extends SwingWorker<List<MacroNode>, MacroNode> {

		// either URL, OpGraph or QueryScript objects
		private List<Object> documents;
		
		private int idx;
		
		public AddDocumentsWorker(List<Object> documents) {
			this(documents, -1);
		}
		
		public AddDocumentsWorker(List<Object> documents, int idx) {
			this.documents = documents;
			this.idx = idx;
		}
		
		@Override
		protected void done() {
			busyLabel.setBusy(false);
			statusLabel.setText("");
			
			// select last document added to list
			int rowIdx = (idx < 0 ? nodeTable.getModel().getRowCount()-1 : idx-1);
			nodeTable.setRowSelectionInterval(rowIdx, rowIdx);
		}
		
		@Override
		protected void process(List<MacroNode> chunks) {
			for(MacroNode node:chunks) {
				if(node.getExtension(QueryScript.class) != null) {
					setupQueryNameListener(node);
					// remove temporary extension
					node.putExtension(QueryScript.class, null);
				}
				
				addNode(node, (idx >= 0 ? idx++ : -1));
			}
		}

		@Override
		protected List<MacroNode> doInBackground() {
			SwingUtilities.invokeLater(() -> busyLabel.setBusy(true));
			List<MacroNode> retVal = new ArrayList<>();
			for(Object document:documents) {
				if(document instanceof StockItemTuple) {
					StockItemTuple tuple = (StockItemTuple)document;
					final URL documentURL = tuple.getObj2();
					try(InputStream is = documentURL.openStream()) {
						final String documentFile = URLDecoder.decode(documentURL.toString(), "UTF-8");
						final String documentName = FilenameUtils.getBaseName(documentFile);

						final URI uri = new URI("classpath", tuple.getObj1(), null);
						final MacroNodeData nodeData = new MacroNodeData(documentURL, uri, documentName, "", "", nodeInstantiator, false);

						final MacroNode analysisNode = nodeInstantiator.newInstance(nodeData, getGraph());
						
						final SimpleEditorExtension editorExt = analysisNode.getGraph().getExtension(SimpleEditorExtension.class);
						if(editorExt != null) {
							retVal.addAll(editorExt.getMacroNodes());
							
							super.publish(editorExt.getMacroNodes().toArray(new MacroNode[0]));
						} else {
							if(analysisNode.getName().length() == 0)
								analysisNode.setName(documentName);
							final NodeMetadata nodeMeta = new NodeMetadata(X_START, Y_START + macroNodes.size() * Y_SEP);
							analysisNode.putExtension(NodeMetadata.class, nodeMeta);
							
							retVal.add(analysisNode);
							super.publish(analysisNode);
						}
					} catch (IOException e) {
						fireDocumentError(documentURL, e);
						LOGGER.error( e.getLocalizedMessage(), e);
					} catch (URISyntaxException | InstantiationException e) {
						fireDocumentError(documentURL, new IOException(e));
						LOGGER.error( e.getLocalizedMessage(), e);
					}
				} else if(document instanceof URL) {
					final URL documentURL = (URL)document;
					try(InputStream is = documentURL.openStream()) {
						final String documentFile = URLDecoder.decode(documentURL.toString(), "UTF-8");
						final String documentName = FilenameUtils.getBaseName(documentFile);

						final URI uri = documentURL.toURI();
						final MacroNodeData nodeData = new MacroNodeData(documentURL, uri, documentName, "", "", nodeInstantiator, false);

						final MacroNode analysisNode = nodeInstantiator.newInstance(nodeData, getGraph());
						
						final SimpleEditorExtension editorExt = analysisNode.getGraph().getExtension(SimpleEditorExtension.class);
						if(editorExt != null) {
							retVal.addAll(editorExt.getMacroNodes());
							
							super.publish(editorExt.getMacroNodes().toArray(new MacroNode[0]));
						} else {
							if(analysisNode.getName().length() == 0)
								analysisNode.setName(documentName);
							final NodeMetadata nodeMeta = new NodeMetadata(X_START, Y_START + macroNodes.size() * Y_SEP);
							analysisNode.putExtension(NodeMetadata.class, nodeMeta);
							
							retVal.add(analysisNode);
							super.publish(analysisNode);
						}
					} catch (IOException e) {
						fireDocumentError(documentURL, e);
						LOGGER.error( e.getLocalizedMessage(), e);
					} catch (URISyntaxException | InstantiationException e) {
						fireDocumentError(documentURL, new IOException(e));
						LOGGER.error( e.getLocalizedMessage(), e);
					}
				} else if(document instanceof OpGraph) {
					try {
						final MacroNode node = nodeInstantiator.newInstance((OpGraph)document);
						retVal.add(node);
						super.publish(node);
					} catch (InstantiationException e) {
						fireDocumentError(document, new IOException(e));
						LOGGER.error( e.getLocalizedMessage(), e);
					}
				} else if(document instanceof QueryScript) {
					final QueryScript queryScript = (QueryScript)document;
					final MacroNode node = queryNodeInstantiator.apply(queryScript, new OpGraph());
					node.putExtension(QueryScript.class, queryScript);
					
					retVal.add(node);
					super.publish(node);
				} else if(document instanceof MacroNode) {
					retVal.add((MacroNode)document);
					super.publish((MacroNode)document);
				} else {
					Toolkit.getDefaultToolkit().beep();
					fireDocumentError(document, new IOException(new IllegalArgumentException()));
					LogUtil.warning(document.getClass().getName() + " is not a valid document type");
				}
			}
			return retVal;
		}
		
	}
	
	private class ListCellRenderer extends DefaultListCellRenderer {
	
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

	private final DataFlavor nodeTreeDataFlavor = new DataFlavor(DefaultMutableTreeNode[].class, "DefaultMutableTreeNode Array");

	private class NodeTreeTransferHandler extends TransferHandler {
	
		@Override
		public int getSourceActions(JComponent c) {
			return MOVE;
		}
	
		@Override
		public boolean canImport(TransferSupport support) {
			return false;
		}
	
		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			return false;
		}
	
		@Override
		protected Transferable createTransferable(JComponent c) {
			final TreePath[] selectedPaths = documentTree.getSelectionPaths();
			List<DefaultMutableTreeNode> selectedNodes = new ArrayList<>();
			for(TreePath selectedPath:selectedPaths) {
				selectedNodes.add((DefaultMutableTreeNode)selectedPath.getLastPathComponent());
			}
			
			return new NodeTreeTransferrable(selectedNodes.toArray(new DefaultMutableTreeNode[0]));
		}
		
	}

	private class NodeTreeTransferrable implements Transferable {
		
		public final DefaultMutableTreeNode[] treeNodes;
		
		public NodeTreeTransferrable(DefaultMutableTreeNode[] nodes) {
			this.treeNodes = nodes;
		}
	
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[] {nodeTreeDataFlavor};
		}
	
		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return (flavor.equals(nodeTreeDataFlavor));
		}
	
		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			return this.treeNodes;
		}
		
	}

	private class NodeTableTransferHandler extends TransferHandler {
	
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
				// files dropped into list
				try {
					@SuppressWarnings("unchecked")
					final List<File> fileList =
							(List<File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					addDocuments(fileList, dropLocation.getRow());
					retVal = true;
				} catch (IOException | UnsupportedFlavorException e) {
					LOGGER.error( e.getLocalizedMessage(), e);
				}
			} else if(support.isDataFlavorSupported(nodeTreeDataFlavor)) {
				addSelectedDocuments(documentTree, dropLocation.getRow());
				retVal = true;
			} else {
				// Re-ordering nodes
				try {
	                // convert data to string
	                String s = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
	
	                int idx = dropLocation.getRow();
	                int origIdx = Integer.parseInt(s);
	                MacroNode srcNode = macroNodes.remove(origIdx);
	
	                if(idx < 0) {
	                	idx = macroNodes.size();
	                } else if(idx > origIdx) {
	                	--idx;
	                }
	                macroNodes.add(idx, srcNode);
	
	                ((NodeTableModel)nodeTable.getModel()).fireTableDataChanged();
	                getModel().getDocument().getUndoSupport().beginUpdate();
	                updateNodeLocations();
	                getModel().getDocument().getUndoSupport().endUpdate();
	
	                retVal = true;
	            } catch (IOException | UnsupportedFlavorException e) {
	            	LOGGER.error( e.getLocalizedMessage(), e);
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
					|| support.isDataFlavorSupported(nodeTreeDataFlavor)
					|| support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
		}
	
		@Override
		protected Transferable createTransferable(JComponent c) {
			int selectedRow = nodeTable.getSelectedRow();
			return new StringSelection(""+selectedRow);
		}
	
	}

	private class NodeTableModel extends AbstractTableModel {
	
		@Override
		public int getRowCount() {
			return macroNodes.size();
		}
	
		@Override
		public int getColumnCount() {
			return 1;
		}
	
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			final OpNode node = macroNodes.get(rowIndex);
			final WizardExtension ext = getModel().getDocument().getRootGraph().getExtension(WizardExtension.class);
			switch(columnIndex) {
			case 0:
				return node.getName();
	
			case 1:
				return (ext != null && ext.isNodeOptional(node));
	
			default:
				return null;
			}
		}
	
		@Override
		public String getColumnName(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return WordUtils.capitalize(getModel().getNoun().getObj1()) + " Name";
	
			case 1:
				return "Optional";
	
			default:
				return super.getColumnName(columnIndex);
			}
		}
	
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch(columnIndex) {
			case 0:
				return String.class;
	
			case 1:
				return Boolean.class;
	
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
			final OpNode node = macroNodes.get(rowIndex);
			if(columnIndex == 0) {
				if(aValue.toString().trim().length() == 0) return;
	
				node.setName(aValue.toString());
				
				node.putExtension(QueryName.class, new QueryName(node.getName()));
	
				updateReportTitle((MacroNode)node);
			} else if (columnIndex == 1) {
				if((Boolean)aValue) {
					getModel().getDocument().getRootGraph().getExtension(WizardExtension.class).addOptionalNode(node);
					getModel().getDocument().getRootGraph().getExtension(WizardExtension.class).setOptionalNodeDefault(node, true);
				} else {
					getModel().getDocument().getRootGraph().getExtension(WizardExtension.class).removeOptionalNode(node);
				}
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
	
			final String type = (OSInfo.isNix() ? "text-xml" : "xml");
			final ImageIcon xmlIcon =
					IconManager.getInstance().getSystemIconForFileType(type, "mimetypes/text-xml", IconSize.SMALL);
			super.setLeafIcon(xmlIcon);
		}
	
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			JLabel retVal = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
	
			if(value instanceof DefaultMutableTreeNode) {
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
				if(node.getUserObject() instanceof StockItemTuple) {
					StockItemTuple tuple = (StockItemTuple)node.getUserObject();
					final URL analysisURL = tuple.getObj2();
					try {
						final String analysisFile = URLDecoder.decode(analysisURL.toString(), "UTF-8");
						final String analysisName = FilenameUtils.getBaseName(analysisFile);
						retVal.setText(analysisName);
					} catch (UnsupportedEncodingException e) {
						LOGGER.error( e.getLocalizedMessage(), e);
					}
				} else if(node.getUserObject() instanceof URL) {
					final URL analysisURL = (URL)node.getUserObject();
					try {
						final String analysisFile = URLDecoder.decode(analysisURL.toString(), "UTF-8");
						final String analysisName = FilenameUtils.getBaseName(analysisFile);
						retVal.setText(analysisName);
					} catch (UnsupportedEncodingException e) {
						LOGGER.error( e.getLocalizedMessage(), e);
					}
				} else if(node.getUserObject() instanceof QueryScript) {
					final QueryScript queryScript = (QueryScript)node.getUserObject();
					final QueryName queryName = queryScript.getExtension(QueryName.class);
					if(queryName != null) {
						if(queryName.getLocation() != null) {
							try {
								String decodedPath = URLDecoder.decode(queryName.getLocation().getPath(), "UTF-8");
								File file = new File(decodedPath);
								String filename = 
										(file.getName().indexOf('.') > 0 
												? file.getName().substring(0, file.getName().lastIndexOf('.'))
												: file.getName() );
								retVal.setText(filename);
							} catch (UnsupportedEncodingException e) {
								LogUtil.warning(e);
							}
						} else {
							retVal.setText(queryName.getName());
						}
					}
				}
			}
	
			return retVal;
		}
	
	}

	private class DocumentSettingsPanel extends JPanel {
	
		private final MacroNode analysisNode;
	
		private JTabbedPane tabPanel;
	
		public DocumentSettingsPanel(MacroNode analysisNode) {
			super();
	
			this.analysisNode = analysisNode;
	
			init();
			update();
		}
	
		private void init() {
			setLayout(new BorderLayout());
	
			tabPanel = new JTabbedPane();
			add(tabPanel, BorderLayout.CENTER);
		}
	
		private void update() {
			final OpGraph analysisGraph = analysisNode.getGraph();
			final WizardExtension analysisExt = analysisGraph.getExtension(WizardExtension.class);
	
			tabPanel.removeAll();
			final List<OpNode> settingsNodes = new ArrayList<>();
			for(int i = 0; i < analysisExt.size(); i++) {
				final OpNode node = analysisExt.getNode(i);
				final NodeSettings nodeSettings = node.getExtension(NodeSettings.class);
				if(nodeSettings != null) {
					settingsNodes.add(node);
	
					final Component nodeSettingsPanel = nodeSettings.getComponent(getModel().getDocument());
					if(nodeSettingsPanel != null) {
						if(nodeSettingsPanel instanceof QueryNode.QueryNodeSettingsPanel) {
							if(node.getName().equals("Parameters")) {
								// add listener to 'reportTitle' param if it exists
								final PhonScript script = ((QueryNode.QueryNodeSettingsPanel)nodeSettingsPanel).getScriptPanel().getScript();
								try {
									final ScriptParameters params = script.getContext().getScriptParameters(script.getContext().getEvaluatedScope());
									
									final Optional<ScriptParam> reportTitleParam = 
											params.stream().filter( (sp) -> sp.getParamId().equals("reportTitle") ).findFirst();
									if(reportTitleParam.isPresent()) {
										reportTitleParam.get().addPropertyChangeListener("reportTitle", (e) -> {
											updateNodeName(analysisNode);
										});
									}
								} catch (PhonScriptException e) {
									LogUtil.severe(e);
								}
								
							}
							tabPanel.add(node.getName(), nodeSettingsPanel);
						} else {
							final JScrollPane scroller = new JScrollPane(nodeSettingsPanel);
							scroller.getVerticalScrollBar().setUnitIncrement(10);
							tabPanel.add(node.getName(), scroller);
						}
					}
				}
			}
		}
	
	}

	@Override
	public Set<Class<?>> getExtensions() {
		return extSupport.getExtensions();
	}

	@Override
	public <T> T getExtension(Class<T> cap) {
		return extSupport.getExtension(cap);
	}

	@Override
	public <T> T putExtension(Class<T> cap, T impl) {
		return extSupport.putExtension(cap, impl);
	}

	@Override
	public <T> T removeExtension(Class<T> cap) {
		return extSupport.removeExtension(cap);
	}
}
