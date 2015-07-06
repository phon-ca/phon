package ca.phon.ipa;

import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;

@Extension(IPATranscript.class)
public class LevenshteinDistanceProvider implements ExtensionProvider {

	@Override
	public void installExtension(IExtendable obj) {
		if(!(obj instanceof IPATranscript)) 
			throw new IllegalArgumentException(obj + " is not an instance of IPATranscript");
		
		final IPATranscript transcript = (IPATranscript)obj;
		transcript.putExtension(LevenshteinDistance.class, new LevenshteinDistance(transcript));
	}

}
