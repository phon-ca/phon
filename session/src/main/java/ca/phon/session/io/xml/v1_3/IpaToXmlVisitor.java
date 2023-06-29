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
import ca.phon.syllable.SyllabificationInfo;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.visitor.VisitorAdapter;
import ca.phon.visitor.annotation.Visits;
import com.ibm.icu.text.ArabicShaping;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
		if(phone.getPrefixDiacritics().length > 0)
			Arrays.stream(phone.getPrefixDiacritics()).map(Diacritic::getText).forEach(phoneType.getPrefix()::add);
		phoneType.setBase(phone.getBase());
		if(phone.getCombiningDiacritics().length > 0)
			Arrays.stream(phone.getCombiningDiacritics()).map(Diacritic::getText).forEach(phoneType.getCombining()::add);
		String phLen = Arrays.stream(phone.getLengthDiacritics()).map(Diacritic::getText).collect(Collectors.joining());
		if(phLen.length() > 0) phoneType.setPhlen(phLen);
		String toneNum = Arrays.stream(phone.getToneNumberDiacritics()).map(Diacritic::getText).collect(Collectors.joining());
		if(toneNum.length() > 0) phoneType.setToneNumber(toneNum);
		final List<Diacritic> filteredSuffixDiacritics =
				Arrays.stream(phone.getSuffixDiacritics()).filter(d -> d.getType() == DiacriticType.SUFFIX).toList();
		if(filteredSuffixDiacritics.size() > 0)
			filteredSuffixDiacritics.stream().map(Diacritic::getText).forEach(phoneType.getSuffix()::add);
		if(phone.getScType() != SyllableConstituentType.UNKNOWN) {
			XmlSyllableConstituentType scType = switch (phone.getScType()) {
				case AMBISYLLABIC -> XmlSyllableConstituentType.AMBISYLLABIC;
				case CODA -> XmlSyllableConstituentType.CODA;
				case LEFTAPPENDIX -> XmlSyllableConstituentType.LEFT_APPENDIX;
				case NUCLEUS -> XmlSyllableConstituentType.NUCLEUS;
				case OEHS -> XmlSyllableConstituentType.OEHS;
				case ONSET -> XmlSyllableConstituentType.ONSET;
				case RIGHTAPPENDIX -> XmlSyllableConstituentType.RIGHT_APPENDIX;
				case UNKNOWN, WORDBOUNDARYMARKER, SYLLABLESTRESSMARKER, SYLLABLEBOUNDARYMARKER -> null;
			};
			if(scType == XmlSyllableConstituentType.NUCLEUS) {
				final SyllabificationInfo info = phone.getExtension(SyllabificationInfo.class);
				if(info.isDiphthongMember())
					scType = XmlSyllableConstituentType.DIPHTHONG;
			}
			phoneType.setScType(scType);
		}
		this.currentWord.getStressOrPhOrCmph().add(phoneType);
	}

	@Visits
	public void visitCompoundPhone(CompoundPhone cmpPhone) {
		final XmlCompoundPhoneType xmlCompoundPhoneType = factory.createXmlCompoundPhoneType();
		visit(cmpPhone.getFirstPhone());
		final Object firstPhoneXmlType = this.currentWord.getStressOrPhOrCmph().remove(this.currentWord.getStressOrPhOrCmph().size()-1);
		visit(cmpPhone.getSecondPhone());
		final Object secondPhoneXmlType = this.currentWord.getStressOrPhOrCmph().remove(this.currentWord.getStressOrPhOrCmph().size()-1);

		if(firstPhoneXmlType instanceof XmlPhoneType) {
			xmlCompoundPhoneType.getContent().add(factory.createPh(((XmlPhoneType) firstPhoneXmlType)));
		} else {
			xmlCompoundPhoneType.getContent().add(factory.createCmph(((XmlCompoundPhoneType) firstPhoneXmlType)));
		}

		XmlLigatureTypeType ligType = switch (cmpPhone.getLigature()) {
			case '\u035c' -> XmlLigatureTypeType.BREVE_BELOW;
			case '\u0362' -> XmlLigatureTypeType.RIGHT_ARROW_BELOW;
			default -> XmlLigatureTypeType.BREVE;
		};
		final XmlLigatureType lig = factory.createXmlLigatureType();
		lig.setType(ligType);
		xmlCompoundPhoneType.getContent().add(factory.createLig(lig));

		xmlCompoundPhoneType.getContent().add(factory.createPh(((XmlPhoneType) secondPhoneXmlType)));
		this.currentWord.getStressOrPhOrCmph().add(xmlCompoundPhoneType);
	}

	@Visits
	public void visitStress(StressMarker stressMarker) {
		final XmlStressType stressType = factory.createXmlStressType();
		final XmlStressTypeType stt = switch (stressMarker.getType()) {
			case PRIMARY -> XmlStressTypeType.PRIMARY;
			case SECONDARY -> XmlStressTypeType.SECONDARY;
		};
		stressType.setType(stt);
		this.currentWord.getStressOrPhOrCmph().add(stressType);
	}

	@Visits
	public void visitIntraWordPause(IntraWordPause pause) {
		final XmlPhoneticProsodyType pp = factory.createXmlPhoneticProsodyType();
		if(this.currentWord.getStressOrPhOrCmph().size() == 0)
			pp.setType(XmlPhoneticProsodyTypeType.BLOCKING.BLOCKING);
		else
			pp.setType(XmlPhoneticProsodyTypeType.PAUSE.PAUSE);
		this.currentWord.getStressOrPhOrCmph().add(pp);
	}

	@Visits
	public void visitContraction(Contraction sandhi) {
		final XmlSandhiType sandhiType = factory.createXmlSandhiType();
		sandhiType.setType(XmlSandhiTypeType.CONTRACTION);
		this.currentWord.getStressOrPhOrCmph().add(sandhiType);
	}

	@Visits
	public void visitLinker(Linker linker) {
		final XmlSandhiType sandhiType = factory.createXmlSandhiType();
		sandhiType.setType(XmlSandhiTypeType.LINKER);
		this.currentWord.getStressOrPhOrCmph().add(sandhiType);
	}

	@Visits
	public void visitIntonationGroup(IntonationGroup ig) {
		final XmlPhoneticProsodyType pp = factory.createXmlPhoneticProsodyType();
		final XmlPhoneticProsodyTypeType ptt = switch (ig.getType()) {
			case MAJOR -> XmlPhoneticProsodyTypeType.MAJOR_INTONATION_GROUP;
			case MINOR -> XmlPhoneticProsodyTypeType.MINOR_INTONATION_GROUP;
		};
		this.currentWord.getStressOrPhOrCmph().add(pp);
	}

	@Visits
	public void visitSyllableBoundary(SyllableBoundary sb) {
		final XmlPhoneticProsodyType pp = factory.createXmlPhoneticProsodyType();
		pp.setType(XmlPhoneticProsodyTypeType.SYLLABLE_BREAK);
		this.currentWord.getStressOrPhOrCmph().add(pp);
	}

	@Visits
	public void visitPause(Pause pause) {
		final XmlPauseType p = factory.createXmlPauseType();
		final XmlPauseSymbolicLengthType type = switch(pause.getType()) {
			case SIMPLE -> XmlPauseSymbolicLengthType.SIMPLE;
			case LONG -> XmlPauseSymbolicLengthType.LONG;
			case VERY_LONG -> XmlPauseSymbolicLengthType.VERY_LONG;
			case NUMERIC -> null;
		};
		if(type != null) {
			p.setSymbolicLength(type);
		} else {
			p.setLength(BigDecimal.valueOf(pause.getLength()).setScale(3, RoundingMode.HALF_UP));
		}
		if(this.currentWord.getStressOrPhOrCmph().size() > 0) {
			this.pho.getPwOrPause().add(this.currentWord);
			this.currentWord = factory.createXmlPhoneticWord();
		}
		this.pho.getPwOrPause().add(p);
	}

	@Visits
	public void visitWordBoundary(WordBoundary wb) {
		if(this.currentWord.getStressOrPhOrCmph().size() > 0) {
			this.pho.getPwOrPause().add(this.currentWord);
			this.currentWord = factory.createXmlPhoneticWord();
		}
	}

	@Override
	public void fallbackVisit(IPAElement obj) {
	}

	public XmlPhoneticTranscriptionType getPho() {
		if(this.currentWord.getStressOrPhOrCmph().size() > 0) {
			this.pho.getPwOrPause().add(this.currentWord);
		}
		return this.pho;
	}

}
