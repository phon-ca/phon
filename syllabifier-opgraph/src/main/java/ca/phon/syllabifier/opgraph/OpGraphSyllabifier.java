/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ca.gedge.opgraph.OpContext;
import ca.gedge.opgraph.OpGraph;
import ca.gedge.opgraph.Processor;
import ca.gedge.opgraph.io.OpGraphSerializer;
import ca.gedge.opgraph.io.OpGraphSerializerFactory;
import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllabifier.opgraph.extensions.SyllabifierSettings;
import ca.phon.util.Language;

/**
 * Syllabifier implementation using operable graphs.
 */
public final class OpGraphSyllabifier implements Syllabifier {
	
	public final static String IPA_CONTEXT_KEY = "_ipa";
	
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
		ctx.put("__ipa__", transcript);
		
		processor.stepAll();
	}

}
