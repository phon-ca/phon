package ca.phon.app.opgraph.nodes;

import java.io.IOException;
import java.io.InputStream;

import ca.gedge.opgraph.OpGraph;
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
			}
			retVal.setName(nodeData.name);
		} catch (IOException e) {
			throw new InstantiationException(e.getLocalizedMessage());
		}
		
		return retVal;
	}

}
