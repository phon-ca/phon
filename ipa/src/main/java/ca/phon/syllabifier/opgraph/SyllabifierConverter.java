/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.syllabifier.opgraph;

import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.OpLink;
import ca.phon.opgraph.OpNode;
import ca.phon.opgraph.OutputField;
import ca.phon.opgraph.app.AutoLayoutManager;
import ca.phon.opgraph.dag.CycleDetectedException;
import ca.phon.opgraph.dag.VertexNotFoundException;
import ca.phon.opgraph.exceptions.ItemMissingException;
import ca.phon.opgraph.nodes.general.MacroNode;
import ca.phon.opgraph.nodes.reflect.ObjectNode;
import ca.phon.syllabifier.basic.BasicSyllabifier;
import ca.phon.syllabifier.basic.io.SonorityValues;
import ca.phon.syllabifier.basic.io.SonorityValues.SonorityClass;
import ca.phon.syllabifier.basic.io.StageType;
import ca.phon.syllabifier.basic.io.SyllabifierDef;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.syllabifier.opgraph.nodes.MarkConstituentNode;
import ca.phon.syllabifier.opgraph.nodes.SonorityNode;
import ca.phon.util.Language;

/**
 * Convert a {@link BasicSyllabifier} into a new {@link OpGraphSyllabifier}
 * graph.
 *
 */
public class SyllabifierConverter {
	
	private final static org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(SyllabifierConverter.class.getName());
	
	public OpGraph syllabifierToGraph(BasicSyllabifier syllabifier) {
		final OpGraph graph = new OpGraph();
		final AutoLayoutManager layoutManager = new AutoLayoutManager();
		
		// setup settings
		final Language lang = syllabifier.getLanguage();
		final SyllabifierSettings settings = new SyllabifierSettings();
		settings.setLanguage(lang);
		settings.setName(syllabifier.getName());
		graph.putExtension(SyllabifierSettings.class, settings);
		
		// add IPASourceNode
		final ObjectNode sourceNode = new ObjectNode(IPATranscript.class);
		sourceNode.setContextKey(OpGraphSyllabifier.IPA_CONTEXT_KEY);
		graph.add(sourceNode);
		
		// setup sonority scale
		final SonorityNode sonorityNode = new SonorityNode();
		final StringBuffer sb = new StringBuffer();
		final SonorityValues sonorityVals = syllabifier.getSonorityScale();
		for(SonorityClass sc:sonorityVals.getSonorityClass()) {
			sb.append(sc.getSonorityValue()).append('=');
			if(sc.getPhonex().size() > 1) sb.append("[");
			for(String phonex:sc.getPhonex()) {
				sb.append(phonex);
			}
			if(sc.getPhonex().size() > 1) sb.append("]");
			sb.append("\n");
		}
		
		sonorityNode.setSonorityScale(sb.toString());
		graph.add(sonorityNode);
		
		try {
			final OpLink link = new OpLink(sourceNode, sourceNode.getOutputFieldWithKey("obj"),
					sonorityNode, sonorityNode.getInputFieldWithKey("ipa"));
			graph.add(link);
		} catch (ItemMissingException | CycleDetectedException | VertexNotFoundException e) {
			LOGGER.error( e.getLocalizedMessage(), e);
		}
		
		// convert stages
		final SyllabifierDef def = syllabifier.getDefinition();
		OpNode lastNode = sonorityNode;
		OutputField lastOutput = sonorityNode.getOutputFieldWithKey("ipa");
		for(StageType st:def.getStage()) {
			final MacroNode stageNode = new MacroNode();
			stageNode.setName(st.getName());
			final OpGraph stageGraph = stageNode.getGraph();
			
			OpNode lastStageNode = null;
			OutputField lastStageOutput = null;
			
			// create a new MarkConstituentNode for each expression
			int idx = 0;
			for(String phonex:st.getPhonex()) {
				final MarkConstituentNode phonexNode = new MarkConstituentNode();
				phonexNode.setPhonex(phonex);
				
				phonexNode.setName(st.getName() + " #" + (idx+1));
				
				stageGraph.add(phonexNode);
				if((idx++) == 0) {
					stageNode.publish("ipa", phonexNode, phonexNode.getInputFieldWithKey("ipa"));
				} else {
					try {
						final OpLink link = new OpLink(lastStageNode, lastStageOutput, 
								phonexNode, phonexNode.getInputFieldWithKey("ipa"));
						stageGraph.add(link);
					} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e) {
						LOGGER.error( e.getLocalizedMessage(), e);
					}
				}
				
				lastStageNode = phonexNode;
				lastStageOutput = phonexNode.getOutputFieldWithKey("ipa out");
			}
			
			stageNode.publish("ipa", lastStageNode, lastStageOutput);
			
			layoutManager.layoutGraph(stageGraph);
			graph.add(stageNode);
			try {
				final OpLink stageLink = new OpLink(lastNode, lastOutput, stageNode, stageNode.getInputFieldWithKey("ipa"));
				graph.add(stageLink);
				lastNode = stageNode;
				lastOutput = stageNode.getOutputFieldWithKey("ipa");
			} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		layoutManager.layoutGraph(graph);
		return graph;
	}
	
}

