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
package ca.phon.app.opgraph.wizard;

import ca.phon.opgraph.OpNode;

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
