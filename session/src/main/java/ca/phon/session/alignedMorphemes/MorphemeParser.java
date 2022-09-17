/*
 * Copyright (C) 2005-2022 Gregory Hedlund & Yvan Rose
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

package ca.phon.session.alignedMorphemes;

import ca.phon.ipa.IPATranscript;
import ca.phon.orthography.OrthoElement;
import ca.phon.session.TierString;

/**
 * This class is responsible for turning tier data for various Phon tiers into a list of
 * morphemes.
 *
 * E.g., The following is an example which shows values from three tiers (identified by letter) and the corresponding
 * alignments. Tokens are in the form A1, B1, etc., but would be the values from our dictionary in practice. '~' or '+'
 * indicates a morpheme boundary within a word; spaces are word boundaries; and '*' is used as a filler within the tier
 * text.
 *
 * <pre>
 Tier 1: A1 A2~A3 A4+A5~A6

 Tier 2: B1 B2 B3+*~B4~B5

 Tier 3: * *~C1 C2+C3~C4

 # of aligned morpheme sets: 7 (determined by tier with most morphemes)
 Alignments:
	 1) A1, B1, *,
	 2) A2, B2, *
	 3) A3, *, C1
	 4) A4, B3, C2
	 5) A5, *, C3
	 6) A6, B4, C4
     7) *, B5, *
 * </pre>
 */
public class MorphemeParser {

	/**
	 * Parse the given orthography into a list of morphemes.
	 *
	 * @param orthoEle
	 * @return
	 */
	public OrthoElement[] parseOrthography(OrthoElement orthoEle) {
		final OrthographyMorphemeVisitor visitor = new OrthographyMorphemeVisitor();
		visitor.visit(orthoEle);
		return visitor.getMorphemes();
	}

	public IPATranscript[] parseIPA(IPATranscript ipa) {
		final IPAMorphemeVisitor visitor = new IPAMorphemeVisitor();
		ipa.accept(visitor);
		return visitor.getMorphemes();
	}

	public TierString[] parseTier(TierString tierString) {
		String[] tokens = tierString.toString().split("[ ~+]");
		TierString[] retVal = new TierString[tokens.length];
		for(int i = 0; i < tokens.length; i++) {
			retVal[i] = new TierString(tokens[i]);
		}
		return retVal;
	}

}
