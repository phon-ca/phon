/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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

import javax.swing.undo.*;

import ca.phon.app.opgraph.wizard.*;
import ca.phon.opgraph.*;

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
