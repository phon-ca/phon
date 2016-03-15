package ca.phon.app.opgraph.nodes.query;

import ca.gedge.opgraph.OpNodeInfo;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.phon.app.query.ScriptPanel;
import ca.phon.script.BasicScript;
import ca.phon.script.PhonScript;

/**
 * Base class for script operations on tables.
 *
 */
public abstract class TableScriptNode extends TableOpNode implements NodeSettings {

	// script
	private PhonScript script = new BasicScript("");
	
	// UI
	private ScriptPanel scriptPanel = new ScriptPanel();
	
}
