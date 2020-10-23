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
package ca.phon.app.opgraph.editor.actions.debug;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

import ca.phon.app.opgraph.editor.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;

public class StepToAction extends OpgraphDebugAction {

	private OpNode stepToNode = null;
	
	private final static String TXT = "Step to current node";
	
	public StepToAction(OpgraphEditor editor) {
		this(editor, null);
	}
	
	public StepToAction(OpgraphEditor editor, OpNode stepToNode) {
		super(editor);
		
		this.stepToNode = stepToNode;
		
		putValue(NAME, TXT);
	}
	
	
	@Override
	public void hookableActionPerformed(ActionEvent arg0) {
		final GraphDocument document = getEditor().getModel().getDocument();
		final OpNode node = (stepToNode != null ? stepToNode : getEditor().getModel().getCanvas().getSelectionModel().getSelectedNode());
		
		Stack<OpNode> nodeStack = new Stack<OpNode>();
		List<OpNode> nodePath = document.getRootGraph().getNodePath(node.getId());
		Collections.reverse(nodePath);
		nodePath.forEach(nodeStack::add);
		
		Runnable inBg =	new Runnable() {
			public void run() {
				if(document != null && node != null) {
					final Processor context = getProcessor(document);
					while(!nodeStack.isEmpty() && context.hasNext()) {
						if(context.getNodeToProcess() == nodeStack.peek()) {
							nodeStack.pop();
							if(!nodeStack.isEmpty()) {
								context.stepInto();
							}
						} else {
							context.step();
						}
					}
					SwingUtilities.invokeLater( () -> {
						document.updateDebugState(context);
						getEditor().getModel().getCanvas().updateDebugState(context);
					});
				}
			}
		};
		getOpgraphThread().invokeLater(inBg);
	}

}
