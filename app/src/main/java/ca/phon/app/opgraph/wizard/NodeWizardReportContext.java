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
