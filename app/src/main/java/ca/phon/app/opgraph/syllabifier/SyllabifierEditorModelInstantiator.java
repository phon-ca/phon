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
package ca.phon.app.opgraph.syllabifier;

import java.io.*;

import ca.phon.app.opgraph.editor.*;
import ca.phon.app.opgraph.editor.EditorModelInstantiator.*;
import ca.phon.ipa.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.dag.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.nodes.reflect.*;
import ca.phon.plugin.*;
import ca.phon.syllabifier.opgraph.*;
import ca.phon.syllabifier.opgraph.nodes.*;

@EditorModelInstantiatorMenuInfo(
		name="Syllabifier",
		tooltip="New syllabifier...",
		modelType=OpGraphSyllabifierEditorModel.class)
public class SyllabifierEditorModelInstantiator implements EditorModelInstantiator, IPluginExtensionPoint<EditorModelInstantiator> {

	@Override
	public OpGraphSyllabifierEditorModel createModel(OpGraph graph) {
		if(graph.getVertices().size() == 0) {
			setupGraph(graph);
		}
		return new OpGraphSyllabifierEditorModel(graph);
	}
	
	private void setupGraph(OpGraph graph) {
		// add IPASourceNode
		final ObjectNode sourceNode = new ObjectNode(IPATranscript.class);
		sourceNode.setContextKey(OpGraphSyllabifier.IPA_CONTEXT_KEY);
		graph.add(sourceNode);
		
		// setup sonority scale
		final SonorityNode sonorityNode = new SonorityNode();
		graph.add(sonorityNode);
		
		try {
			final OpLink link = new OpLink(sourceNode, sourceNode.getOutputFieldWithKey("obj"),
					sonorityNode, sonorityNode.getInputFieldWithKey("ipa"));
			graph.add(link);
		} catch (ItemMissingException | CycleDetectedException | VertexNotFoundException | InvalidEdgeException e) {
		}
	}

	@Override
	public Class<?> getExtensionType() {
		return EditorModelInstantiator.class;
	}

	@Override
	public IPluginExtensionFactory<EditorModelInstantiator> getFactory() {
		return (args) -> this;
	}

	@Override
	public OpGraph defaultTemplate() throws IOException {
		OpGraph graph = new OpGraph();
		setupGraph(graph);
		return graph;
	}
 
}
