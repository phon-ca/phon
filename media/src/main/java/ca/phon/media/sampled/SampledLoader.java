package ca.phon.media.sampled;

import java.io.File;
import java.io.IOException;

import ca.phon.plugin.PluginManager;

/**
 * 
 */
public interface SampledLoader {
	
	/**
	 * Return the default sampled loader
	 */
	public static SampledLoader newLoader() {
		PluginManager pluginManager = PluginManager.getInstance();
		for(var extPt:pluginManager.getExtensionPoints(SampledLoader.class)) {
			var loader = extPt.getFactory().createObject();
			if(loader != null) {
				return loader;
			}
		}
		return new DefaultSampledLoader();
	}

	public boolean canLoadFile(File file);
	
	public Sampled loadSampledFromFile(File file) throws IOException;
	
}
