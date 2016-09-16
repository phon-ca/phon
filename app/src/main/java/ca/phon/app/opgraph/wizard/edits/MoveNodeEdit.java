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
package ca.phon.app.opgraph.wizard.edits;

import ca.gedge.opgraph.OpNode;
import ca.phon.app.opgraph.wizard.NodeWizardPanel;

public class MoveNodeEdit extends WizardExtensionUndoableEdit {

	private int oldIndex = -1;
	
	private OpNode node = null;
	
	private int newIndex = -1;
	
	public MoveNodeEdit(NodeWizardPanel panel, OpNode node, int newIndex) {
		super(panel);
		
		this.node = node;
		this.newIndex = newIndex;
	}
	
	public void doIt() {
		oldIndex = getWizardExtension().indexOf(node);
		getWizardExtension().removeNode(node);
		getWizardExtension().addNode(newIndex, node);
		getWizardPanel().updateTable();
	}
	
	@Override
	public void undo() {
		getWizardExtension().removeNode(node);
		getWizardExtension().addNode(oldIndex, node);
		getWizardPanel().updateTable();
	}
	
	@Override
	public void redo() {
		doIt();
	}

}
