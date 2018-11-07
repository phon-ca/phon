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
package ca.phon.app.opgraph.nodes;

import ca.phon.app.log.LogUtil;
import ca.phon.app.opgraph.wizard.WizardExtension;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.app.util.GraphUtils;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.script.PhonScript;
import ca.phon.script.PhonScriptException;
import ca.phon.script.params.ScriptParam;
import ca.phon.script.params.ScriptParameters;

/**
 * Add Query Reports as {@link MacroNode}s in the graph.  Fields for project
 * and queryId will be published.
 * 
 */
public class ReportNodeInstantiator extends MacroNodeInstantiator {

	@Override
	public MacroNode newInstance(Object... params) throws InstantiationException {
		final MacroNode node = super.newInstance(params);
		final OpGraph graph = node.getGraph();
		// update graph ids to make them unique
		GraphUtils.changeNodeIds(graph);
		
		final OpNode projectNode = graph.getNodesByName("Project").stream().findFirst().orElse(null);
		if(projectNode == null)
			throw new InstantiationException("Project node not found in report document");
		
		final OpNode queryIdNode = graph.getNodesByName("Query ID").stream().findFirst().orElse(null);
		if(queryIdNode == null)
			throw new InstantiationException("Query ID node not found in report document");
		
		final OpNode selectedResultsNode = graph.getNodesByName("Selected Results").stream().findFirst().orElse(null);
		if(selectedResultsNode == null)
			throw new InstantiationException("Selected Results node not found in report document");
		
		node.publish("project", projectNode, projectNode.getInputFieldWithKey("obj"));
		node.publish("queryId", queryIdNode, queryIdNode.getInputFieldWithKey("obj"));
		node.publish("selectedResults", selectedResultsNode, selectedResultsNode.getInputFieldWithKey("obj"));
		
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
