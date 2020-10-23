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
package ca.phon.app.opgraph.nodes.table;

import java.net.*;

import ca.phon.opgraph.*;
import ca.phon.opgraph.library.*;
import ca.phon.opgraph.library.instantiators.*;
import ca.phon.script.*;

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
