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
package ca.phon.app.opgraph.nodes;

import java.io.IOException;
import java.io.InputStream;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.app.util.GraphUtils;
import ca.gedge.opgraph.library.instantiators.Instantiator;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.phon.opgraph.OpgraphIO;

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
