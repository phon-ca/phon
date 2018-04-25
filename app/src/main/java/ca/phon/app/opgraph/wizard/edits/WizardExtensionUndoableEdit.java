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

import javax.swing.undo.*;

import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpGraph;

public class WizardExtensionUndoableEdit extends AbstractUndoableEdit {

	private static final long serialVersionUID = 1L;

	private final OpGraph graph;
	private final WizardExtension oldValue;
	private final WizardExtension newValue;
	
	public WizardExtensionUndoableEdit(OpGraph graph, WizardExtension oldValue, WizardExtension newValue) {
		super();
		
		this.graph = graph;
		this.newValue = newValue;
		this.oldValue = oldValue;
		
		graph.putExtension(WizardExtension.class, this.newValue);
	}
	
	@Override
	public String getPresentationName() {
		return "Wizard Settings";
	}
	
	@Override
	public boolean canRedo() {
		return true;
	}
	
	@Override
	public boolean canUndo() {
		return true;
	}

	public OpGraph getGraph() {
		return graph;
	}

	public WizardExtension getOldValue() {
		return oldValue;
	}

	public WizardExtension getNewValue() {
		return newValue;
	}

	@Override
	public void undo() throws CannotUndoException {
		graph.putExtension(WizardExtension.class, getOldValue());
	}

	@Override
	public void redo() throws CannotRedoException {
		graph.putExtension(WizardExtension.class, getNewValue());
	}
	
}
