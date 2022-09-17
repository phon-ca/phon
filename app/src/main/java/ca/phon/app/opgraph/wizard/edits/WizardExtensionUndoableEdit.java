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
package ca.phon.app.opgraph.wizard.edits;

import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpGraph;

import javax.swing.undo.*;

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
