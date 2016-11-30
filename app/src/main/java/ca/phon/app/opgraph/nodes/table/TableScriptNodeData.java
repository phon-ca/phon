package ca.phon.app.opgraph.nodes.table;

import java.net.URI;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.library.instantiators.Instantiator;
import ca.phon.script.PhonScript;

public class TableScriptNodeData extends NodeData {

	private PhonScript phonScript;
	
	public TableScriptNodeData(PhonScript phonScript, URI uri, String name, String description, String category,
			Instantiator<? extends OpNode> instantiator) {
		super(uri, name, description, category, instantiator);
		this.phonScript = phonScript;
	}
	
	public PhonScript getPhonScript() {
		return this.phonScript;
	}

	public void setPhonScript(PhonScript phonScript) {
		this.phonScript = phonScript;
	}
	
}
