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
package ca.phon.app.opgraph.nodes;

import java.net.URI;
import java.net.URL;

import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.library.NodeData;
import ca.phon.opgraph.library.instantiators.Instantiator;

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
