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
package ca.phon.syllabifier.basic;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ca.phon.ipa.IPAElement;
import ca.phon.ipa.IPATranscript;
import ca.phon.phonex.PhonexMatcher;
import ca.phon.phonex.PhonexPattern;
import ca.phon.syllabifier.basic.io.SonorityValues;
import ca.phon.syllabifier.basic.io.SonorityValues.SonorityClass;
import ca.phon.syllabifier.phonex.SonorityInfo;

public class SonorityScale implements SyllabifierStage {

	private final SonorityValues sonorityValues;
	
	private final Map<Integer, List<PhonexPattern>> sonorityPatterns = 
			new LinkedHashMap<Integer, List<PhonexPattern>>();
	
	public SonorityScale(SonorityValues values) {
		super();
		this.sonorityValues = values;
		
		compile();
	}
	
	private void compile() {
		for(SonorityClass sc:sonorityValues.getSonorityClass()) {
			final List<PhonexPattern> patterns = new ArrayList<PhonexPattern>();
			for(String phonex:sc.getPhonex()) {
				final PhonexPattern pattern = PhonexPattern.compile(phonex);
				patterns.add(pattern);
			}
			sonorityPatterns.put(sc.getSonorityValue(), patterns);
		}
	}

	@Override
	public boolean run(List<IPAElement> phones) {
		Integer lastVal = null;
		for(IPAElement ele:phones) {
			for(int sval:sonorityPatterns.keySet()) {
				boolean handeled = false;
				for(PhonexPattern p:sonorityPatterns.get(sval)) {
					final PhonexMatcher m = p.matcher(new IPATranscript(ele));
					if(m.matches()) {
						final SonorityInfo sInfo = new SonorityInfo(sval, 
								(lastVal == null ? 0 : sval - lastVal));
						ele.putExtension(SonorityInfo.class, sInfo);
						lastVal = sval;
						handeled = true;
						break;
					}
				}
				if(handeled) break;
			}
		}
		// always return false in this stage
		return false;
	}

	@Override
	public boolean repeatWhileChanges() {
		return false;
	}

	@Override
	public String getName() {
		return SonorityScale.class.getSimpleName();
	}
	
}
