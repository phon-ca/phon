/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.syllabifier.basic;

import java.util.*;

import ca.phon.ipa.*;
import ca.phon.phonex.*;
import ca.phon.syllabifier.basic.io.*;
import ca.phon.syllabifier.basic.io.SonorityValues.*;
import ca.phon.syllabifier.phonex.*;

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
