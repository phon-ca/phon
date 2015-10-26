package ca.phon.syllabifier.opgraph;

import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.AutoLayoutManager;
import ca.gedge.opgraph.dag.CycleDetectedException;
import ca.gedge.opgraph.dag.VertexNotFoundException;
import ca.gedge.opgraph.exceptions.ItemMissingException;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.gedge.opgraph.nodes.iteration.ForEachNode;
import ca.gedge.opgraph.nodes.reflect.IterableClassNode;
import ca.gedge.opgraph.nodes.reflect.ObjectNode;
import ca.gedge.opgraph.nodes.reflect.StaticFieldNode;
import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.editor.actions.OpgraphEditorAction;
import ca.phon.opgraph.nodes.phonex.PhonexFindNode;
import ca.phon.phonex.PhonexPattern;
import ca.phon.syllabifier.basic.BasicSyllabifier;
import ca.phon.syllabifier.basic.SonorityScale;
import ca.phon.syllabifier.basic.SyllabifierStage;
import ca.phon.syllabifier.basic.io.ConstituentType;
import ca.phon.syllabifier.basic.io.MarkGroup;
import ca.phon.syllabifier.basic.io.SonorityValues;
import ca.phon.syllabifier.basic.io.SonorityValues.SonorityClass;
import ca.phon.syllabifier.basic.io.StageType;
import ca.phon.syllabifier.basic.io.SyllabifierDef;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.syllabifier.opgraph.nodes.IPASourceNode;
import ca.phon.syllabifier.opgraph.nodes.MarkConstituentNode;
import ca.phon.syllabifier.opgraph.nodes.SonorityNode;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.Language;

/**
 * Convert a {@link BasicSyllabifier} into a new {@link OpGraphSyllabifier}
 * graph.
 *
 */
public class SyllabifierConverter {
	
	private final static Logger LOGGER = Logger.getLogger(SyllabifierConverter.class.getName());
	
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
			sb.append(sc.getSonorityValue()).append('=').append(sc.getPhonex()).append('\n');
		}
		sonorityNode.setSonorityScale(sb.toString());
		graph.add(sonorityNode);
		
		try {
			final OpLink link = new OpLink(sourceNode, sourceNode.getOutputFieldWithKey("obj"),
					sonorityNode, sonorityNode.getInputFieldWithKey("ipa"));
			graph.add(link);
		} catch (ItemMissingException | CycleDetectedException | VertexNotFoundException e) {
			LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
				
				phonexNode.setName("Phonex #" + (idx+1));
				
				stageGraph.add(phonexNode);
				if((idx++) == 0) {
					stageNode.publish("ipa", phonexNode, phonexNode.getInputFieldWithKey("ipa"));
				} else {
					try {
						final OpLink link = new OpLink(lastStageNode, lastStageOutput, 
								phonexNode, phonexNode.getInputFieldWithKey("ipa"));
						stageGraph.add(link);
					} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e) {
						LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
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
				LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		
		layoutManager.layoutGraph(graph);
		return graph;
	}
	
}

