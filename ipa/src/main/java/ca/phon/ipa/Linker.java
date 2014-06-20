package ca.phon.ipa;

public class Linker extends Sandhi {
	
	public final static Character LINKER_CHAR = '\u2040';

	Linker() {}
	
	@Override
	public String getText() {
		return new StringBuilder().append(LINKER_CHAR).toString();
	}

}
