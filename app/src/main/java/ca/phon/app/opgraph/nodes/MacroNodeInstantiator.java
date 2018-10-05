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

import java.io.IOException;
import java.io.InputStream;

import ca.phon.app.opgraph.OpgraphIO;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.app.util.GraphUtils;
import ca.phon.opgraph.library.instantiators.Instantiator;
import ca.phon.opgraph.nodes.general.MacroNode;

/**
 * This class will generate a new macro node given the location
 * of an {@link OpGraph} document.
 * 
 */
public class MacroNodeInstantiator implements Instantiator<MacroNode> {

	@Override
	public MacroNode newInstance(Object... params) throws InstantiationException {
		if(params.length < 1) 
			throw new InstantiationException("Incorrect number of parameters");
		final Object obj = params[0];
		if(!(obj instanceof MacroNodeData)) 
			throw new InstantiationException("Incorrect node data type");
		final MacroNodeData nodeData = (MacroNodeData)obj;
		
		MacroNode retVal = null;
		
		// read graph document
		try(InputStream is = nodeData.getAnalysisURL().openStream()) {
			final OpGraph graph = OpgraphIO.read(is);
			GraphUtils.changeNodeIds(graph);
			
			if(graph.getVertices().size() == 1 && graph.getVertices().get(0) instanceof MacroNode) {
				// use the macro node from the graph
				retVal = (MacroNode)graph.getVertices().get(0);
			} else {
				retVal = new MacroNode(graph);
				
				// find input nodes and publish fields
				final OpNode projectNode = graph.getNodesByName("Project").stream().findFirst().orElse(null);
				if(projectNode != null) {
					// publish obj field to project
					retVal.publish("project", projectNode, projectNode.getInputFieldWithKey("obj"));
				}
			}
			retVal.setName(nodeData.name);
		} catch (IOException e) {
			throw new InstantiationException(e.getLocalizedMessage());
		}
		
		return retVal;
	}

}
