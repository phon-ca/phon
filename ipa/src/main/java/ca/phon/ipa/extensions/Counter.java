package ca.phon.ipa.extensions;

import java.lang.ref.WeakReference;

import ca.phon.extensions.Extension;
import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;

@Extension(IPATranscript.class)
public class Counter {

	private final WeakReference<IPATranscript> transcriptRef;
	
	public Counter(IPATranscript transcript) {
		this.transcriptRef = new WeakReference<IPATranscript>(transcript);
	}
	
	public int getNumConsonants() {
		int retVal = -1;
		final IPATranscript transcript = transcriptRef.get();
		if(transcript == null) return retVal;
		
		final PhonexPattern pattern = PhonexPattern.compile("\\c");
		final PhonexMatcher matcher = pattern.matcher(transcript);
		retVal = 0;
		while(matcher.find()) {
			retVal++;
		}
		
		return retVal;
	}
	
}
