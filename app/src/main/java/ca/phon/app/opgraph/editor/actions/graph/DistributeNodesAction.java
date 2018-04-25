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
package ca.phon.app.opgraph.editor.actions.graph;

import java.awt.event.ActionEvent;
import java.util.*;

import javax.swing.*;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.edits.graph.DistributeNodesEdit;
import ca.phon.util.icons.*;

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
