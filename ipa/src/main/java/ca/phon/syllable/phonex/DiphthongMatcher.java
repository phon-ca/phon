package ca.phon.syllable.phonex;

import ca.phon.ipa.IPAElement;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;

/**
 * Tests nucleus membership in diphthongs.
 */
public class DiphthongMatcher implements PhoneMatcher {

	private boolean isDiphthong;
	
	public DiphthongMatcher() {
		super();
	}
	
	public DiphthongMatcher(boolean diphthong) {
		super();
		this.isDiphthong = diphthong;
	}

	@Override
	public boolean matches(IPAElement p) {
		final SyllabificationInfo info = p.getExtension(SyllabificationInfo.class);
		if(info == null) return false;
		
		return info.getConstituentType() == SyllableConstituentType.NUCLEUS && info.isDiphthongMember();
	}

	@Override
	public boolean matchesAnything() {
		return false;
	}
	
}
