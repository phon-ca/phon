package ca.phon.ipa.extensions;

import ca.phon.extensions.Extension;
import ca.phon.extensions.ExtensionProvider;
import ca.phon.extensions.IExtendable;
import ca.phon.ipa.IPATranscript;

@Extension(IPATranscript.class)
public class CounterProvider implements ExtensionProvider {

	@Override
	public void installExtension(IExtendable obj) {
		if(obj instanceof IPATranscript) {
			final IPATranscript transcript = IPATranscript.class.cast(obj);
			final Counter counter = new Counter(transcript);
			transcript.putExtension(Counter.class, counter);
		}
	}

}
