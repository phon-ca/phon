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
