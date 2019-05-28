package ca.phon.media;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ca.phon.media.sampled.SampledLongSound;
import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;

/**
 * LongSound objects allow for access to audio stream information
 * without loading the entire audio file into memory.  Segments may
 * be extracted from the LongSound for analysis.
 * 
 */
public abstract class LongSound {

	private final File file;
	
	public static LongSound fromFile(File file) throws IOException {
		final List<IPluginExtensionPoint<LongSound>> soundLoaders =
				PluginManager.getInstance().getExtensionPoints(LongSound.class);
		if(soundLoaders.size() > 0) {
			IPluginExtensionPoint<LongSound> defaultLoader = soundLoaders.get(0);
			try {
				return defaultLoader.getFactory().createObject(file);
			} catch (Exception e) {
				throw new IOException(e);
			}
		} else {
			return new SampledLongSound(file);
		}
	}
	
	protected LongSound(File file) {
		super();
		
		this.file = file;
	}
	
	public File getFile() {
		return this.file;
	}
	
	/**
	 * Number of channels in audio file.
	 * @return number of channels
	 */
	public abstract int numberOfChannels();
	
	/**
	 * Length of audio in seconds
	 * 
	 * @return
	 */
	public abstract float length();
	
	/**
	 * Extract portion of sound for analysis.
	 * 
	 * @param startTime
	 * @param endTime
	 * 
	 * @return sound segment
	 */
	public abstract Sound extractPart(float startTime, float endTime);
	
}
