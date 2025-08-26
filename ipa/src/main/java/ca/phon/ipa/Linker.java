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
 * An IPAElement that represents a linker in an IPA transcription.  This is a character that
 * ties syllables together.
 */
public class Linker extends Sandhi {
	
	public final static Character LINKER_CHAR = '\u2040';

	Linker() {
        this(null, null);
    }

    public Linker(FeatureSet overrideFeatureSet, SyllableInfo syllableInfo) {
        super(overrideFeatureSet, syllableInfo != null ? syllableInfo : new SyllableInfo(SyllableConstituentType.UNKNOWN));
    }
	
	@Override
	public String getText() {
		return Character.toString(LINKER_CHAR);
	}

}
