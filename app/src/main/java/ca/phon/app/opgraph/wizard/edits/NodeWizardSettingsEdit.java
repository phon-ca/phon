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
package ca.phon.app.opgraph.wizard.edits;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;

public class NodeWizardSettingsEdit extends AbstractUndoableEdit {
	
	private OpGraph graph;
	
	private WizardExtension wizardExtension;
	
	private OpNode node;
	
	private boolean addNodeToSettings;
	
	private boolean forced;
	
	public NodeWizardSettingsEdit(OpGraph graph, WizardExtension wizardExt, OpNode node, boolean addNodeToSettings, boolean forced) {
		super();
		
		this.graph = graph;
		this.wizardExtension = wizardExt;
		this.node = node;
		this.addNodeToSettings = addNodeToSettings;
		this.forced = forced;
		
		if(addNodeToSettings) {
			wizardExt.addNode(node);
			wizardExt.setNodeForced(node, forced);
		} else {
			wizardExt.removeNode(node);
		}
	}

	@Override
	public void undo() throws CannotUndoException {
		super.undo();
		
		if(addNodeToSettings) {
			wizardExtension.removeNode(node);
		} else {
			wizardExtension.addNode(node);
			wizardExtension.setNodeForced(node, forced);
		}
	}

	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public void redo() throws CannotRedoException {
		super.redo();
		
		if(addNodeToSettings) {
			wizardExtension.addNode(node);
			wizardExtension.setNodeForced(node, forced);
		} else {
			wizardExtension.removeNode(node);
		}
	}

	@Override
	public boolean canRedo() {
		return true;
	}
	
}
