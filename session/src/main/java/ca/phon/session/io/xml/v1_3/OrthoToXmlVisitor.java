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

import ca.phon.orthography.*;
import ca.phon.orthography.xml.*;
import ca.phon.util.Language;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OrthoToXmlVisitor extends AbstractOrthographyVisitor {

	private final ObjectFactory factory = new ObjectFactory();

	private final List<Object> elements;

	public OrthoToXmlVisitor() {
		this(new ArrayList());
	}

	public OrthoToXmlVisitor(List<Object> elements) {
		this.elements = elements;
	}

	@Override
	public void visitCompoundWord(CompoundWord compoundWord) {
		visitWord(compoundWord);
	}

	@Override
	public void visitWord(Word word) {
		final XMLOrthographyW w = factory.createXMLOrthographyW();
		final XMLOrthographyWordType wordType = word.getPrefix() == null ? null : switch (word.getPrefix().getType()) {
			case FILLER -> XMLOrthographyWordType.FILLER;
			case FRAGMENT -> XMLOrthographyWordType.FRAGMENT;
			case NONWORD -> XMLOrthographyWordType.NONWORD;
			case OMISSION -> XMLOrthographyWordType.OMISSION;
		};
		w.setType(wordType);

		if(word.getSuffix() != null) {
			final XMLOrthographyWordFormType wordFormType = word.getSuffix().getType() == null
					? null : switch (word.getSuffix().getType()) {
				case ADDITION -> XMLOrthographyWordFormType.ADDITION;
				case BABBLING -> XMLOrthographyWordFormType.BABBLING;
				case CHILD_INVENTED -> XMLOrthographyWordFormType.CHILD_INVENTED;
				case DIALECT -> XMLOrthographyWordFormType.DIALECT;
				case ECHOLALIA -> XMLOrthographyWordFormType.ECHOLALIA;
				case FAMILY_SPECIFIC -> XMLOrthographyWordFormType.FAMILY_SPECIFIC;
				case FILLED_PAUSE -> XMLOrthographyWordFormType.FILLED_PAUSE;
				case GENERIC -> XMLOrthographyWordFormType.GENERIC;
				case INTERJECTION -> XMLOrthographyWordFormType.INTERJECTION;
				case KANA -> XMLOrthographyWordFormType.KANA;
				case LETTER -> XMLOrthographyWordFormType.LETTER;
				case NEOLOGISM -> XMLOrthographyWordFormType.NEOLOGISM;
				case NO_VOICE -> XMLOrthographyWordFormType.NO_VOICE;
				case ONOMATOPOEIA -> XMLOrthographyWordFormType.ONOMATOPOEIA;
				case PHONOLOGY_CONSISTENT -> XMLOrthographyWordFormType.PHONOLOGY_CONSISTENT;
				case QUOTED_METAREFERENCE -> XMLOrthographyWordFormType.QUOTED_METAREFERENCE;
				case SIGNED_LANGUAGE -> XMLOrthographyWordFormType.SIGNED_LANGUAGE;
				case SIGN_SPEECH -> XMLOrthographyWordFormType.SIGN_SPEECH;
				case SINGING -> XMLOrthographyWordFormType.SINGING;
				case TEST -> XMLOrthographyWordFormType.TEST;
				case UNIBET -> XMLOrthographyWordFormType.UNIBET;
				case WORDS_TO_BE_EXCLUDED -> XMLOrthographyWordFormType.WORDS_TO_BE_EXCLUDED;
				case WORD_PLAY -> XMLOrthographyWordFormType.WORD_PLAY;
			};
			w.setFormType(wordFormType);
			w.setFormSuffix(word.getSuffix().getFormSuffix());
			w.setSeparatedPrefix(word.getSuffix().isSeparatedPrefix());
			if(word.isUntranscribed()) {
				final XMLOrthographyUntranscribedType untranscribed = switch (word.getUntranscribedType()) {
					case UNTRANSCRIBED -> XMLOrthographyUntranscribedType.UNTRANSCRIBED;
					case UNINTELLIGIBLE -> XMLOrthographyUntranscribedType.UNINTELLIGIBLE;
					case UNINTELLIGIBLE_WORD_WITH_PHO -> XMLOrthographyUntranscribedType.UNINTELLIGIBLE_WITH_PHO;
				};
				w.setUntranscribed(untranscribed);
			}
			w.setUserSpecialForm(word.getSuffix().getUserSpecialForm());
		}

		// add langs
		if(word.getLangs() != null) {
			XMLOrthographyLangs langs = factory.createXMLOrthographyLangs();
			switch(word.getLangs().getType()) {
				case SINGLE:
					langs.setSingle(word.getLangs().getLangs().get(0).toString());
					break;

				case SECONDARY:
					break;

				case AMBIGUOUS:
					langs.getAmbiguous().addAll(word.getLangs().getLangs().stream().map(Language::toString).toList());
					break;

				case MULTIPLE:
					langs.getMultiple().addAll(word.getLangs().getLangs().stream().map(Language::toString).toList());
					break;

				case UNSPECIFIED:
				default:
					langs = null;
					break;
			}
			if(langs != null)
				w.getContent().add(langs);
		}

		final WordElementTwoXmlVisitor wordContentVisitor = new WordElementTwoXmlVisitor(w.getContent());
		word.getWordElements().forEach(wordContentVisitor::visit);

		if(word.getSuffix() != null) {
			for (WordPos wordPos : word.getSuffix().getWordPos()) {
				final XMLOrthographyPos pos = factory.createXMLOrthographyPos();
				pos.setC(wordPos.getCategory());
				wordPos.getSubCategories().forEach(pos.getS()::add);
				w.getContent().add(pos);
			}
		}

		for(Replacement replacement:word.getReplacements()) {
			final XMLOrthographyReplacement xmlReplacement = factory.createXMLOrthographyReplacement();
			xmlReplacement.setReal(replacement.isReal());
			final OrthoToXmlVisitor wordVisitor = new OrthoToXmlVisitor();
			replacement.getWords().forEach(wordVisitor::visitWord);
			wordVisitor.elements.stream().map(XMLOrthographyW.class::cast).forEach(xmlReplacement.getW()::add);
		}

		elements.add(w);
	}

	@Override
	public void visitLinker(Linker linker) {
		final XMLOrthographyLinker xmlLinker = factory.createXMLOrthographyLinker();
		final XMLOrthographyLinkerType type = switch (linker.getType()) {
			case LAZY_OVERLAP_MARK -> XMLOrthographyLinkerType.LAZY_OVERLAP_MARK;
			case NO_BREAK_TCU_COMPLETION -> XMLOrthographyLinkerType.NO_BREAK_TCU_COMPLETION;
			case OTHER_COMPLETION -> XMLOrthographyLinkerType.OTHER_COMPLETION;
			case QUICK_UPTAKE -> XMLOrthographyLinkerType.QUICK_UPTAKE;
			case QUOTED_UTTERANCE_NEXT -> XMLOrthographyLinkerType.QUOTED_UTTERANCE_NEXT;
			case SELF_COMPLETION -> XMLOrthographyLinkerType.SELF_COMPLETION;
			case TECHNICAL_BREAK_TCU_COMPLETION -> XMLOrthographyLinkerType.TECHNICAL_BREAK_TCU_COMPLETION;
		};
		xmlLinker.setType(type);
		elements.add(xmlLinker);
	}

	@Override
	public void visitOrthoGroup(OrthoGroup group) {
		final XMLOrthographyG xmlG = factory.createXMLOrthographyG();
		final OrthoToXmlVisitor innerGVisitor = new OrthoToXmlVisitor(xmlG.getWOrGOrPg());
		group.getElements().forEach(innerGVisitor::visit);
		final OrthoAnnotationToXmlVisitor annotationVisitor = new OrthoAnnotationToXmlVisitor(xmlG.getKOrErrorOrDuration());
		group.getAnnotations().forEach(annotationVisitor::visit);
		elements.add(xmlG);
	}

	@Override
	public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
		final XMLOrthographyPg xmlPg = factory.createXMLOrthographyPg();
		final OrthoToXmlVisitor innnerGVisitor = new OrthoToXmlVisitor(xmlPg.getWOrGOrE());
		phoneticGroup.getElements().forEach(innnerGVisitor::visit);
		elements.add(xmlPg);
	}

	@Override
	public void visitQuotation(Quotation quotation) {
		final XMLOrthographyQuotation xmlQuotation = factory.createXMLOrthographyQuotation();
		final XMLOrthographyBeginEndType beginEnd = switch (quotation.getBeginEnd()) {
			case BEGIN -> XMLOrthographyBeginEndType.BEGIN;
			case END -> XMLOrthographyBeginEndType.END;
		};
		xmlQuotation.setType(beginEnd);
		elements.add(xmlQuotation);
	}

	@Override
	public void visitPause(Pause pause) {
		final XMLOrthographyPause xmlPause = factory.createXMLOrthographyPause();
		final XMLOrthographyPauseSymbolicLengthType type = switch (pause.getType()) {
			case SIMPLE -> XMLOrthographyPauseSymbolicLengthType.SIMPLE;
			case LONG -> XMLOrthographyPauseSymbolicLengthType.LONG;
			case VERY_LONG -> XMLOrthographyPauseSymbolicLengthType.VERY_LONG;
			case NUMERIC -> null;
		};
		if(type == null) {
			xmlPause.setLength(BigDecimal.valueOf(pause.getLength() * 1000.0f));
		} else {
			xmlPause.setSymbolicLength(type);
		}
		elements.add(xmlPause);
	}

	@Override
	public void visitInternalMedia(InternalMedia internalMedia) {
		final XMLOrthographyMediaType xmlMedia = factory.createXMLOrthographyMediaType();
		xmlMedia.setUnit(XMLOrthographyMediaUnitType.MS);
		float startTime = internalMedia.getStartTime() * 1000.0f;
		float endTime = internalMedia.getEndTime() * 1000.0f;
		xmlMedia.setStart(BigDecimal.valueOf(startTime));
		xmlMedia.setEnd(BigDecimal.valueOf(endTime));
		elements.add(xmlMedia);
	}

	@Override
	public void visitFreecode(Freecode freecode) {
		final XMLOrthographyFreecode xmlFreecode = factory.createXMLOrthographyFreecode();
		xmlFreecode.setValue(freecode.getCode());
		elements.add(xmlFreecode);
	}

	public void visitEventAnnotations(Event event, XMLOrthographyE xmlEvent) {
		final OrthoAnnotationToXmlVisitor annotationVisitor = new OrthoAnnotationToXmlVisitor(xmlEvent.getKOrErrorOrOverlap());
		event.getAnnotations().forEach(annotationVisitor::visit);
	}

	@Override
	public void visitAction(Action action) {
		final XMLOrthographyE xmlEvent = factory.createXMLOrthographyE();
		xmlEvent.setAction(factory.createXMLOrthographyAction());
		visitEventAnnotations(action, xmlEvent);
		elements.add(xmlEvent);
	}

	@Override
	public void visitHappening(Happening happening) {
		final XMLOrthographyE xmlEvent = factory.createXMLOrthographyE();
		xmlEvent.setHappening(happening.getData());
		visitEventAnnotations(happening, xmlEvent);
		elements.add(xmlEvent);
	}

	@Override
	public void visitOtherSpokenEvent(OtherSpokenEvent otherSpokenEvent) {
		final XMLOrthographyE xmlEvent = factory.createXMLOrthographyE();
		final XMLOrthographyOtherSpokenEvent xmlOte = factory.createXMLOrthographyOtherSpokenEvent();
		xmlOte.setWho(otherSpokenEvent.getWho());
		xmlOte.setSaid(otherSpokenEvent.getData());
		xmlEvent.setOtherSpokenEvent(xmlOte);
		visitEventAnnotations(otherSpokenEvent, xmlEvent);
		elements.add(xmlEvent);
	}

	@Override
	public void visitSeparator(Separator separator) {
		final XMLOrthographyS xmlS = factory.createXMLOrthographyS();
		final XMLOrthographySeparatorType type = switch (separator.getType()) {
			case CLAUSE_DELIMITER -> XMLOrthographySeparatorType.CLAUSE_DELIMITER;
			case COLON -> XMLOrthographySeparatorType.COLON;
			case SEMICOLON -> XMLOrthographySeparatorType.SEMICOLON;
			case UNMARKED_ENDING -> XMLOrthographySeparatorType.UNMARKED_ENDING;
			case UPTAKE -> XMLOrthographySeparatorType.UPTAKE;
		};
		xmlS.setType(type);
		elements.add(xmlS);
	}

	@Override
	public void visitToneMarker(ToneMarker toneMarker) {
		final XMLOrthographyToneMarker xmlToneMarker = factory.createXMLOrthographyToneMarker();
		final XMLOrthographyToneMarkerType type = switch (toneMarker.getType()) {
			case FALLING_TO_LOW -> XMLOrthographyToneMarkerType.FALLING_TO_LOW;
			case FALLING_TO_MID -> XMLOrthographyToneMarkerType.FALLING_TO_MID;
			case LEVEL -> XMLOrthographyToneMarkerType.LEVEL;
			case RISING_TO_HIGH -> XMLOrthographyToneMarkerType.RISING_TO_HIGH;
			case RISING_TO_MID -> XMLOrthographyToneMarkerType.RISING_TO_MID;
		};
		xmlToneMarker.setType(type);
		elements.add(xmlToneMarker);
	}

	@Override
	public void visitTagMarker(TagMarker tagMarker) {
		final XMLOrthographyTagMarker xmlTagMarker = factory.createXMLOrthographyTagMarker();
		final XMLOrthographyTagMarkerType type = switch (tagMarker.getType()) {
			case COMMA -> XMLOrthographyTagMarkerType.COMMA;
			case TAG -> XMLOrthographyTagMarkerType.TAG;
			case VOCATIVE -> XMLOrthographyTagMarkerType.VOCATIVE;
		};
		xmlTagMarker.setType(type);
		elements.add(xmlTagMarker);
	}

	@Override
	public void visitOverlapPoint(OverlapPoint overlapPoint) {
		final XMLOrthographyOverlapPoint xmlOverlapPt = factory.createXMLOrthographyOverlapPoint();
		final XMLOrthographyStartEndType startEnd = switch (overlapPoint.getType()) {
			case TOP_END,BOTTOM_END -> XMLOrthographyStartEndType.END;
			case TOP_START,BOTTOM_START -> XMLOrthographyStartEndType.START;
		};
		final XMLOrthographyTopBottomType topBottom = switch (overlapPoint.getType()) {
			case TOP_START,TOP_END -> XMLOrthographyTopBottomType.TOP;
			case BOTTOM_END,BOTTOM_START -> XMLOrthographyTopBottomType.BOTTOM;
		};
		xmlOverlapPt.setStartEnd(startEnd);
		xmlOverlapPt.setTopBottom(topBottom);
		if(overlapPoint.getIndex() >= 0)
			xmlOverlapPt.setIndex(BigInteger.valueOf(overlapPoint.getIndex()));
		elements.add(xmlOverlapPt);
	}

	@Override
	public void visitUnderline(Underline underline) {
		final XMLOrthographyUnderline xmlUnderline = factory.createXMLOrthographyUnderline();
		final XMLOrthographyBeginEndType beginEnd = switch (underline.getBeginEnd()) {
			case END -> XMLOrthographyBeginEndType.END;
			case BEGIN -> XMLOrthographyBeginEndType.BEGIN;
		};
		xmlUnderline.setType(beginEnd);
		elements.add(underline);
	}

	@Override
	public void visitItalic(Italic italic) {
		final XMLOrthographyItalic xmlItalic = factory.createXMLOrthographyItalic();
		final XMLOrthographyBeginEndType beginEnd = switch (italic.getBeginEnd()) {
			case END -> XMLOrthographyBeginEndType.END;
			case BEGIN -> XMLOrthographyBeginEndType.BEGIN;
		};
		xmlItalic.setType(beginEnd);
		elements.add(xmlItalic);
	}

	@Override
	public void visitLongFeature(LongFeature longFeature) {
		final XMLOrthographyLongFeature xmlLongFeature = factory.createXMLOrthographyLongFeature();
		final XMLOrthographyBeginEndType beginEnd = switch (longFeature.getBeginEnd()) {
			case BEGIN -> XMLOrthographyBeginEndType.BEGIN;
			case END -> XMLOrthographyBeginEndType.END;
		};
		xmlLongFeature.setType(beginEnd);
		xmlLongFeature.setValue(longFeature.getLabel());
		elements.add(xmlLongFeature);
	}

	@Override
	public void visitNonvocal(Nonvocal nonvocal) {
		final XMLOrthographyNonvocal xmlNonvocal = factory.createXMLOrthographyNonvocal();
		final XMLOrthographyBeginEndSimpleType beginEndSimple = switch (nonvocal.getBeginEndSimple()) {
			case BEGIN -> XMLOrthographyBeginEndSimpleType.BEGIN;
			case END -> XMLOrthographyBeginEndSimpleType.END;
			case SIMPLE -> XMLOrthographyBeginEndSimpleType.SIMPLE;
		};
		xmlNonvocal.setType(beginEndSimple);
		xmlNonvocal.setValue(nonvocal.getLabel());
		elements.add(xmlNonvocal);
	}

	@Override
	public void visitTerminator(Terminator terminator) {
		final XMLOrthographyT xmlT = factory.createXMLOrthographyT();
		final XMLOrthographyTerminatorType type = switch (terminator.getType()) {
			case BROKEN_FOR_CODING -> XMLOrthographyTerminatorType.BROKEN_FOR_CODING;
			case EXCLAMATION -> XMLOrthographyTerminatorType.E;
			case INTERRUPTION -> XMLOrthographyTerminatorType.INTERRUPTION;
			case INTERRUPTION_QUESTION -> XMLOrthographyTerminatorType.INTERRUPTION_QUESTION;
			case NO_BREAK_TCU_CONTINUATION -> XMLOrthographyTerminatorType.NO_BREAK_TCU_CONTINUATION;
			case PERIOD -> XMLOrthographyTerminatorType.P;
			case QUESTION -> XMLOrthographyTerminatorType.Q;
			case QUESTION_EXCLAMATION -> XMLOrthographyTerminatorType.QUESTION_EXCLAMATION;
			case QUOTATION_NEXT_LINE -> XMLOrthographyTerminatorType.QUOTATION_NEXT_LINE;
			case QUOTATION_PRECEDES -> XMLOrthographyTerminatorType.QUOTATION_PRECEDES;
			case SELF_INTERRUPTION -> XMLOrthographyTerminatorType.SELF_INTERRUPTION;
			case SELF_INTERRUPTION_QUESTION -> XMLOrthographyTerminatorType.SELF_INTERRUPTION_QUESTION;
			case TECHNICAL_BREAK_TCU_CONTINUATION -> XMLOrthographyTerminatorType.TECHNICAL_BREAK_TCU_CONTINUATION;
			case TRAIL_OFF -> XMLOrthographyTerminatorType.TRAIL_OFF;
			case TRAIL_OFF_QUESTION -> XMLOrthographyTerminatorType.TRAIL_OFF_QUESTION;
		};
		xmlT.setType(type);
		elements.add(xmlT);
	}

	@Override
	public void visitPostcode(Postcode postcode) {
		final XMLOrthographyPostcode xmlPostcode = factory.createXMLOrthographyPostcode();
		xmlPostcode.setValue(postcode.getCode());
		elements.add(xmlPostcode);
	}

	public List<Object> getElements() {
		return elements;
	}

	@Override
	public void fallbackVisit(OrthographyElement obj) {

	}

}
