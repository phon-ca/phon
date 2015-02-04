package ca.phon.orthography;

/**
 * Two words joined by a marker.
 */
public class OrthoWordnet extends AbstractOrthoElement {
	
	private final OrthoWord word1;
	
	private final OrthoWordnetMarker marker;
	
	private final OrthoWord word2;
	
	public OrthoWordnet(OrthoWord word1, OrthoWord word2) {
		this(word1, word2, OrthoWordnetMarker.COMPOUND);
	}
	
	public OrthoWordnet(OrthoWord word1, OrthoWord word2, OrthoWordnetMarker marker) {
		super();
		this.word1 = word1;
		this.word2 = word2;
		this.marker = marker;
	}
	
	public OrthoWord getWord1() {
		return word1;
	}

	public OrthoWordnetMarker getMarker() {
		return marker;
	}

	public OrthoWord getWord2() {
		return word2;
	}

	@Override
	public String text() {
		return word1.toString() + marker.getMarker() + word2.toString();
	}
	
	@Override
	public String toString() {
		return text();
	}

}
