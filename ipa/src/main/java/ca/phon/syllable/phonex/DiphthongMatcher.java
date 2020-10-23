/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.syllable.phonex;

import ca.phon.ipa.*;
import ca.phon.phonex.*;
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
