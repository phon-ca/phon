/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.syllable.phonex;

import ca.phon.ipa.IPAElement;
import ca.phon.phonex.PhoneMatcher;
import ca.phon.syllable.*;

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
