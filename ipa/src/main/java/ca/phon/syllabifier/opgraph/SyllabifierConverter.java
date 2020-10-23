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
package ca.phon.syllabifier.opgraph;

import ca.phon.ipa.*;
import ca.phon.opgraph.*;
import ca.phon.opgraph.app.*;
import ca.phon.opgraph.dag.*;
import ca.phon.opgraph.exceptions.*;
import ca.phon.opgraph.nodes.general.*;
import ca.phon.opgraph.nodes.reflect.*;
import ca.phon.syllabifier.basic.*;
import ca.phon.syllabifier.basic.io.*;
import ca.phon.syllabifier.basic.io.SonorityValues.*;
import ca.phon.syllabifier.opgraph.extensions.*;
import ca.phon.syllabifier.opgraph.nodes.*;
import ca.phon.util.*;

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
		} catch (ItemMissingException | CycleDetectedException | VertexNotFoundException | InvalidEdgeException e) {
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
					} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException | InvalidEdgeException e) {
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
			} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException | InvalidEdgeException e) {
				LOGGER.error( e.getLocalizedMessage(), e);
			}
		}
		
		layoutManager.layoutGraph(graph);
		return graph;
	}
	
}

