package ca.phon.media.sampled;

import java.io.File;
import java.io.IOException;

import ca.phon.audio.Sampled;
import ca.phon.plugin.IPluginExtensionFactory;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PhonPlugin;
import ca.phon.plugin.Rank;

/**
 * Return the default sampled implementation {@link PCMSampled}
 * 
 */
@PhonPlugin(name="phon-media")
@Rank(10)
public class DefaultSampledLoader implements SampledLoader, IPluginExtensionPoint<SampledLoader> {

	@Override
	public Class<?> getExtensionType() {
		return SampledLoader.class;
	}

	@Override
	public IPluginExtensionFactory<SampledLoader> getFactory() {
		return (args) -> this;
	}

	@Override
	public boolean canLoadFile(File file) {
		return file.getName().endsWith(".wav");
	}

	@Override
	public Sampled loadSampledFromFile(File file) throws IOException {
		return new PCMSampled(file);
	}

}
