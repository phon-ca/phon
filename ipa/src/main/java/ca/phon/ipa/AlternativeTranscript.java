package ca.phon.ipa;

import java.util.HashMap;

import ca.phon.extensions.Extension;

/**
 * Extension for alternative forms for an IPATranscript.
 * 
 * This is most often used for mutl-blind transcription methods.
 */
@Extension(IPATranscript.class)
public class AlternativeTranscript extends HashMap<String, IPATranscript> {
	
	private static final long serialVersionUID = -9179068664990132970L;
	
	private String selected = null;

	public AlternativeTranscript() {
		super();
	}
	
	/**
	 * Set selected transcriber key
	 * 
	 * @param selected
	 */
	public void setSelected(String selected) {
		this.selected = selected;
	}
	
	/**
	 * Get selected transcriber key
	 * 
	 * @return selected transcriber or <code>null</code>
	 */
	public String getSelected() {
		return this.selected;
	}
	
}
