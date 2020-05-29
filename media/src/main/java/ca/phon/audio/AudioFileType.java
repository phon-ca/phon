package ca.phon.audio;

/**
 * Audio file types
 *
 */
public enum AudioFileType {
	AIFF(".aiff", ".aif"),
	AIFC(".aifc", ".aic"),
	WAV(".wav");
	
	String[] extensions;
	
	private AudioFileType(String ... exts) {
		this.extensions = exts;
	}
	
	public String[] getExtensions() {
		return this.extensions;
	}
	
}
