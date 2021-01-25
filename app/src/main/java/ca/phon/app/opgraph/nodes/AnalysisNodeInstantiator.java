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
package ca.phon.app.opgraph.nodes;

import ca.phon.app.log.*;
import ca.phon.app.opgraph.wizard.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.library.instantiators.*;
import ca.phon.opgraph.nodes.general.*;
import ca.phon.script.*;
import ca.phon.script.params.*;

/**
 * Node {@link Instantiator} for analysis documents.  The {@link Instantiator}
 * will wrap the analysis document in a macro node and publish
 * fields for project, selectedSessions, and selectedParticipants.
 * 
 */
public class AnalysisNodeInstantiator extends MacroNodeInstantiator {
	
	@Override
	public MacroNode newInstance(Object... params) throws InstantiationException {
		MacroNode node = null;
		OpGraph graph = null;
		if(params.length == 1 && params[0] instanceof OpGraph) {
			graph = (OpGraph)params[0];
			node = new MacroNode(graph);
		} else {
			node = super.newInstance(params);
			graph = node.getGraph();
		}
		
		// set 'name' to Parameters node 'reportname' if available
		// find the 'Parameters' settings node
		final WizardExtension wizardExtension = graph.getExtension(WizardExtension.class);
		OpNode parametersNode = null;
		for(OpNode n:graph.getVertices()) {
			if(n.getName().equals("Parameters") && n instanceof PhonScriptNode
					&& graph.getNodePath(n.getId()).size() == 1) {
				parametersNode = n;
				break;
			}
		}
		LinkedMacroNodeOverrides nodeOverrides = new LinkedMacroNodeOverrides();
		for(OpNode settingsNode:wizardExtension) {
			nodeOverrides.getNodeOverrides().add(settingsNode);
		}
		node.putExtension(LinkedMacroNodeOverrides.class, nodeOverrides);
		
		String nodeName = "";
		if(parametersNode != null) {
			final PhonScriptNode scriptNode = (PhonScriptNode)parametersNode;
			final PhonScript script = scriptNode.getScript();

			try {
				final ScriptParameters scriptParams = script.getContext().getScriptParameters(script.getContext().getEvaluatedScope());
				for(ScriptParam sp:scriptParams) {
					if(sp.getParamIds().contains("reportTitle")) {
						nodeName = sp.getValue("reportTitle").toString();
						break;
					}
				}
			} catch (PhonScriptException e) {
				LogUtil.severe( e.getLocalizedMessage(), e);
			}
		}
		node.setName(nodeName);
		
		return node;
	}
	
}
