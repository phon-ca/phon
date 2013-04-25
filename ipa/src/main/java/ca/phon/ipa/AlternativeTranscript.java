package ca.phon.ipa;

import java.util.HashMap;

import ca.phon.extensions.Extension;

/**
 * Extension for alternative forms for an IPATranscript.
 * 
 * This is most often used for mutl-blind transcription methods.
 */
@Extension(IPATranscript.class)
public class AlternativeTranscript extends HashMap<String, IPATranscript> {
	
	private static final long serialVersionUID = -9179068664990132970L;

	public AlternativeTranscript() {
		super();
	}
	
	
}
