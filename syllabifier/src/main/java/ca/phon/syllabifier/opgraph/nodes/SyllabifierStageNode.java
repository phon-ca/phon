package ca.phon.syllabifier.opgraph.nodes;

import ca.gedge.opgraph.InputField;
import ca.gedge.opgraph.OperableContext;
import ca.gedge.opgraph.OperableGraph;
import ca.gedge.opgraph.OperableVertexInfo;
import ca.gedge.opgraph.OutputField;
import ca.gedge.opgraph.ProcessingContext;
import ca.gedge.opgraph.exceptions.ProcessingException;
import ca.gedge.opgraph.nodes.general.MacroNode;
import ca.phon.ipa.IPATranscript;

/**
 * A macro node for syllabifier stages.  Includes
 * a pass-through ipa input and an ipa source
 * node.
 *
 */
@OperableVertexInfo(
		name="Syllabifier Stage",
		description="Syllabifier macro with ipa pass-through.",
		category="Syllabifier")
public class SyllabifierStageNode extends MacroNode {
	// pass-through ipa
	private final InputField ipaIn = 
			new InputField("ipa", "ipa transcript", IPATranscript.class);
	private final OutputField ipaOut = 
			new OutputField("ipa", "ipa transcript output", true, IPATranscript.class);
	
	/**
	 * Constructor
	 */
	public SyllabifierStageNode() {
		super();
		init();
	}
	
	public SyllabifierStageNode(OperableGraph graph) {
		super(graph);
		init();
	}
	
	private void init() {
		putField(ipaIn);
		putField(ipaOut);
		
//		getGraph().add(new IPASourceNode());
	}

	@Override
	public void operate(OperableContext context) throws ProcessingException {
		if(graph != null) {
			// First set up processor
			final ProcessingContext processor = new ProcessingContext(graph);
			processor.reset(context);
			
			context.put("__ipa__", context.get(ipaIn));
			
			// The reset call above could clear out the context, so map after
			mapInputs(context);
			
			// Now run the graph
			processor.stepAll();
			if(processor.getError() != null)
				throw processor.getError();
			
			context.put(ipaOut, context.get(ipaIn));
			
			// Map the published outputs from the child vertices back into context
			mapOutputs(context);
		}
	}
	
}
