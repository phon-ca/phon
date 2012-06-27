package ca.phon.syllabifier.opgraph;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import ca.gedge.opgraph.OperableContext;
import ca.gedge.opgraph.OperableGraph;
import ca.gedge.opgraph.ProcessingContext;
import ca.gedge.opgraph.app.extensions.NodeSettings;
import ca.gedge.opgraph.io.OpgraphSerializer;
import ca.gedge.opgraph.io.OpgraphSerializerFactory;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.util.LanguageEntry;
import ca.phon.util.LanguageParser;

/**
 * Syllabifier implementation using operable graphs.
 */
public final class OpGraphSyllabifier implements Syllabifier {
	/**
	 * graph
	 */
	private final OperableGraph graph;
	
	/**
	 * Create a new syllabifier from the given stream.
	 * 
	 * @param stream
	 */
	public static OpGraphSyllabifier createSyllabifier(InputStream is)
		throws IOException {
		// Get the serializer
		final OpgraphSerializer serializer = OpgraphSerializerFactory.getDefaultSerializer();
		if(serializer == null) {
			throw new IOException("No default serializer available");
		}
		final OperableGraph graph = serializer.read(is);
		return new OpGraphSyllabifier(graph);
	}
	
	/**
	 * Constructor
	 */
	public OpGraphSyllabifier(OperableGraph graph) {
		this.graph = graph;
	}

	@Override
	public String getName() {
		String retVal = "";
		
		final SyllabifierSettings graphSettings = graph.getExtension(SyllabifierSettings.class);
		if(graphSettings != null) {
			retVal = graphSettings.getName();
		}
		
		return retVal;
	}

	@Override
	public LanguageEntry getLanguage() {
		LanguageEntry retVal = new LanguageEntry();
		
		final SyllabifierSettings graphSettings = graph.getExtension(SyllabifierSettings.class);
		if(graphSettings != null) {
			final String langId = graphSettings.getLanguage();
			if(langId != null) {
				retVal = LanguageParser.getInstance().getEntryById(langId);
			}
		}
		
		return retVal;
	}

	@Override
	public void syllabify(List<IPAElement> phones) {
		final ProcessingContext processor = new ProcessingContext(graph);
		final OperableContext ctx = processor.getContext();
		final IPATranscript transcript = new IPATranscript(phones);
		ctx.put("__ipa__", transcript);
		
		processor.stepAll();
	}

}
