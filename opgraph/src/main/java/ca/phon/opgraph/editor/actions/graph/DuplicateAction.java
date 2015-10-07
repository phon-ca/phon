package ca.phon.opgraph.editor.actions.graph;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.KeyStroke;
import javax.swing.undo.CompoundEdit;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.edits.graph.AddLinkEdit;
import ca.gedge.opgraph.app.edits.graph.AddNodeEdit;
import ca.gedge.opgraph.app.edits.graph.DeleteNodesEdit;
import ca.gedge.opgraph.app.extensions.NodeMetadata;
import ca.gedge.opgraph.app.util.GraphUtils;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;

public class DuplicateAction extends OpgraphEditorAction {
	
	private final static Logger LOGGER = Logger.getLogger(DuplicateAction.class.getName());

	private static final long serialVersionUID = -2540261955364824184L;
	
	public final static String TXT = "Duplicate nodes";
	
	public final static String DESC = "Duplicate selected nodes";
	
	public final static KeyStroke KS = KeyStroke.getKeyStroke(KeyEvent.VK_D, 
			Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

	public DuplicateAction(OpgraphEditor editor) {
		super(editor);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
		putValue(ACCELERATOR_KEY, KS);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final GraphDocument document = getEditor().getModel().getDocument();
		if(document != null) {
			// Check to make sure the clipboard has something we can paste
			final Collection<OpNode> selectedNodes = document.getSelectionModel().getSelectedNodes();
			if(isEnabled() && selectedNodes.size() > 0) {
				final CompoundEdit cmpEdit = new CompoundEdit();
				final OpGraph graph = document.getGraph();
				final Map<String, String> nodeMap = new HashMap<String, String>();

				// Create a new node edit for each node in the contents
				final Collection<OpNode> newNodes = new ArrayList<OpNode>();
				for(OpNode node : selectedNodes) {
					// Clone the node
					final OpNode newNode = GraphUtils.cloneNode(node);
					newNodes.add(newNode);
					nodeMap.put(node.getId(), newNode.getId());

					// Offset to avoid pasting on top of current nodes
					final NodeMetadata metadata = newNode.getExtension(NodeMetadata.class);
					if(metadata != null) {
						metadata.setX(metadata.getX() + 50);
						metadata.setY(metadata.getY() + 30);
					}

					// Add an undoable edit for this node
					cmpEdit.addEdit(new AddNodeEdit(graph, newNode));
				}

				// Duplicated nodes become the selection
				document.getSelectionModel().setSelectedNodes(newNodes);

				// For each selected node, copy outgoing links if they fully contained in the selection
				for(OpNode selectedNode : selectedNodes) {
					final Collection<OpLink> outgoingLinks = graph.getOutgoingEdges(selectedNode);
					for(OpLink link : outgoingLinks) {
						if(selectedNodes.contains(link.getDestination())) {
							final OpNode srcNode = graph.getNodeById(nodeMap.get(link.getSource().getId()), false);
							final OutputField srcField = srcNode.getOutputFieldWithKey(link.getSourceField().getKey());
							final OpNode dstNode = graph.getNodeById(nodeMap.get(link.getDestination().getId()), false);
							final InputField dstField = dstNode.getInputFieldWithKey(link.getDestinationField().getKey());

							try {
								final OpLink newLink = new OpLink(srcNode, srcField, dstNode, dstField);
								cmpEdit.addEdit(new AddLinkEdit(graph, newLink));
							} catch(VertexNotFoundException exc) {
								LOGGER.severe(exc.getMessage());
							} catch(CycleDetectedException exc) {
								LOGGER.severe(exc.getMessage());
							} catch(ItemMissingException exc) {
								LOGGER.severe(exc.getMessage());
							}
						}
					}
				}

				// Add the compound edit to the undo manager
				cmpEdit.end();
				document.getUndoSupport().postEdit(cmpEdit);
			}
		}
	}

}
