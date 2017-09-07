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

import ca.gedge.opgraph.*;
import ca.phon.app.opgraph.wizard.WizardExtension;

public class NodeWizardOptionalsEdit extends AbstractUndoableEdit {

	private OpGraph graph;
	
	private WizardExtension wizardExtension;
	
	private OpNode node;
	
	private boolean addNodeToOptionals;
	
	private boolean defaultValue;
	
	public NodeWizardOptionalsEdit(OpGraph graph, WizardExtension wizardExt, OpNode node, boolean addNodeToOptionals, boolean defaultValue) {
		super();
		
		this.graph = graph;
		this.wizardExtension = wizardExt;
		this.node = node;
		this.addNodeToOptionals = addNodeToOptionals;
		this.defaultValue = defaultValue;
		
		if(addNodeToOptionals) {
			wizardExt.addOptionalNode(node);
			wizardExt.setOptionalNodeDefault(node, defaultValue);
		} else {
			wizardExt.removeOptionalNode(node);
		}
	}
	
	@Override
	public boolean canUndo() {
		return true;
	}

	@Override
	public void undo() {
		super.undo();
		
		if(addNodeToOptionals) {
			wizardExtension.removeOptionalNode(node);
		} else {
			wizardExtension.addOptionalNode(node);
			wizardExtension.setOptionalNodeDefault(node, defaultValue);
		}
	}
	
	@Override
	public boolean canRedo() {
		return true;
	}
	
	@Override
	public void redo() {
		super.redo();
		
		if(addNodeToOptionals) {
			wizardExtension.addOptionalNode(node);
			wizardExtension.setOptionalNodeDefault(node, defaultValue);
		} else {
			wizardExtension.removeOptionalNode(node);
		}
	}
	
}
