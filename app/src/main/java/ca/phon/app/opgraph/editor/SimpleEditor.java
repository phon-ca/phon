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
package ca.phon.app.opgraph.editor;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.Collectors;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.WordUtils;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.JXStatusBar.Constraint.ResizeBehavior;

import com.sun.glass.events.KeyEvent;

import ca.gedge.opgraph.*;
import ca.gedge.opgraph.app.edits.graph.*;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.extensions.NodeMetadata;
import ca.gedge.opgraph.library.instantiators.Instantiator;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.hedlund.desktopicons.*;
import ca.phon.app.modules.EntryPointArgs;
import ca.phon.app.opgraph.nodes.*;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.opgraph.wizard.edits.NodeWizardOptionalsEdit;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.plugin.PluginEntryPointRunner;
import ca.phon.project.Project;
import ca.phon.query.script.*;
import ca.phon.script.*;
import ca.phon.script.params.*;
import ca.phon.ui.CommonModuleFrame;
import ca.phon.ui.action.*;
import ca.phon.ui.decorations.DialogHeader;
import ca.phon.ui.layout.ButtonBarBuilder;
import ca.phon.ui.menu.MenuBuilder;
import ca.phon.ui.nativedialogs.*;
import ca.phon.ui.nativedialogs.FileFilter;
import ca.phon.util.OSInfo;
import ca.phon.util.icons.*;
import ca.phon.util.resources.ResourceLoader;
import ca.phon.worker.PhonWorker;

/**
 * UI for creating new {@link OpgraphEditor} documents using existing
 * documents.  This UI is used for both {@link OpGraph} analysis and
 * report document types.
 */
public class SimpleEditor extends CommonModuleFrame {

	private static final long serialVersionUID = -7170934782795308487L;

	private final static Logger LOGGER = Logger.getLogger(SimpleEditor.class.getName());

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

	private Function<QueryScript, MacroNode> queryNodeInstantiator;

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

	private JXTable nodeTable;
	private List<MacroNode> macroNodes;

	private JXStatusBar statusBar;
	private JXBusyLabel busyLabel;
	private JLabel statusLabel;

	private JMenuBar menuBar;

	private final OpgraphEditorModel model;

	private boolean includeQueries = false;

	private final BiFunction<OpGraph, Project, Runnable> runFactory;

	/**
	 * Constructor
	 *
	 * @param project if <code>null</code> project graphs will not be displayed
	 * @param library library display in add item dialog
	 * @param modelInstantiator the editor model instantiator
	 * @param nodeInstantiator instantiator for nodes created by adding documents from the library
	 * @param queryNodeInstantiator instantiator for nodes created by adding queries to the doucment
	 * @param runFactory factory for runnables used to execute graphs
	 */
	public SimpleEditor(Project project, OpGraphLibrary library,
			EditorModelInstantiator modelInstantiator, Instantiator<MacroNode> nodeInstantiator,
			Function<QueryScript, MacroNode> queryNodeInstantiator,
			BiFunction<OpGraph, Project, Runnable> runFactory) {
		super();

		this.library = library;
		this.nodeInstantiator = nodeInstantiator;
		this.queryNodeInstantiator = queryNodeInstantiator;
		this.runFactory = runFactory;

		model = modelInstantiator.createModel(new OpGraph());
		model.getDocument().getUndoSupport().addUndoableEditListener( (e) -> updateTitle() );

		putExtension(Project.class, project);

		init();
		updateTitle();
	}

	private void init() {
		// create components for popup window selection
		final ImageIcon saveIcn =
				IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		final PhonUIAction saveAct = new PhonUIAction(this, "saveData");
		saveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save");
		saveAct.putValue(PhonUIAction.SMALL_ICON, saveIcn);
		saveButton = new JButton(saveAct);

		final ImageIcon addIcn =
				IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final PhonUIAction addAct = new PhonUIAction(this, "onAdd");
		addAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add " + getModel().getNoun().getObj1());
		addAct.putValue(PhonUIAction.SMALL_ICON, addIcn);
		addButton = new JButton(addAct);

		final ImageIcon removeIcn =
				IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction removeAct = new PhonUIAction(this, "onRemove");
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected " + getModel().getNoun().getObj1());
		removeAct.putValue(PhonUIAction.SMALL_ICON, removeIcn);
		final KeyStroke removeKs = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		removeButton = new JButton(removeAct);

		final ImageIcon settingsIcn =
				IconManager.getInstance().getIcon("actions/settings-black", IconSize.SMALL);
		final PhonUIAction settingsAct = new PhonUIAction(this, "onShowSettings");
		settingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show settings for selected " + getModel().getNoun().getObj1());
		settingsAct.putValue(PhonUIAction.SMALL_ICON, settingsIcn);
		settingsButton = new JButton(settingsAct);

		final ImageIcon renameIcn =
				IconManager.getInstance().getIcon("actions/edit-rename", IconSize.SMALL);
		final PhonUIAction renameAct = new PhonUIAction(this, "onRename");
		renameAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Rename selected " + getModel().getNoun().getObj1());
		renameAct.putValue(PhonUIAction.SMALL_ICON, renameIcn);
		renameButton = new JButton(renameAct);

		final ImageIcon upIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-up", IconSize.SMALL);
		final PhonUIAction upAct = new PhonUIAction(this, "onMoveUp");
		upAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected " + getModel().getNoun().getObj1() + " up");
		upAct.putValue(PhonUIAction.SMALL_ICON, upIcn);
		final KeyStroke upKs = KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		moveUpButton = new JButton(upAct);

		final ImageIcon downIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-down", IconSize.SMALL);
		final PhonUIAction downAct = new PhonUIAction(this, "onMoveDown");
		downAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected " + getModel().getNoun().getObj1() + " down");
		downAct.putValue(PhonUIAction.SMALL_ICON, downIcn);
		final KeyStroke downKs = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		moveDownButton = new JButton(downAct);

		final ImageIcon runIcn =
				IconManager.getInstance().getIcon("actions/media-playback-start-7", IconSize.SMALL);
		final PhonUIAction runAct = new PhonUIAction(this, "onRun");
		runAct.putValue(PhonUIAction.NAME, "Run " + getModel().getNoun().getObj1());
		runAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Run " + getModel().getNoun().getObj1());
		runAct.putValue(PhonUIAction.SMALL_ICON, runIcn);
		runButton = new JButton(runAct);

		final ImageIcon graphIcn =
				IconManager.getInstance().getIcon("opgraph/graph", IconSize.SMALL);
		final PhonUIAction openInComposerAct = new PhonUIAction(this, "onOpenInComposer");
		openInComposerAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Open in Composer (advanced)");
		openInComposerAct.putValue(PhonUIAction.SMALL_ICON, graphIcn);
		openInComposerButton = new JButton(openInComposerAct);

		macroNodes = Collections.synchronizedList(new ArrayList<>());
		nodeTable = new JXTable(new NodeTableModel());
		nodeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		nodeTable.setSortable(false);
		nodeTable.setDragEnabled(true);
		nodeTable.setTransferHandler(new NodeTableTransferHandler());
		nodeTable.setDropMode(DropMode.INSERT);
		nodeTable.setVisibleRowCount(10);
		nodeTable.getColumn(1).setMaxWidth(100);

		final ActionMap am = nodeTable.getActionMap();
		final InputMap inputMap = nodeTable.getInputMap(JComponent.WHEN_FOCUSED);

		inputMap.put(upKs, "moveUp");
		am.put("moveUp", upAct);

		inputMap.put(downKs, "moveDown");
		am.put("moveDown", downAct);

		inputMap.put(removeKs, "delete");
		am.put("delete", removeAct);

		// setup settings column
		final JScrollPane nodeScroller = new JScrollPane(nodeTable);

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

		toolbar.add(openInComposerButton);
		toolbar.addSeparator();
		toolbar.add(runButton);

		busyLabel = new JXBusyLabel(new Dimension(16, 16));
		statusLabel = new JLabel();

		statusBar = new JXStatusBar();
		statusBar.add(busyLabel, new JXStatusBar.Constraint(16));
		statusBar.add(statusLabel, new JXStatusBar.Constraint(ResizeBehavior.FILL));

		setLayout(new BorderLayout());
		add(nodeScroller, BorderLayout.CENTER);
		add(toolbar, BorderLayout.NORTH);
		add(statusBar, BorderLayout.SOUTH);
	}

	@Override
	public void setJMenuBar(JMenuBar menuBar) {
		super.setJMenuBar(menuBar);
		this.menuBar = menuBar;
		setupMenu();
	}

	public boolean isIncludeQueries() {
		return this.includeQueries;
	}

	public void setIncludeQueries(boolean includeQueries) {
		this.includeQueries = includeQueries;
	}

	protected void setupMenu() {
		final ImageIcon saveIcn =
				IconManager.getInstance().getIcon("actions/document-save", IconSize.SMALL);
		final PhonUIAction saveAct = new PhonUIAction(this, "saveData");
		saveAct.putValue(PhonUIAction.NAME, "Save");
		saveAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Save " + getModel().getNoun().getObj1());
		saveAct.putValue(PhonUIAction.SMALL_ICON, saveIcn);
		final KeyStroke saveKs = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		saveAct.putValue(PhonUIAction.ACCELERATOR_KEY, saveKs);
		final JMenuItem saveItem = new JMenuItem(saveAct);

		final ImageIcon addIcn =
				IconManager.getInstance().getIcon("actions/list-add", IconSize.SMALL);
		final PhonUIAction addAct = new PhonUIAction(this, "onAdd");
		addAct.putValue(PhonUIAction.NAME, "Add " + getModel().getNoun().getObj1() + "...");
		addAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add " + getModel().getNoun().getObj1() + "...");
		addAct.putValue(PhonUIAction.SMALL_ICON, addIcn);
		final JMenuItem addItem = new JMenuItem(addAct);

		final ImageIcon removeIcn =
				IconManager.getInstance().getIcon("actions/list-remove", IconSize.SMALL);
		final PhonUIAction removeAct = new PhonUIAction(this, "onRemove");
		removeAct.putValue(PhonUIAction.NAME, "Remove " + getModel().getNoun().getObj1());
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected " + getModel().getNoun().getObj1());
		removeAct.putValue(PhonUIAction.SMALL_ICON, removeIcn);
		final KeyStroke removeKs = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
		removeAct.putValue(PhonUIAction.ACCELERATOR_KEY, removeKs);
		final JMenuItem removeItem = new JMenuItem(removeAct);

		final ImageIcon settingsIcn =
				IconManager.getInstance().getIcon("actions/settings-black", IconSize.SMALL);
		final PhonUIAction settingsAct = new PhonUIAction(this, "onShowSettings");
		settingsAct.putValue(PhonUIAction.NAME, "Settings...");
		settingsAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Show settings for selected " + getModel().getNoun().getObj1());
		settingsAct.putValue(PhonUIAction.SMALL_ICON, settingsIcn);
		final KeyStroke settingsKs = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		settingsAct.putValue(PhonUIAction.ACCELERATOR_KEY, settingsKs);
		final JMenuItem settingsItem = new JMenuItem(settingsAct);

		final ImageIcon renameIcn =
				IconManager.getInstance().getIcon("actions/edit-rename", IconSize.SMALL);
		final PhonUIAction renameAct = new PhonUIAction(this, "onRename");
		renameAct.putValue(PhonUIAction.NAME, "Rename");
		renameAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Rename selected " + getModel().getNoun().getObj1());
		renameAct.putValue(PhonUIAction.SMALL_ICON, renameIcn);
		final JMenuItem renameItem = new JMenuItem(renameAct);

		final ImageIcon upIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-up", IconSize.SMALL);
		final PhonUIAction upAct = new PhonUIAction(this, "onMoveUp");
		upAct.putValue(PhonUIAction.NAME, "Move up");
		upAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected " + getModel().getNoun().getObj1() + " up");
		upAct.putValue(PhonUIAction.SMALL_ICON, upIcn);
		final KeyStroke upKs = KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		upAct.putValue(PhonUIAction.ACCELERATOR_KEY, upKs);
		final JMenuItem upItem = new JMenuItem(upAct);

		final ImageIcon downIcn =
				IconManager.getInstance().getIcon("actions/draw-arrow-down", IconSize.SMALL);
		final PhonUIAction downAct = new PhonUIAction(this, "onMoveDown");
		downAct.putValue(PhonUIAction.NAME, "Move down");
		downAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected " + getModel().getNoun().getObj1() + " down");
		downAct.putValue(PhonUIAction.SMALL_ICON, downIcn);
		final KeyStroke downKs = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		downAct.putValue(PhonUIAction.ACCELERATOR_KEY, downKs);
		final JMenuItem downItem = new JMenuItem(downAct);

		final ImageIcon runIcn =
				IconManager.getInstance().getIcon("actions/media-playback-start-7", IconSize.SMALL);
		final PhonUIAction runAct = new PhonUIAction(this, "onRun");
		runAct.putValue(PhonUIAction.NAME, "Run " + getModel().getNoun().getObj1() + "...");
		runAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Run " + getModel().getNoun().getObj1());
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

		builder.addMenu(".@Edit", "Table");
		builder.addItem("Table", addItem);
		builder.addItem("Table", removeItem);
		builder.addSeparator("Table", "settings");
		builder.addItem("Table", settingsItem);
		builder.addItem("Table", renameItem);
		builder.addSeparator("Table", "move");
		builder.addItem("Table", upItem);
		builder.addItem("Table", downItem);
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

		// update node title for wizard
		final WizardExtension ext = getModel().getDocument().getRootGraph().getExtension(WizardExtension.class);
		if(ext != null) {
			final String name = FilenameUtils.getBaseName(source.getAbsolutePath());
			ext.setWizardTitle(name);
		}
	}

	protected void updateTitle() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Composer (" + getModel().getNoun().getObj1() + "-simple)");
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
		final JComponent comp = addButton;

		final JTree tree = new JTree(createTreeModel());
		tree.setRootVisible(false);
		tree.setVisibleRowCount(20);
		tree.setCellRenderer(new TreeNodeRenderer());


		// expand all first-level siblings
		final DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)tree.getModel().getRoot();
		final TreePath rootPath = new TreePath(rootNode);
		for(int i = 0; i < rootNode.getChildCount(); i++) {
			final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)rootNode.getChildAt(i);
			final TreePath childPath = rootPath.pathByAddingChild(childNode);
			tree.expandPath(childPath);
		}

		final JScrollPane scroller = new JScrollPane(tree);
		scroller.setPreferredSize(new Dimension(300, tree.getPreferredScrollableViewportSize().height));

		final Point p = new Point(0, comp.getHeight());
		SwingUtilities.convertPointToScreen(p, comp);

		final JFrame popup = new JFrame("Add " + getModel().getNoun().getObj1());
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

		tree.addMouseListener(new MouseInputAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					final int clickedRow = tree.getRowForLocation(e.getX(), e.getY());
					if(clickedRow >= 0 && clickedRow < tree.getRowCount()) {
						destroyPopup(popup);
						addSelectedDocuments(tree);
					}
				}
			}


		});

		final PhonUIAction cancelAct = new PhonUIAction(this, "destroyPopup", popup);
		cancelAct.putValue(PhonUIAction.NAME, "Cancel");
		final JButton cancelBtn = new JButton(cancelAct);

		final PhonUIAction okAct = new PhonUIAction(this, "addSelectedDocuments", tree);
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

	public Project getProject() {
		return getExtension(Project.class);
	}

	public void addSelectedDocuments(JTree tree) {
		final TreePath[] selectedPaths = tree.getSelectionPaths();
		if(selectedPaths != null && selectedPaths.length > 0) {
			for(TreePath selectedPath:selectedPaths)
				addDocuments((DefaultMutableTreeNode)selectedPath.getLastPathComponent());
			if(selectedPaths.length == 1) 
				SwingUtilities.invokeLater( this::onShowSettings );
		}
	}

	private void addDocuments(List<File> fileList) {
		for(File f:fileList) {
			if(f.isFile()
					&& f.getName().endsWith(".xml")) {
				
				Runnable r = () -> {};
				try {
					OpgraphIO.read(f);
					r = () -> { 
						try {
							addDocument(f);
						} catch (InstantiationException | IOException e) {
							LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
						} 
					};
				} catch (IOException e) {
					try {
						final QueryScript queryScript = new QueryScript(f.toURI().toURL());
						r = () -> { addQuery(queryScript); };
					} catch (IOException e2) {
						final MessageDialogProperties props = new MessageDialogProperties();
						props.setParentWindow(this);
						props.setHeader("Add Analysis");
						props.setTitle("Unable to add analysis");
						props.setMessage("Document is not an analysis or query");
						props.setOptions(MessageDialogProperties.okOptions);
						NativeDialogs.showMessageDialog(props);
					}
				}
				
				PhonWorker.getInstance().invokeLater( r );
			}
		}
		if(fileList.size() == 1) {
			SwingUtilities.invokeLater( this::onShowSettings );
		}
	}

	private void addDocuments(DefaultMutableTreeNode node) {
		if(node.isLeaf()) {
			if(node.getUserObject() instanceof URL) {
				final URL documentURL = (URL)node.getUserObject();
				PhonWorker.getInstance().invokeLater( () -> {
					try {
						addDocument(documentURL);
					} catch (IOException | InstantiationException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				} );
			} else if(isIncludeQueries() && node.getUserObject() instanceof QueryScript) {
				addQuery((QueryScript)node.getUserObject());
			}
		} else {
			for(int i = 0; i < node.getChildCount(); i++) {
				final DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
				addDocuments(childNode);
			}
		}
	}
	
	public void addGraph(OpGraph graph) {
		try {
			final MacroNode node = nodeInstantiator.newInstance(graph);
			
			final AddNodeEdit addNodeEdit = 
					new AddNodeEdit(getGraph(), node, X_START, Y_START + macroNodes.size() * Y_SEP);
			model.getDocument().getUndoSupport().postEdit(addNodeEdit);
			
			final NodeWizardOptionalsEdit optEdit =
					new NodeWizardOptionalsEdit(getGraph(),  getGraph().getExtension(WizardExtension.class), node, true, true);
			model.getDocument().getUndoSupport().postEdit(optEdit);
			
			updateNodeName(node);
			
			macroNodes.add(node);
			((NodeTableModel)nodeTable.getModel()).fireTableRowsInserted(macroNodes.size()-1, macroNodes.size()-1);
			nodeTable.setRowSelectionInterval(macroNodes.size()-1, macroNodes.size()-1);
		} catch (InstantiationException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
	}

	public void addQuery(QueryScript queryScript) {
		final MacroNode node = queryNodeInstantiator.apply(queryScript);

		final AddNodeEdit addNodeEdit =
				new AddNodeEdit(getGraph(), node, X_START, Y_START + macroNodes.size() * Y_SEP);
		model.getDocument().getUndoSupport().postEdit(addNodeEdit);

		final NodeWizardOptionalsEdit optEdit =
				new NodeWizardOptionalsEdit(getGraph(), getGraph().getExtension(WizardExtension.class), node, true, true);
		model.getDocument().getUndoSupport().postEdit(optEdit);

		updateReportTitle(node);

		macroNodes.add(node);
		((NodeTableModel)nodeTable.getModel()).fireTableRowsInserted(macroNodes.size()-1, macroNodes.size()-1);
		nodeTable.setRowSelectionInterval(macroNodes.size()-1, macroNodes.size()-1);
	}

	public void addDocument(File file) throws IOException, InstantiationException {
		addDocument(file.toURI().toURL());
	}

	/*
	 * This method should be executed on a background thread
	 */
	public void addDocument(URL documentURL) throws IOException, InstantiationException {
		// create analysis node
		try(InputStream is = documentURL.openStream()) {
			final String documentFile = URLDecoder.decode(documentURL.toString(), "UTF-8");
			final String documentName = FilenameUtils.getBaseName(documentFile);
			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(true);
				statusLabel.setText("Adding " + documentName + "...");
			});

			final URI uri = new URI("class", MacroNode.class.getName(), documentName);
			final MacroNodeData nodeData = new MacroNodeData(documentURL, uri, documentName, "", "", nodeInstantiator);

			final MacroNode analysisNode = nodeInstantiator.newInstance(nodeData);
			analysisNode.setName(documentName);
			final NodeMetadata nodeMeta = new NodeMetadata(X_START, Y_START + macroNodes.size() * Y_SEP);
			analysisNode.putExtension(NodeMetadata.class, nodeMeta);

			SwingUtilities.invokeLater( () -> {
				final AddNodeEdit addEdit = new AddNodeEdit(getGraph(), analysisNode);
				getModel().getDocument().getUndoSupport().postEdit(addEdit);

				final NodeWizardOptionalsEdit optEdit =
						new NodeWizardOptionalsEdit(getGraph(), getGraph().getExtension(WizardExtension.class), analysisNode, true, true);
				getModel().getDocument().getUndoSupport().postEdit(optEdit);

				macroNodes.add(analysisNode);
				((NodeTableModel)nodeTable.getModel()).fireTableRowsInserted(macroNodes.size()-1, macroNodes.size()-1);
				nodeTable.setRowSelectionInterval(macroNodes.size()-1, macroNodes.size()-1);
			});
		} catch (IOException | InstantiationException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			throw e;
		} catch (URISyntaxException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SwingUtilities.invokeLater( () -> {
				busyLabel.setBusy(false);
				statusLabel.setText("");
			});
		}
	}

	public void onRemove() {
		final int selectedRow = nodeTable.getSelectedRow();
		if(selectedRow >= 0 && selectedRow < macroNodes.size()) {
			final OpNode selectedNode = macroNodes.get(selectedRow);

			final DeleteNodesEdit removeEdit =
					new DeleteNodesEdit(getGraph(), Collections.singleton(selectedNode));
			getModel().getDocument().getUndoSupport().postEdit(removeEdit);

			macroNodes.remove(selectedRow);
			((NodeTableModel)nodeTable.getModel()).fireTableRowsDeleted(selectedRow, selectedRow);

			updateNodeLocations();

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
			nodeTable.editCellAt(selectedRow, 1);
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

			updateNodeLocations();
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

			updateNodeLocations();
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
		addDocuments(fileList);
		if(fileList.size() == 1) {
			SwingUtilities.invokeLater( this::onShowSettings );
		}
	}

	public void showDocumentSettings(MacroNode documentNode) {
		final DocumentSettingsPanel settingsPanel = new DocumentSettingsPanel(documentNode);
		final JDialog settingsDialog = new JDialog(CommonModuleFrame.getCurrentFrame(), "Settings : " + documentNode.getName(), true);

		final DialogHeader header = new DialogHeader("Settings : " + documentNode.getName(), "Edit settings for the " + documentNode.getName() + " " + getModel().getNoun().getObj1() + ".");
		settingsDialog.getContentPane().setLayout(new BorderLayout());
		settingsDialog.getContentPane().add(header, BorderLayout.NORTH);
		settingsDialog.getContentPane().add(settingsPanel, BorderLayout.CENTER);

		final PhonUIAction closeSettingsAct = new PhonUIAction(this, "onCloseSettings", settingsDialog);
		closeSettingsAct.putValue(PhonUIAction.NAME, "Ok");
		final JButton closeSettingsBtn = new JButton(closeSettingsAct);
		closeSettingsBtn.addActionListener( (e) -> updateNodeName(documentNode) );

		settingsDialog.getContentPane().add(ButtonBarBuilder.buildOkBar(closeSettingsBtn), BorderLayout.SOUTH);
		settingsDialog.getRootPane().setDefaultButton(closeSettingsBtn);

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
				updateNodeName(documentNode);
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

	public void onCloseSettings(JDialog dialog) {
		dialog.setVisible(false);
		dialog.dispose();
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
	}

	private void updateNodeLocations() {
		getModel().getDocument().getUndoSupport().beginUpdate();
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
		getModel().getDocument().getUndoSupport().endUpdate();
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
			for(QueryScript userScript:userScriptLoader) {
				final DefaultMutableTreeNode userScriptNode = new DefaultMutableTreeNode(userScript, false);
				userScriptRoot.add(userScriptNode);
			}
			root.add(userScriptRoot);
		}

		if(getProject() != null) {
			final ResourceLoader<QueryScript> projectScriptLoader = scriptLibrary.projectScriptFiles(getProject());
			if(projectScriptLoader.iterator().hasNext()) {
				final DefaultMutableTreeNode projectScriptRoot = new DefaultMutableTreeNode("Project Queries");
				for(QueryScript projectScript:projectScriptLoader) {
					final DefaultMutableTreeNode projectScriptNode = new DefaultMutableTreeNode(projectScript, false);
					projectScriptRoot.add(projectScriptNode);
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
				} catch (UnsupportedEncodingException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
			root.add(stockNode);
		}

		// user library
		final ResourceLoader<URL> userLoader = library.getUserGraphs();
		final Iterator<URL> userIterator = userLoader.iterator();
		if(userIterator.hasNext()) {
			final DefaultMutableTreeNode userNode = new DefaultMutableTreeNode("User " + getModel().getNoun().getObj2(), true);
			while(userIterator.hasNext()) {
				final URL documentURL = userIterator.next();

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
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}

			}
			root.add(userNode);
		}

		if(getProject() != null) {
			final ResourceLoader<URL> projectLoader = library.getProjectGraphs(getProject());
			final Iterator<URL> projectIterator = projectLoader.iterator();
			if(projectIterator.hasNext()) {
				final DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode("Project " + getModel().getNoun().getObj2(), true);
				while(projectIterator.hasNext()) {
					final URL documentURL = projectIterator.next();

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
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				}

				root.add(projectNode);
			}
		}
	}

	public OpgraphEditorModel getModel() {
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
		props.setTitle("Save " + getModel().getNoun().getObj1());

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
				try {
					@SuppressWarnings("unchecked")
					final List<File> fileList =
							(List<File>)support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
					addDocuments(fileList);
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
	                MacroNode srcNode = macroNodes.remove(origIdx);

	                if(idx < 0) {
	                	idx = macroNodes.size();
	                } else if(idx > origIdx) {
	                	--idx;
	                }
	                macroNodes.add(idx, srcNode);

	                ((NodeTableModel)nodeTable.getModel()).fireTableDataChanged();
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
			return 2;
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
				} else if(node.getUserObject() instanceof QueryScript) {
					final QueryScript queryScript = (QueryScript)node.getUserObject();
					final QueryName queryName = queryScript.getExtension(QueryName.class);
					if(queryName != null) {
						retVal.setText(queryName.getName());
					}
				}
			}

			return retVal;
		}

	}

	private class DocumentSettingsPanel extends JPanel {

		private final MacroNode analysisNode;

		private JComboBox<OpNode> settingsNodeBox;
		private CardLayout settingsLayout;
		private JPanel settingsPanel;

		public DocumentSettingsPanel(MacroNode analysisNode) {
			super();

			this.analysisNode = analysisNode;

			init();
			update();
		}

		private void init() {
			setLayout(new BorderLayout());

			this.settingsNodeBox = new JComboBox<>();
			this.settingsNodeBox.setRenderer(new ListCellRenderer());
			settingsNodeBox.addItemListener( (e) -> {
				if(e.getStateChange() == ItemEvent.SELECTED) {
					final OpNode node = (OpNode)e.getItem();
					settingsLayout.show(settingsPanel, node.getId());
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
			final List<OpNode> settingsNodes = new ArrayList<>();
			for(int i = 0; i < analysisExt.size(); i++) {
				final OpNode node = analysisExt.getNode(i);
				final NodeSettings nodeSettings = node.getExtension(NodeSettings.class);
				if(nodeSettings != null) {
					settingsNodes.add(node);

					settingsPanel.add(nodeSettings.getComponent(getModel().getDocument()),
							node.getId());
				}
			}

			final DefaultComboBoxModel<OpNode> boxModel = new DefaultComboBoxModel<>(settingsNodes.toArray(new OpNode[0]));
			settingsNodeBox.setModel(boxModel);

			if(settingsNodes.size() > 0) {
				settingsNodeBox.setSelectedIndex(0);
				settingsLayout.show(settingsPanel, settingsNodes.get(0).getId());
			}
		}

	}

}
