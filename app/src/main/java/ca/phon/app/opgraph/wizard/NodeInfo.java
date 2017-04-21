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
