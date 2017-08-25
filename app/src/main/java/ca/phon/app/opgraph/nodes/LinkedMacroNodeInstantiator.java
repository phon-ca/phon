package ca.phon.app.opgraph.nodes;

import ca.gedge.opgraph.library.instantiators.Instantiator;
import ca.gedge.opgraph.nodes.general.LinkedMacroNode;

public class LinkedMacroNodeInstantiator implements Instantiator<LinkedMacroNode> {

	@Override
	public LinkedMacroNode newInstance(Object... params) throws InstantiationException {
		if(params.length < 1) 
			throw new InstantiationException("Incorrect number of parameters");
		final Object obj = params[0];
		if(!(obj instanceof LinkedMacroNodeData)) 
			throw new InstantiationException("Incorrect node data type");
		final LinkedMacroNodeData nodeData = (LinkedMacroNodeData)obj;
		
		return new LinkedMacroNode(nodeData.getGraphURI());
	}

}
