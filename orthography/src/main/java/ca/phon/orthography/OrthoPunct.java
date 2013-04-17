package ca.phon.orthography;


/**
 * Represents punctuation and syntax elements.
 */
public class OrthoPunct implements OrthoElement {
	
	private final OrthoPunctType type;

	public OrthoPunct(OrthoPunctType type) {
		super();
		this.type = type;
	}
	
	public OrthoPunctType getType() {
		return this.type;
	}
	
	@Override
	public String text() {
		return this.type.getChar() + "";
	}

}
