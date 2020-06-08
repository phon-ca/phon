package ca.phon.media;

import java.io.File;
import java.io.IOException;

import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.Rank;

@PhonPlugin(name = "LongSound" )
@Rank(10)
public class SampledLongSoundExtPt implements IPluginExtensionPoint<LongSound> {

	@Override
	public Class<?> getExtensionType() {
		return LongSound.class;
	}

	@Override
	public IPluginExtensionFactory<LongSound> getFactory() {
		return (args) -> {
			try {
				return new SampledLongSound((File)args[0]);
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		};
	}

}
