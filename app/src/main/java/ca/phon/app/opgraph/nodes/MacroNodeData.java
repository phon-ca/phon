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
package ca.phon.app.opgraph.nodes;

import java.net.URI;
import java.net.URL;

import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.library.NodeData;
import ca.gedge.opgraph.library.instantiators.Instantiator;

public class MacroNodeData extends NodeData {
	
	private URL analysisURL;

	public MacroNodeData(URL analysisURL, URI uri, String name, String description, String category,
			Instantiator<? extends OpNode> instantiator) {
		super(uri, name, description, category, instantiator);
		this.analysisURL = analysisURL;
	}

	public URL getAnalysisURL() {
		return this.analysisURL;
	}
	
	public void setAnalysisURL(URL url) {
		this.analysisURL = url;
	}
	
}
