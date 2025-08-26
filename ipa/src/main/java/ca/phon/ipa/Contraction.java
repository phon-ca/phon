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
package ca.phon.ipa;

import ca.phon.ipa.features.FeatureSet;

/**
 * An IPAElement that represents a contraction in an IPA transcription.
 *
 */
public class Contraction extends Sandhi {

    private final static char CONTRACTION_CHAR = '\u203f';

	public Contraction() {
        this(null, null);
    }

    public Contraction(FeatureSet overrideFeatureSet, SyllableInfo syllableInfo) {
        super(overrideFeatureSet, syllableInfo != null ? syllableInfo :
                new SyllableInfo(SyllableConstituentType.SYLLABLEBOUNDARYMARKER));
    }

	@Override
	public String getText() {
        return Character.toString(CONTRACTION_CHAR);
	}

}
