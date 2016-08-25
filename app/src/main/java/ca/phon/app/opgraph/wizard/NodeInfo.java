package ca.phon.app.opgraph.wizard;

import ca.gedge.opgraph.OpNode;

public class NodeInfo extends WizardInfo {

	private OpNode node;
	
	private boolean settingsForced = false;
	
	public NodeInfo(OpNode node) {
		this(node, "");
	}
	
	public NodeInfo(OpNode node, String title) {
		this(node, title, "");
	}
	
	public NodeInfo(OpNode node, String title, String message) {
		this(node, title, message, WizardInfoMessageFormat.HTML);
	}
	
	public NodeInfo(OpNode node, String title, String message, WizardInfoMessageFormat format) {
		super(title, message, format);
		this.node = node;
	}
	
	public OpNode getNode() {
		return this.node;
	}
	
	public void setNode(OpNode node) {
		this.node = node;
	}
	
	public boolean isSettingsForced() {
		return this.settingsForced;
	}
	
	public void setSettingsForced(boolean settingsForced) {
		this.settingsForced = settingsForced;
	}
	
}
