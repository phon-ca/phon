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
package ca.phon.app.opgraph.syllabifier;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.phon.app.opgraph.editor.EditorModelInstantiator;
import ca.phon.app.opgraph.editor.EditorModelInstantiator.EditorModelInstantiatorMenuInfo;
import ca.phon.ipa.IPATranscript;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.syllabifier.opgraph.OpGraphSyllabifier;
import ca.phon.syllabifier.opgraph.nodes.SonorityNode;

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
		} catch (ItemMissingException | CycleDetectedException | VertexNotFoundException e) {
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
 
}
