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
package ca.phon.app.opgraph.editor.actions.graph;

import ca.phon.app.opgraph.editor.OpgraphEditor;
import ca.phon.app.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.app.GraphDocument;
import ca.phon.opgraph.app.edits.graph.DistributeNodesEdit;
import ca.phon.util.icons.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.*;

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
