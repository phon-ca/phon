package ca.phon.app.opgraph.analysis;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.TransferHandler.TransferSupport;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FilenameUtils;
import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.VerticalLayout;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.edits.graph.AddNodeEdit;
import ca.gedge.opgraph.app.edits.graph.DeleteNodesEdit;
import ca.gedge.opgraph.app.edits.graph.MoveNodesEdit;
import ca.gedge.opgraph.app.edits.notes.RemoveNoteEdit;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.extensions.NodeMetadata;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.phon.app.opgraph.editor.EditorModelInstantiator;
import ca.phon.app.opgraph.editor.actions.file.NewAction;
import ca.phon.app.opgraph.nodes.project.SessionSelectorNode;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.app.opgraph.wizard.edits.NodeWizardOptionalsEdit;
import ca.phon.opgraph.OpgraphIO;
import ca.phon.project.Project;
import ca.phon.ui.action.PhonUIAction;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxState;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTree;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellEditor;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeCellRenderer;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeModel;
import ca.phon.ui.tristatecheckbox.TristateCheckBoxTreeNode;
import ca.phon.util.resources.ResourceLoader;

public class AnalysisGraphGeneratorPanel extends JPanel {
	
	private static final long serialVersionUID = -7170934782795308487L;

	private final static Logger LOGGER = Logger.getLogger(AnalysisGraphGeneratorPanel.class.getName());

	private final static int Y_START = 50;
	private final static int X_START = 400;
	private final static int Y_SEP = 150;

	private JButton addButton;
	private JButton removeButton;
	private TristateCheckBoxTree analysisTree;

	private JButton moveUpButton;
	private JButton moveDownButton;
	private JXTable analysisNodeTable;
	private List<OpNode> analysisNodes;

	private JComboBox<OpNode> settingsNodeBox;
	private CardLayout settingsLayout;
	private JPanel settingsPanel;

	private final AnalysisOpGraphEditorModel model;

	private final Project project;
	
	/**
	 * Constructor
	 *
	 * @param project if <code>null</code> project graphs will not be displayed
	 */
	public AnalysisGraphGeneratorPanel(Project project) {
		super();

		final EditorModelInstantiator instantiator = new AnalysisEditorModelInstantiator();
		model = (AnalysisOpGraphEditorModel)instantiator.createModel(new OpGraph());

		this.project = project;

		init();
	}

	private void init() {
		setLayout(new BorderLayout());

		final JPanel p1 = new JPanel(new BorderLayout());

		final PhonUIAction addAct = new PhonUIAction(this, "onAdd");
		addAct.putValue(PhonUIAction.NAME, "Add >");
		addAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Add checked analyses");
		addButton = new JButton(addAct);

		final PhonUIAction removeAct = new PhonUIAction(this, "onRemove");
		removeAct.putValue(PhonUIAction.NAME, "< Remove");
		removeAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Remove selected analses");
		removeButton = new JButton(removeAct);

		final TristateCheckBoxTreeModel analysisTreeModel = createTreeModel();
		analysisTree = new TristateCheckBoxTree(analysisTreeModel);
		analysisTree.setCellRenderer(new TreeNodeRenderer());
		final TreeNodeRenderer editorRenderer = new TreeNodeRenderer();
		analysisTree.setCellEditor(new TristateCheckBoxTreeCellEditor(analysisTree, editorRenderer));
		analysisTree.setVisibleRowCount(10);
		final JScrollPane analysisScroller = new JScrollPane(analysisTree);

		p1.add(analysisScroller, BorderLayout.CENTER);

		final JPanel bp1 = new JPanel(new VerticalLayout());
		bp1.add(addButton);
		bp1.add(removeButton);
		p1.add(bp1, BorderLayout.EAST);
		
		final JPanel p2 = new JPanel(new BorderLayout());

		final PhonUIAction upAct = new PhonUIAction(this, "onMoveUp");
		upAct.putValue(PhonUIAction.NAME, "Up");
		upAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected analysis up");
		moveUpButton = new JButton(upAct);

		final PhonUIAction downAct = new PhonUIAction(this, "onMoveDown");
		downAct.putValue(PhonUIAction.NAME, "Down");
		downAct.putValue(PhonUIAction.SHORT_DESCRIPTION, "Move selected analysis down");
		moveDownButton = new JButton(downAct);

		analysisNodes = new ArrayList<>();
		analysisNodeTable = new JXTable(new AnalysisNodeTableModel());
		analysisNodeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		analysisNodeTable.setSortable(false);
		analysisNodeTable.getSelectionModel().addListSelectionListener( (e) -> {
			final int selectedRow = analysisNodeTable.getSelectedRow();
			if(selectedRow >= 0 && selectedRow < analysisNodes.size()) {
				final OpNode analysisNode = analysisNodes.get(selectedRow);
				setSelectedAnalysisNode(analysisNode);
			}
		});
		analysisNodeTable.setDragEnabled(true);
		analysisNodeTable.setTransferHandler(new AnalysisNodeTableTransferHandler());
		analysisNodeTable.setDropMode(DropMode.INSERT);
		analysisNodeTable.setVisibleRowCount(10);
		final JScrollPane analysisNodeScroller = new JScrollPane(analysisNodeTable);

		p2.add(analysisNodeScroller, BorderLayout.CENTER);

		final JPanel bp2 = new JPanel(new VerticalLayout());
		bp2.add(moveUpButton);
		bp2.add(moveDownButton);
		p2.add(bp2, BorderLayout.EAST);
		
		final JPanel p3 = new JPanel(new BorderLayout());

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

		p3.add(settingsNodeBox, BorderLayout.NORTH);
		p3.add(settingsPanel, BorderLayout.CENTER);
		
		final JPanel leftPanel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		leftPanel.add(p1, gbc);
		++gbc.gridx;
		leftPanel.add(p2, gbc);
		
		add(leftPanel, BorderLayout.NORTH);
		add(p3, BorderLayout.CENTER);

	}
	
	private void setSelectedAnalysisNode(OpNode analysisNode) {
		final MacroNode macroNode = (MacroNode)analysisNode;
		final OpGraph analysisGraph = macroNode.getGraph();
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
	
	public void onAdd() {
		// add all checked analysis as new nodes in graph
		final List<TreePath> checkedPaths = analysisTree.getCheckedPaths();
		int selectionIndex = analysisNodes.size();
		for(TreePath checkedPath:checkedPaths) {
			final TristateCheckBoxTreeNode node = 
					(TristateCheckBoxTreeNode)checkedPath.getLastPathComponent();
			if(node.isLeaf() && node.getUserObject() instanceof URL) {
				final URL analysisURL = (URL)node.getUserObject();
				
				// create analysis node
				try(InputStream is = analysisURL.openStream()) {
					final String analysisFile = URLDecoder.decode(analysisURL.toString(), "UTF-8");
					final String analysisName = FilenameUtils.getBaseName(analysisFile);
					
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
					
					final AddNodeEdit addEdit = new AddNodeEdit(getGraph(), analysisNode);
					getModel().getDocument().getUndoSupport().postEdit(addEdit);
					
					final NodeWizardOptionalsEdit optEdit = 
							new NodeWizardOptionalsEdit(getGraph(), getGraph().getExtension(WizardExtension.class), analysisNode, true, true);
					getModel().getDocument().getUndoSupport().postEdit(optEdit);
					
					analysisNodes.add(analysisNode);
					((AnalysisNodeTableModel)analysisNodeTable.getModel()).fireTableRowsInserted(analysisNodes.size()-1, analysisNodes.size()-1);
				} catch (IOException e) {
					// TODO show message to user
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
		}
		analysisNodeTable.setRowSelectionInterval(selectionIndex, selectionIndex);
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

	private TristateCheckBoxTreeModel createTreeModel() {
		final TristateCheckBoxTreeNode root =
				new TristateCheckBoxTreeNode("All Analyses", TristateCheckBoxState.UNCHECKED, true, false);

		final AnalysisLibrary library = new AnalysisLibrary();
		final ResourceLoader<URL> stockAnalysisLoader = library.getStockGraphs();
		final Iterator<URL> stockItr = stockAnalysisLoader.iterator();
		if(stockItr.hasNext()) {
			final TristateCheckBoxTreeNode stockNode =
					new TristateCheckBoxTreeNode("Stock Analyses", TristateCheckBoxState.UNCHECKED, true, false);
			while(stockItr.hasNext()) {
				final URL analysisURL = stockItr.next();

				try {
					final String fullPath = URLDecoder.decode(analysisURL.getPath(), "UTF-8");
					String relativePath =
							fullPath.substring(fullPath.indexOf(AnalysisLibrary.ANALYSIS_FOLDER + "/")+AnalysisLibrary.ANALYSIS_FOLDER.length()+1);

					TristateCheckBoxTreeNode parentNode = stockNode;
					int splitIdx = -1;
					while((splitIdx = relativePath.indexOf('/')) >= 0) {
						final String nodeName = relativePath.substring(0, splitIdx);

						TristateCheckBoxTreeNode node = null;
						for(int i = 0; i < parentNode.getChildCount(); i++) {
							final TristateCheckBoxTreeNode childNode = (TristateCheckBoxTreeNode)parentNode.getChildAt(i);
							if(childNode.getUserObject().equals(nodeName)) {
								node = childNode;
								break;
							}
						}
						if(node == null) {
							node = new TristateCheckBoxTreeNode(nodeName, TristateCheckBoxState.UNCHECKED, true, false);
							parentNode.add(node);
						}
						parentNode = node;
					}

					final TristateCheckBoxTreeNode analysisNode =
							new TristateCheckBoxTreeNode(analysisURL, TristateCheckBoxState.UNCHECKED, false, false);
					parentNode.add(analysisNode);
				} catch (UnsupportedEncodingException e) {

				}
			}
			root.add(stockNode);
		}

		// TODO user graphs

		// TODO project graphs

		return new TristateCheckBoxTreeModel(root);
	}

	public AnalysisOpGraphEditorModel getModel() {
		return this.model;
	}

	public OpGraph getGraph() {
		return this.model.getDocument().getGraph();
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
			try {
                // convert data to string
                String s = (String)support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                final JTable.DropLocation dropLocation = 
                		(JTable.DropLocation)support.getDropLocation();
                
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
                
                return true;
            } catch (IOException | UnsupportedFlavorException e) {
            	
            }

            return false;
		}

		@Override
		public boolean canImport(TransferSupport support) {
			return support.isDataFlavorSupported(DataFlavor.stringFlavor);
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
			return node.getName();
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			return "Analysis";
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if(aValue.toString().trim().length() == 0) return;
			
			final OpNode node = analysisNodes.get(rowIndex);
			node.setName(aValue.toString());
		}
		
	}

	private class TreeNodeRenderer extends TristateCheckBoxTreeCellRenderer {

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			TristateCheckBoxTreeNodePanel retVal = (TristateCheckBoxTreeNodePanel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			if(value instanceof TristateCheckBoxTreeNode) {
				final TristateCheckBoxTreeNode node = (TristateCheckBoxTreeNode)value;
				if(node.getUserObject() instanceof URL) {
					final URL analysisURL = (URL)node.getUserObject();
					try {
						final String analysisFile = URLDecoder.decode(analysisURL.toString(), "UTF-8");
						final String analysisName = FilenameUtils.getBaseName(analysisFile);
						retVal.getLabel().setText(analysisName);
					} catch (UnsupportedEncodingException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
					}
				}
			}
			
			return retVal;
		}
		
	}
	
}
