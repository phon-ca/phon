package ca.phon.app.session.editor.view.ipa_lookup;

import ca.phon.ipa.IPATranscript;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.orthography.Orthography;

public interface IPALookupPostProcessor {

	/**
	 * Apply rules/transformations to given {@link IPATranscript} object
	 * using given information. This is executed during IPA Lookup/automatic
	 * transcription before the value is assigned to the tier.
	 *
	 * @param dictionary
	 * @param orthography
	 * @param transcript
	 */
	public IPATranscript postProcess(IPADictionary dictionary, String orthography, IPATranscript transcript);

}
