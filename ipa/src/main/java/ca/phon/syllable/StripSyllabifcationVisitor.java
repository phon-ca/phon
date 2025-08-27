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
package ca.phon.syllable;

import ca.phon.ipa.*;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

/**
 * Visitor to strip syllabification information from an
 * {@link IPATranscript}. The resulting transcript will have
 * all phones and compound phones set to {@link SyllableConstituentType#UNKNOWN}.
 *
 * Usage:
 * <pre>
 *     StripSyllabifcationVisitor visitor = new StripSyllabifcationVisitor();
 *     ipaTranscript.accept(visitor);
 *     IPATranscript noSyllabification = visitor.getTranscript();
 * </pre>
 */
public class StripSyllabifcationVisitor extends VisitorAdapter<IPAElement> {

    final IPATranscriptBuilder builder = new IPATranscriptBuilder();

    final IPAElementFactory factory = new IPAElementFactory();

	@Override
	public void fallbackVisit(IPAElement obj) {
        builder.append(obj);
	}
	
	@Visits
	public void visitPhone(Phone p) {
        builder.append(factory.clonePhoneWithScType(p, SyllableConstituentType.UNKNOWN));
	}
	
	@Visits
	public void visitCompoundPhone(CompoundPhone cp) {
		builder.append(factory.clonePhoneWithScType(cp, SyllableConstituentType.UNKNOWN));
	}

    public IPATranscript getTranscript() {
        return builder.toIPATranscript();
    }

}
