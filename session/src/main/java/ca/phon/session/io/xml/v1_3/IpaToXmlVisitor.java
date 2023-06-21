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
	
	private XmlPhoneticTranscriptionType pho;

	private XmlPhoneticWord currentWord;

	private int currentIndex = 0;
	
	public IpaToXmlVisitor() {
		this.pho = factory.createXmlPhoneticTranscriptionType();
		this.currentWord = factory.createXmlPhoneticWord();
	}

	@Visits
	public void visitPhone(Phone phone) {
		final XmlPhoneType phoneType = factory.createXmlPhoneType();
		phoneType.setContent(phone.getText());
		if(phone.getScType() != SyllableConstituentType.UNKNOWN) {
			final XmlSyllableConstituentType scType = switch (phone.getScType()) {
				case AMBISYLLABIC -> XmlSyllableConstituentType.AMBISYLLABIC;
				case CODA -> XmlSyllableConstituentType.CODA;
				case LEFTAPPENDIX -> XmlSyllableConstituentType.LEFT_APPENDIX;
				case NUCLEUS -> XmlSyllableConstituentType.NUCLEUS;
				case OEHS -> XmlSyllableConstituentType.OEHS;
				case ONSET -> XmlSyllableConstituentType.ONSET;
				case RIGHTAPPENDIX -> XmlSyllableConstituentType.RIGHT_APPENDIX;
				case UNKNOWN, WORDBOUNDARYMARKER, SYLLABLESTRESSMARKER, SYLLABLEBOUNDARYMARKER -> null;
			};
			phoneType.setScType(scType);
		}
		this.currentWord.getStressOrPhOrPp().add(phoneType);
		if(phone.getToneNumberDiacritics().length > 0) {
			final XmlToneNumberType toneNumberType = factory.createXmlToneNumberType();
			// TODO
		}
	}

	@Visits
	public void visitStress(StressMarker stressMarker) {
		final XmlStressType stressType = factory.createXmlStressType();
		final XmlStressTypeType stt = switch (stressMarker.getType()) {
			case PRIMARY -> XmlStressTypeType.PRIMARY;
			case SECONDARY -> XmlStressTypeType.SECONDARY;
		};
		stressType.setType(stt);
		this.currentWord.getStressOrPhOrPp().add(stressType);
	}

	@Visits
	public void visitIntraWordPause(IntraWordPause pause) {
		final XmlPhoneticProsodyType pp = factory.createXmlPhoneticProsodyType();
		if(this.currentWord.getStressOrPhOrPp().size() == 0)
			pp.setType(XmlPhoneticProsodyTypeType.BLOCKING.BLOCKING);
		else
			pp.setType(XmlPhoneticProsodyTypeType.PAUSE.PAUSE);
		this.currentWord.getStressOrPhOrPp().add(pp);
	}

	@Visits
	public void visitContraction(Contraction sandhi) {
		final XmlSandhiType sandhiType = factory.createXmlSandhiType();
		sandhiType.setType(XmlSandhiTypeType.CONTRACTION);
		this.currentWord.getStressOrPhOrPp().add(sandhiType);
	}

	@Visits
	public void visitLinker(Linker linker) {
		final XmlSandhiType sandhiType = factory.createXmlSandhiType();
		sandhiType.setType(XmlSandhiTypeType.LINKER);
		this.currentWord.getStressOrPhOrPp().add(sandhiType);
	}

	@Visits
	public void visitIntonationGroup(IntonationGroup ig) {
		final XmlPhoneticProsodyType pp = factory.createXmlPhoneticProsodyType();
		final XmlPhoneticProsodyTypeType ptt = switch (ig.getType()) {
			case MAJOR -> XmlPhoneticProsodyTypeType.MAJOR_INTONATION_GROUP;
			case MINOR -> XmlPhoneticProsodyTypeType.MINOR_INTONATION_GROUP;
		};
		this.currentWord.getStressOrPhOrPp().add(pp);
	}

	@Visits
	public void visitSyllableBoundary(SyllableBoundary sb) {
		final XmlPhoneticProsodyType pp = factory.createXmlPhoneticProsodyType();
		pp.setType(XmlPhoneticProsodyTypeType.SYLLABLE_BREAK);
		this.currentWord.getStressOrPhOrPp().add(pp);
	}

	@Visits
	public void visitPause(Pause pause) {
		final XmlPauseType p = factory.createXmlPauseType();
		final XmlPauseSymbolicLengthType type = switch(pause.getLength()) {
			case SIMPLE -> XmlPauseSymbolicLengthType.SIMPLE;
			case LONG -> XmlPauseSymbolicLengthType.LONG;
			case VERY_LONG -> XmlPauseSymbolicLengthType.VERY_LONG;
		};
		p.setSymbolicLength(type);
		if(this.currentWord.getStressOrPhOrPp().size() > 0) {
			this.pho.getPwOrPause().add(this.currentWord);
			this.currentWord = factory.createXmlPhoneticWord();
		}
		this.pho.getPwOrPause().add(p);
	}

	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		if(this.currentWord.getStressOrPhOrPp().size() > 0) {
			this.pho.getPwOrPause().add(this.currentWord);
			this.currentWord = factory.createXmlPhoneticWord();
		}
	}

	@Override
	public void fallbackVisit(IPAElement obj) {
	}

	public XmlPhoneticTranscriptionType getPho() {
		if(this.currentWord.getStressOrPhOrPp().size() > 0) {
			this.pho.getPwOrPause().add(this.currentWord);
		}
		return this.pho;
	}

}
