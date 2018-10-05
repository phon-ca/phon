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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.opgraph.OpContext;
import ca.phon.opgraph.OpGraph;
import ca.phon.opgraph.Processor;
import ca.phon.opgraph.io.OpGraphSerializer;
import ca.phon.opgraph.io.OpGraphSerializerFactory;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.util.Language;

/**
 * Syllabifier implementation using operable graphs.
 */
public final class OpGraphSyllabifier implements Syllabifier {
	
	public final static String IPA_CONTEXT_KEY = "__ipa";
	
	/**
	 * graph
	 */
	private final OpGraph graph;
	
	/**
	 * Create a new syllabifier from the given stream.
	 * 
	 * @param stream
	 */
	public static OpGraphSyllabifier createSyllabifier(InputStream is)
		throws IOException {
		// Get the serializer
		final OpGraphSerializer serializer = OpGraphSerializerFactory.getDefaultSerializer();
		if(serializer == null) {
			throw new IOException("No default serializer available");
		}
		final OpGraph graph = serializer.read(is);
		return new OpGraphSyllabifier(graph);
	}
	
	/**
	 * Constructor
	 */
	public OpGraphSyllabifier(OpGraph graph) {
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
	
	public OpGraph getGraph() {
		return this.graph;
	}

	@Override
	public Language getLanguage() {
		Language retVal = new Language();
		
		final SyllabifierSettings graphSettings = graph.getExtension(SyllabifierSettings.class);
		if(graphSettings != null) {
			final String langId = graphSettings.getLanguage().toString();
			if(langId != null) {
				retVal = Language.parseLanguage(langId);
			}
		}
		
		return retVal;
	}

	@Override
	public void syllabify(List<IPAElement> phones) {
		final Processor processor = new Processor(graph);
		final OpContext ctx = processor.getContext();
		final IPATranscript transcript = new IPATranscript(phones);
		ctx.put(IPA_CONTEXT_KEY, transcript);
		
		processor.stepAll();
	}

	@Override
	public String toString() {
		return getName() + " (" + getLanguage().toString() + ")";
	}
	
}
