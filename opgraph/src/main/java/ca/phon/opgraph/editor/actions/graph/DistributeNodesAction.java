package ca.phon.opgraph.editor.actions.graph;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.GraphDocument;
import ca.gedge.opgraph.app.edits.graph.DistributeNodesEdit;
import ca.phon.opgraph.editor.OpgraphEditor;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.util.icons.IconManager;
import ca.phon.util.icons.IconSize;

public class DistributeNodesAction extends OpgraphEditorAction {

	private static final long serialVersionUID = 9079609000731201100L;

	private int direction;
	
	public DistributeNodesAction(OpgraphEditor editor, int direction) {
		super(editor);
		
		this.direction = direction;
		
		putValue(NAME, getName());
		putValue(SHORT_DESCRIPTION, getName());
		putValue(SMALL_ICON, getIcon());
	}
	
	protected String getName() {
		return (direction == SwingConstants.HORIZONTAL ? "Distribute evenly (horizontal)" : "Distribute evenly (vertical)");
	}
	
	protected ImageIcon getIcon() {
		final String iconName = 
				(direction == SwingConstants.HORIZONTAL ? "actions/distribute-horizontal-equal" : 
					"actions/distribute-vertical-equal");
		return IconManager.getInstance().getIcon(iconName, IconSize.SMALL);
	}

	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final GraphDocument document = getEditor().getModel().getDocument();
		if(document != null) {
			final Collection<OpNode> selectedNodes = document.getSelectionModel().getSelectedNodes();
			
			// nodes should be sorted
			final List<OpNode> sortedNodes = document.getGraph().getVertices();
			final List<Integer> nodeLocations = new ArrayList<>();
			for(OpNode node:selectedNodes) {
				nodeLocations.add(sortedNodes.indexOf(node));
			}
			Collections.sort(nodeLocations);
			
			final List<OpNode> sortedSelection = new ArrayList<>();
			for(Integer idx:nodeLocations) sortedSelection.add(sortedNodes.get(idx));
			
			if(selectedNodes.size() > 1) {
				document.getUndoSupport().postEdit(new DistributeNodesEdit(sortedSelection, direction));
			}
		}
	}

}
