package ca.phon.syllabifier.opgraph;

import java.util.logging.Level;
import java.util.logging.Logger;

import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.OpLink;
import ca.gedge.opgraph.OpNode;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.app.edits.graph.AutoLayoutEdit;
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
		
		// setup settings
		final Language lang = syllabifier.getLanguage();
		final SyllabifierSettings settings = new SyllabifierSettings();
		settings.setLanguage(lang);
		settings.setName(syllabifier.getName());
		graph.putExtension(SyllabifierSettings.class, settings);
		
		// add IPASourceNode
		final IPASourceNode sourceNode = new IPASourceNode();
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
			final OpLink link = new OpLink(sourceNode, sourceNode.getOutputFieldWithKey("ipa"),
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
			
			final ObjectNode stageSourceNode = new ObjectNode(IPATranscript.class);
			stageSourceNode.setName("IPA Transcript");
			stageGraph.add(stageSourceNode);
			stageNode.publish("ipa", stageSourceNode, stageSourceNode.getInputFieldWithKey("obj"));
			OpNode lastStageNode = stageSourceNode;
			OutputField lastStageOutput = stageSourceNode.getOutputFieldWithKey("obj");
			
			// create a new PhonexFind node for each expression
			for(String phonex:st.getPhonex()) {
				final PhonexPattern pattern = PhonexPattern.compile(phonex);
				
				final PhonexFindNode phonexNode = new PhonexFindNode();
				phonexNode.setName(phonex);
				phonexNode.setPhonex(phonex);
				
				final OpGraph phonexGraph = phonexNode.getGraph();
				for(MarkGroup mg:st.getGroup()) {
					final String groupName = mg.getName();
					final ConstituentType markType = mg.getMark();
					final SyllableConstituentType markAs = SyllableConstituentType.fromString(markType.name());
					
					if(pattern.groupIndex(groupName) > 0) {
						// find group node in graph
						final OpNode phonexGroupNode = phonexGraph.getNodesByName(groupName).get(0);
						if(phonexGroupNode != null) {
							final IterableClassNode itrNode = new IterableClassNode(IPATranscript.class);
							final OpGraph itrGraph = itrNode.getGraph();
							phonexNode.getGraph().add(itrNode);
							
							final OpNode eleNode = itrGraph.getNodesByName("IPAElement").get(0);
							if(eleNode != null) {
								// add static field and attach to correct input
								final StaticFieldNode staticFieldNode = new StaticFieldNode();
								staticFieldNode.setDeclaredClass(SyllableConstituentType.class);
								try {
									staticFieldNode.setClassMember(SyllableConstituentType.class.getField(markAs.toString()));
								} catch (NoSuchFieldException | SecurityException e) {
									LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
								}
								
								itrGraph.add(staticFieldNode);
								// add link
								try {
									final OpLink itrLink = new OpLink(staticFieldNode, "value", eleNode, "scType");
									itrGraph.add(itrLink);
								} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e) {
									LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
								}
								
							}
							
							// link group to itrNode
							try {
								final OpLink itrLink = new OpLink(phonexGroupNode, "ipa", 
										itrNode, "obj");
								phonexGraph.add(itrLink);
							} catch (CycleDetectedException | VertexNotFoundException | ItemMissingException e) {
								LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
							}
						}
					}
				}
				
				// add stage link
				stageGraph.add(phonexNode);
				try {
					final OpLink stageLink = new OpLink(lastStageNode, lastStageOutput,
							phonexNode, phonexNode.getInputFieldWithKey("ipa"));
					stageGraph.add(stageLink);
					
					lastStageNode = phonexNode;
					lastStageOutput = phonexNode.getOutputFieldWithKey("ipa out");
				} catch (ItemMissingException | VertexNotFoundException | CycleDetectedException e) {
					LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			}
			stageNode.publish("ipa", lastStageNode, lastStageOutput);
			
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
		
		
		
		return graph;
	}
	
}

