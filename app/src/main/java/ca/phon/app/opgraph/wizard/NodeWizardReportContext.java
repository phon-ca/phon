/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
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

import org.apache.velocity.VelocityContext;
import org.apache.velocity.tools.*;

public class NodeWizardReportContext {
	
	private final VelocityContext context;
	
	public NodeWizardReportContext() {
		super();
		
		final ToolManager toolManager = new ToolManager();
		final ToolContext toolContext = toolManager.createContext();
		
		this.context = new VelocityContext(toolContext);
	}

	public boolean containsKey(Object key) {
		return context.containsKey(key);
	}

	public Object get(String key) {
		return context.get(key);
	}

	public Object[] getKeys() {
		return context.getKeys();
	}

	public Object put(String key, Object value) {
		return context.put(key, value);
	}

	public Object remove(Object key) {
		return context.remove(key);
	}
	
	VelocityContext velocityContext() {
		return this.context;
	}

}
