package ca.phon.app.opgraph.nodes;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.exceptions.ProcessingException;

@OpNodeInfo(
		name="Script",
		category="Utility",
		description="Generic script node with optional parameter setup.",
		showInLibrary=true
)
public class PhonScriptNode extends OpNode {

	@Override
	public void operate(OpContext context) throws ProcessingException {
		
	}

}
