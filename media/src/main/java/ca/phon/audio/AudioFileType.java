package ca.phon.audio;

import java.util.ArrayList;
import java.util.List;

import ca.phon.plugin.IPluginExtensionPoint;
import ca.phon.plugin.PluginManager;

/**
 * Audio file types
 *
 */
public class AudioFileType {
	final public static AudioFileType AIFF = new AudioFileType("AIFF file", "aiff", "aif");
	final public static AudioFileType AIFC = new AudioFileType("AIFC file", "aifc", "aic");
	final public static AudioFileType WAV = new AudioFileType("WAV file", "wav");
	
	private String name;
	
	private String[] extensions;
	
	protected AudioFileType(String name, String ... exts) {
		this.name = name;
		this.extensions = exts;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String[] getExtensions() {
		return this.extensions;
	}
		
	/**
	 * Return a list of supported file types.
	 * These include AIFF, AIFC, WAV and any other
	 * registered AudioFileType plug-ins
	 */
	public static List<AudioFileType> getSupportedFileTypes() {
		List<AudioFileType> retVal = new ArrayList<>();
		retVal.add(AIFF);
		retVal.add(AIFC);
		retVal.add(WAV);
		
		for(IPluginExtensionPoint<AudioFileType> audioFileTypeExtPt:PluginManager.getInstance().getExtensionPoints(AudioFileType.class)) {
			AudioFileType extendedType = audioFileTypeExtPt.getFactory().createObject();
			retVal.add(extendedType);
		}
		
		return retVal;
	}
	
}
