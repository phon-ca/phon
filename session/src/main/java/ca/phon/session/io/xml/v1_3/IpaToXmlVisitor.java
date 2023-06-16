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
package ca.phon.session.io.xml.v1_3;

import ca.phon.ipa.*;
import ca.phon.ipa.Linker;
import ca.phon.ipa.Pause;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;

import java.math.BigInteger;

/**
 * 
 */
public class IpaToXmlVisitor extends VisitorAdapter<IPAElement> {
	
	private final ObjectFactory factory = new ObjectFactory();
	
	private PhoneticTranscriptionType pho;

	private PhoneticWord currentWord;

	private int currentIndex = 0;
	
	public IpaToXmlVisitor() {
		this.pho = factory.createPhoneticTranscriptionType();
		this.currentWord = factory.createPhoneticWord();
	}

	@Visits
	public void visitPhone(Phone phone) {
		final PhoneType phoneType = factory.createPhoneType();
		phoneType.setContent(phone.getText());
		if(phone.getScType() != SyllableConstituentType.UNKNOWN) {
			final ca.phon.session.io.xml.v1_3.SyllableConstituentType scType = switch (phone.getScType()) {
				case AMBISYLLABIC -> ca.phon.session.io.xml.v1_3.SyllableConstituentType.AMBISYLLABIC;
				case CODA -> ca.phon.session.io.xml.v1_3.SyllableConstituentType.CODA;
				case LEFTAPPENDIX -> ca.phon.session.io.xml.v1_3.SyllableConstituentType.LEFT_APPENDIX;
				case NUCLEUS -> ca.phon.session.io.xml.v1_3.SyllableConstituentType.NUCLEUS;
				case OEHS -> ca.phon.session.io.xml.v1_3.SyllableConstituentType.OEHS;
				case ONSET -> ca.phon.session.io.xml.v1_3.SyllableConstituentType.ONSET;
				case RIGHTAPPENDIX -> ca.phon.session.io.xml.v1_3.SyllableConstituentType.RIGHT_APPENDIX;
				case UNKNOWN, WORDBOUNDARYMARKER, SYLLABLESTRESSMARKER, SYLLABLEBOUNDARYMARKER -> null;
			};
			phoneType.setScType(scType);
		}
		this.currentWord.getStressOrPhOrPp().add(phoneType);
		if(phone.getToneNumberDiacritics().length > 0) {
			final ToneNumberType toneNumberType = factory.createToneNumberType();
			// TODO
		}
	}

	@Visits
	public void visitStress(StressMarker stressMarker) {
		final StressType stressType = factory.createStressType();
		final StressTypeType stt = switch (stressMarker.getType()) {
			case PRIMARY -> StressTypeType.PRIMARY;
			case SECONDARY -> StressTypeType.SECONDARY;
		};
		stressType.setType(stt);
		this.currentWord.getStressOrPhOrPp().add(stressType);
	}

	@Visits
	public void visitIntraWordPause(IntraWordPause pause) {
		final PhoneticProsodyType pp = factory.createPhoneticProsodyType();
		if(this.currentWord.getStressOrPhOrPp().size() == 0)
			pp.setType(PhoneticProsodyTypeType.BLOCKING);
		else
			pp.setType(PhoneticProsodyTypeType.PAUSE);
		this.currentWord.getStressOrPhOrPp().add(pp);
	}

	@Visits
	public void visitContraction(Contraction sandhi) {
		final SandhiType sandhiType = factory.createSandhiType();
		sandhiType.setType(SandhiTypeType.CONTRACTION);
		this.currentWord.getStressOrPhOrPp().add(sandhiType);
	}

	@Visits
	public void visitLinker(Linker linker) {
		final SandhiType sandhiType = factory.createSandhiType();
		sandhiType.setType(SandhiTypeType.LINKER);
		this.currentWord.getStressOrPhOrPp().add(sandhiType);
	}

	@Visits
	public void visitIntonationGroup(IntonationGroup ig) {
		final PhoneticProsodyType pp = factory.createPhoneticProsodyType();
		final PhoneticProsodyTypeType ptt = switch (ig.getType()) {
			case MAJOR -> PhoneticProsodyTypeType.MAJOR_INTONATION_GROUP;
			case MINOR -> PhoneticProsodyTypeType.MINOR_INTONATION_GROUP;
		};
		this.currentWord.getStressOrPhOrPp().add(pp);
	}

	@Visits
	public void visitSyllableBoundary(SyllableBoundary sb) {
		final PhoneticProsodyType pp = factory.createPhoneticProsodyType();
		pp.setType(PhoneticProsodyTypeType.SYLLABLE_BREAK);
		this.currentWord.getStressOrPhOrPp().add(pp);
	}

	@Visits
	public void visitPause(Pause pause) {
		final ca.phon.session.io.xml.v1_3.Pause p = factory.createPause();
		final PauseSymbolicLengthType type = switch(pause.getLength()) {
			case SIMPLE -> PauseSymbolicLengthType.SIMPLE;
			case LONG -> PauseSymbolicLengthType.LONG;
			case VERY_LONG -> PauseSymbolicLengthType.VERY_LONG;
		};
		p.setSymbolicLength(type);
		if(this.currentWord.getStressOrPhOrPp().size() > 0) {
			this.pho.getPwOrPause().add(this.currentWord);
			this.currentWord = factory.createPhoneticWord();
		}
		this.pho.getPwOrPause().add(p);
	}

	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		if(this.currentWord.getStressOrPhOrPp().size() > 0) {
			this.pho.getPwOrPause().add(this.currentWord);
			this.currentWord = factory.createPhoneticWord();
		}
	}

	@Override
	public void fallbackVisit(IPAElement obj) {
	}

	public PhoneticTranscriptionType getPho() {
		return this.pho;
	}

}
