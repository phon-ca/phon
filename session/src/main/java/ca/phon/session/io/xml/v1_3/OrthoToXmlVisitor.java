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
import ca.phon.orthography.Action;
import ca.phon.orthography.Freecode;
import ca.phon.orthography.Italic;
import ca.phon.orthography.Linker;
import ca.phon.orthography.LongFeature;
import ca.phon.orthography.Nonvocal;
import ca.phon.orthography.OtherSpokenEvent;
import ca.phon.orthography.OverlapPoint;
import ca.phon.orthography.Pause;
import ca.phon.orthography.Postcode;
import ca.phon.orthography.Quotation;
import ca.phon.orthography.Replacement;
import ca.phon.orthography.SeparatorType;
import ca.phon.orthography.TagMarker;
import ca.phon.orthography.TagMarkerType;
import ca.phon.orthography.TerminatorType;
import ca.phon.orthography.ToneMarker;
import ca.phon.orthography.ToneMarkerType;
import ca.phon.orthography.Underline;
import ca.phon.session.io.xml.v13.*;
import ca.phon.util.Language;
import ca.phon.visitor.annotation.Visits;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 *
 */
public class OrthoToXmlVisitor extends AbstractOrthographyVisitor {

	private final ObjectFactory factory = new ObjectFactory();

	private final UtteranceType u;

	public OrthoToXmlVisitor() {
		this.u = factory.createUtteranceType();
	}

	public OrthoToXmlVisitor(UtteranceType u) {
		this.u = u;
	}

	@Override
    @Visits
	public void visitCompoundWord(CompoundWord compoundWord) {
		visitWord(compoundWord);
	}

	@Override
    @Visits
	public void visitWord(Word word) {
		final W w = factory.createW();
		final ca.phon.session.io.xml.v13.WordType wordType = word.getPrefix() == null ? null : switch (word.getPrefix().getType()) {
			case FILLER -> ca.phon.session.io.xml.v13.WordType.FILLER;
			case FRAGMENT -> ca.phon.session.io.xml.v13.WordType.FRAGMENT;
			case NONWORD -> ca.phon.session.io.xml.v13.WordType.NONWORD;
			case OMISSION -> ca.phon.session.io.xml.v13.WordType.OMISSION;
		};
		w.setType(wordType);

		if(word.getSuffix() != null) {
			final ca.phon.session.io.xml.v13.WordFormType wordFormType = word.getSuffix().getType() == null
					? null : switch (word.getSuffix().getType()) {
				case ADDITION -> ca.phon.session.io.xml.v13.WordFormType.ADDITION;
				case BABBLING -> ca.phon.session.io.xml.v13.WordFormType.BABBLING;
				case CHILD_INVENTED -> ca.phon.session.io.xml.v13.WordFormType.CHILD_INVENTED;
				case DIALECT -> ca.phon.session.io.xml.v13.WordFormType.DIALECT;
				case ECHOLALIA -> ca.phon.session.io.xml.v13.WordFormType.ECHOLALIA;
				case FAMILY_SPECIFIC -> ca.phon.session.io.xml.v13.WordFormType.FAMILY_SPECIFIC;
				case FILLED_PAUSE -> ca.phon.session.io.xml.v13.WordFormType.FILLED_PAUSE;
				case GENERIC -> ca.phon.session.io.xml.v13.WordFormType.GENERIC;
				case INTERJECTION -> ca.phon.session.io.xml.v13.WordFormType.INTERJECTION;
				case KANA -> ca.phon.session.io.xml.v13.WordFormType.KANA;
				case LETTER -> ca.phon.session.io.xml.v13.WordFormType.LETTER;
				case NEOLOGISM -> ca.phon.session.io.xml.v13.WordFormType.NEOLOGISM;
				case NO_VOICE -> ca.phon.session.io.xml.v13.WordFormType.NO_VOICE;
				case ONOMATOPOEIA -> ca.phon.session.io.xml.v13.WordFormType.ONOMATOPOEIA;
				case PHONOLOGY_CONSISTENT -> ca.phon.session.io.xml.v13.WordFormType.PHONOLOGY_CONSISTENT;
				case QUOTED_METAREFERENCE -> ca.phon.session.io.xml.v13.WordFormType.QUOTED_METAREFERENCE;
				case SIGNED_LANGUAGE -> ca.phon.session.io.xml.v13.WordFormType.SIGNED_LANGUAGE;
				case SIGN_SPEECH -> ca.phon.session.io.xml.v13.WordFormType.SIGN_SPEECH;
				case SINGING -> ca.phon.session.io.xml.v13.WordFormType.SINGING;
				case TEST -> ca.phon.session.io.xml.v13.WordFormType.TEST;
				case UNIBET -> ca.phon.session.io.xml.v13.WordFormType.UNIBET;
				case WORDS_TO_BE_EXCLUDED -> ca.phon.session.io.xml.v13.WordFormType.WORDS_TO_BE_EXCLUDED;
				case WORD_PLAY -> ca.phon.session.io.xml.v13.WordFormType.WORD_PLAY;
			};
			w.setFormType(wordFormType);
			w.setFormSuffix(word.getSuffix().getFormSuffix());
			if(word.getSuffix().isSeparatedPrefix())
				w.setSeparatedPrefix(word.getSuffix().isSeparatedPrefix());
			if(word.isUntranscribed()) {
				final ca.phon.session.io.xml.v13.UntranscribedType untranscribed = switch (word.getUntranscribedType()) {
					case UNTRANSCRIBED -> ca.phon.session.io.xml.v13.UntranscribedType.UNTRANSCRIBED;
					case UNINTELLIGIBLE -> ca.phon.session.io.xml.v13.UntranscribedType.UNINTELLIGIBLE;
					case UNINTELLIGIBLE_WORD_WITH_PHO -> ca.phon.session.io.xml.v13.UntranscribedType.UNINTELLIGIBLE_WITH_PHO;
				};
				w.setUntranscribed(untranscribed);
			}
			if(word.getSuffix().getUserSpecialForm() != null && word.getSuffix().getUserSpecialForm().length() > 0)
				w.setUserSpecialForm(word.getSuffix().getUserSpecialForm());
		}

		// add langs
		if(word.getLangs() != null) {
			ca.phon.session.io.xml.v13.Langs langs = factory.createLangs();
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
				final Pos pos = factory.createPos();
				pos.setC(wordPos.getCategory());
				wordPos.getSubCategories().forEach(pos.getS()::add);
				w.getContent().add(pos);
			}
		}

		for(Replacement replacement:word.getReplacements()) {
			final ca.phon.session.io.xml.v13.Replacement xmlReplacement = factory.createReplacement();
			xmlReplacement.setReal(replacement.isReal());
			final OrthoToXmlVisitor wordVisitor = new OrthoToXmlVisitor();
			replacement.getWords().forEach(wordVisitor::visitWord);
			wordVisitor.getU().getWOrGOrPg().stream().map(W.class::cast).forEach(xmlReplacement.getW()::add);
		}

		u.getWOrGOrPg().add(w);
	}

	@Override
    @Visits
	public void visitLinker(Linker linker) {
		final ca.phon.session.io.xml.v13.Linker xmlLinker = factory.createLinker();
		final ca.phon.session.io.xml.v13.LinkerType type = switch (linker.getType()) {
			case LAZY_OVERLAP_MARK -> ca.phon.session.io.xml.v13.LinkerType.LAZY_OVERLAP_MARK;
			case NO_BREAK_TCU_COMPLETION -> ca.phon.session.io.xml.v13.LinkerType.NO_BREAK_TCU_COMPLETION;
			case OTHER_COMPLETION -> ca.phon.session.io.xml.v13.LinkerType.OTHER_COMPLETION;
			case QUICK_UPTAKE -> ca.phon.session.io.xml.v13.LinkerType.QUICK_UPTAKE;
			case QUOTED_UTTERANCE_NEXT -> ca.phon.session.io.xml.v13.LinkerType.QUOTED_UTTERANCE_NEXT;
			case SELF_COMPLETION -> ca.phon.session.io.xml.v13.LinkerType.SELF_COMPLETION;
			case TECHNICAL_BREAK_TCU_COMPLETION -> ca.phon.session.io.xml.v13.LinkerType.TECHNICAL_BREAK_TCU_COMPLETION;
		};
		xmlLinker.setType(type);
		u.getLinker().add(xmlLinker);
	}

	@Override
    @Visits
	public void visitOrthoGroup(OrthoGroup group) {
		final G xmlG = factory.createG();
		final OrthoToXmlVisitor innerGVisitor = new OrthoToXmlVisitor();
		group.getElements().forEach(innerGVisitor::visit);
		innerGVisitor.getU().getWOrGOrPg().forEach(xmlG.getWOrGOrPg()::add);
		final OrthoAnnotationToXmlVisitor annotationVisitor = new OrthoAnnotationToXmlVisitor(xmlG.getKOrErrorOrDuration());
		group.getAnnotations().forEach(annotationVisitor::visit);
		u.getWOrGOrPg().add(xmlG);
	}

	@Override
    @Visits
	public void visitPhoneticGroup(PhoneticGroup phoneticGroup) {
		final Pg xmlPg = factory.createPg();
		final OrthoToXmlVisitor innnerGVisitor = new OrthoToXmlVisitor();
		phoneticGroup.getElements().forEach(innnerGVisitor::visit);
		innnerGVisitor.getU().getWOrGOrPg().forEach(xmlPg.getWOrGOrE()::add);
		u.getWOrGOrPg().add(xmlPg);
	}

	@Override
    @Visits
	public void visitQuotation(Quotation quotation) {
		final ca.phon.session.io.xml.v13.Quotation xmlQuotation = factory.createQuotation();
		final BeginEndType beginEnd = switch (quotation.getBeginEnd()) {
			case BEGIN -> BeginEndType.BEGIN;
			case END -> BeginEndType.END;
		};
		xmlQuotation.setType(beginEnd);
		u.getWOrGOrPg().add(xmlQuotation);
	}

	@Override
    @Visits
	public void visitPause(Pause pause) {
		final ca.phon.session.io.xml.v13.Pause xmlPause = factory.createPause();
		final PauseSymbolicLengthType type = switch (pause.getType()) {
			case SIMPLE -> PauseSymbolicLengthType.SIMPLE;
			case LONG -> PauseSymbolicLengthType.LONG;
			case VERY_LONG -> PauseSymbolicLengthType.VERY_LONG;
			case NUMERIC -> null;
		};
		if(type == null) {
			xmlPause.setLength(BigDecimal.valueOf(pause.getLength() * 1000.0f));
		} else {
			xmlPause.setSymbolicLength(type);
		}
		u.getWOrGOrPg().add(xmlPause);
	}

	@Override
    @Visits
	public void visitInternalMedia(InternalMedia internalMedia) {
		final MediaType xmlMedia = factory.createMediaType();
		xmlMedia.setUnit(MediaUnitType.MS);
		float startTime = internalMedia.getStartTime() * 1000.0f;
		float endTime = internalMedia.getEndTime() * 1000.0f;
		xmlMedia.setStart(BigDecimal.valueOf(startTime));
		xmlMedia.setEnd(BigDecimal.valueOf(endTime));
		u.getWOrGOrPg().add(xmlMedia);
	}

	@Override
    @Visits
	public void visitFreecode(Freecode freecode) {
		final ca.phon.session.io.xml.v13.Freecode xmlFreecode = factory.createFreecode();
		xmlFreecode.setValue(freecode.getCode());
		u.getWOrGOrPg().add(xmlFreecode);
	}

	public void visitEventAnnotations(Event event, E xmlEvent) {
		final OrthoAnnotationToXmlVisitor annotationVisitor = new OrthoAnnotationToXmlVisitor(xmlEvent.getKOrErrorOrOverlap());
		event.getAnnotations().forEach(annotationVisitor::visit);
	}

	@Override
    @Visits
	public void visitAction(Action action) {
		final E xmlEvent = factory.createE();
		xmlEvent.setAction(factory.createAction());
		visitEventAnnotations(action, xmlEvent);
		u.getWOrGOrPg().add(xmlEvent);
	}

	@Override
    @Visits
	public void visitHappening(Happening happening) {
		final E xmlEvent = factory.createE();
		xmlEvent.setHappening(happening.getData());
		visitEventAnnotations(happening, xmlEvent);
		u.getWOrGOrPg().add(xmlEvent);
	}

	@Override
    @Visits
	public void visitOtherSpokenEvent(OtherSpokenEvent otherSpokenEvent) {
		final E xmlEvent = factory.createE();
		final ca.phon.session.io.xml.v13.OtherSpokenEvent xmlOte = factory.createOtherSpokenEvent();
		xmlOte.setWho(otherSpokenEvent.getWho());
		xmlOte.setSaid(otherSpokenEvent.getData());
		xmlEvent.setOtherSpokenEvent(xmlOte);
		visitEventAnnotations(otherSpokenEvent, xmlEvent);
		u.getWOrGOrPg().add(xmlEvent);
	}

	@Override
    @Visits
	public void visitSeparator(Separator separator) {
		final S xmlS = factory.createS();
		final ca.phon.session.io.xml.v13.SeparatorType type = switch (separator.getType()) {
			case CLAUSE_DELIMITER -> ca.phon.session.io.xml.v13.SeparatorType.CLAUSE_DELIMITER;
			case COLON -> ca.phon.session.io.xml.v13.SeparatorType.COLON;
			case SEMICOLON -> ca.phon.session.io.xml.v13.SeparatorType.SEMICOLON;
			case UNMARKED_ENDING -> ca.phon.session.io.xml.v13.SeparatorType.UNMARKED_ENDING;
			case UPTAKE -> ca.phon.session.io.xml.v13.SeparatorType.UPTAKE;
		};
		xmlS.setType(type);
		u.getWOrGOrPg().add(xmlS);
	}

	@Override
    @Visits
	public void visitToneMarker(ToneMarker toneMarker) {
		final ca.phon.session.io.xml.v13.ToneMarker xmlToneMarker = factory.createToneMarker();
		final ca.phon.session.io.xml.v13.ToneMarkerType type = switch (toneMarker.getType()) {
			case FALLING_TO_LOW -> ca.phon.session.io.xml.v13.ToneMarkerType.FALLING_TO_LOW;
			case FALLING_TO_MID -> ca.phon.session.io.xml.v13.ToneMarkerType.FALLING_TO_MID;
			case LEVEL -> ca.phon.session.io.xml.v13.ToneMarkerType.LEVEL;
			case RISING_TO_HIGH -> ca.phon.session.io.xml.v13.ToneMarkerType.RISING_TO_HIGH;
			case RISING_TO_MID -> ca.phon.session.io.xml.v13.ToneMarkerType.RISING_TO_MID;
		};
		xmlToneMarker.setType(type);
		u.getWOrGOrPg().add(xmlToneMarker);
	}

	@Override
    @Visits
	public void visitTagMarker(TagMarker tagMarker) {
		final ca.phon.session.io.xml.v13.TagMarker xmlTagMarker = factory.createTagMarker();
		final ca.phon.session.io.xml.v13.TagMarkerType type = switch (tagMarker.getType()) {
			case COMMA -> ca.phon.session.io.xml.v13.TagMarkerType.COMMA;
			case TAG -> ca.phon.session.io.xml.v13.TagMarkerType.TAG;
			case VOCATIVE -> ca.phon.session.io.xml.v13.TagMarkerType.VOCATIVE;
		};
		xmlTagMarker.setType(type);
		u.getWOrGOrPg().add(xmlTagMarker);
	}

	@Override
    @Visits
	public void visitOverlapPoint(OverlapPoint overlapPoint) {
		final ca.phon.session.io.xml.v13.OverlapPoint xmlOverlapPt = factory.createOverlapPoint();
		final StartEndType startEnd = switch (overlapPoint.getType()) {
			case TOP_END,BOTTOM_END -> StartEndType.END;
			case TOP_START,BOTTOM_START -> StartEndType.START;
		};
		final TopBottomType topBottom = switch (overlapPoint.getType()) {
			case TOP_START,TOP_END -> TopBottomType.TOP;
			case BOTTOM_END,BOTTOM_START -> TopBottomType.BOTTOM;
		};
		xmlOverlapPt.setStartEnd(startEnd);
		xmlOverlapPt.setTopBottom(topBottom);
		if(overlapPoint.getIndex() >= 0)
			xmlOverlapPt.setIndex(BigInteger.valueOf(overlapPoint.getIndex()));
		u.getWOrGOrPg().add(xmlOverlapPt);
	}

	@Override
    @Visits
	public void visitUnderline(Underline underline) {
		final ca.phon.session.io.xml.v13.Underline xmlUnderline = factory.createUnderline();
		final BeginEndType beginEnd = switch (underline.getBeginEnd()) {
			case END -> BeginEndType.END;
			case BEGIN -> BeginEndType.BEGIN;
		};
		xmlUnderline.setType(beginEnd);
		u.getWOrGOrPg().add(xmlUnderline);
	}

	@Override
    @Visits
	public void visitItalic(Italic italic) {
		final ca.phon.session.io.xml.v13.Italic xmlItalic = factory.createItalic();
		final BeginEndType beginEnd = switch (italic.getBeginEnd()) {
			case END -> BeginEndType.END;
			case BEGIN -> BeginEndType.BEGIN;
		};
		xmlItalic.setType(beginEnd);
		u.getWOrGOrPg().add(xmlItalic);
	}

	@Override
    @Visits
	public void visitLongFeature(LongFeature longFeature) {
		final ca.phon.session.io.xml.v13.LongFeature xmlLongFeature = factory.createLongFeature();
		final BeginEndType beginEnd = switch (longFeature.getBeginEnd()) {
			case BEGIN -> BeginEndType.BEGIN;
			case END -> BeginEndType.END;
		};
		xmlLongFeature.setType(beginEnd);
		xmlLongFeature.setValue(longFeature.getLabel());
		u.getWOrGOrPg().add(xmlLongFeature);
	}

	@Override
    @Visits
	public void visitNonvocal(Nonvocal nonvocal) {
		final ca.phon.session.io.xml.v13.Nonvocal xmlNonvocal = factory.createNonvocal();
		final BeginEndSimpleType beginEndSimple = switch (nonvocal.getBeginEndSimple()) {
			case BEGIN -> BeginEndSimpleType.BEGIN;
			case END -> BeginEndSimpleType.END;
			case SIMPLE -> BeginEndSimpleType.SIMPLE;
		};
		xmlNonvocal.setType(beginEndSimple);
		xmlNonvocal.setValue(nonvocal.getLabel());
		u.getWOrGOrPg().add(xmlNonvocal);
	}

	@Override
    @Visits
	public void visitTerminator(Terminator terminator) {
		final T xmlT = factory.createT();
		final ca.phon.session.io.xml.v13.TerminatorType type = switch (terminator.getType()) {
			case BROKEN_FOR_CODING -> ca.phon.session.io.xml.v13.TerminatorType.BROKEN_FOR_CODING;
			case EXCLAMATION -> ca.phon.session.io.xml.v13.TerminatorType.E;
			case INTERRUPTION -> ca.phon.session.io.xml.v13.TerminatorType.INTERRUPTION;
			case INTERRUPTION_QUESTION -> ca.phon.session.io.xml.v13.TerminatorType.INTERRUPTION_QUESTION;
			case NO_BREAK_TCU_CONTINUATION -> ca.phon.session.io.xml.v13.TerminatorType.NO_BREAK_TCU_CONTINUATION;
			case PERIOD -> ca.phon.session.io.xml.v13.TerminatorType.P;
			case QUESTION -> ca.phon.session.io.xml.v13.TerminatorType.Q;
			case QUESTION_EXCLAMATION -> ca.phon.session.io.xml.v13.TerminatorType.QUESTION_EXCLAMATION;
			case QUOTATION_NEXT_LINE -> ca.phon.session.io.xml.v13.TerminatorType.QUOTATION_NEXT_LINE;
			case QUOTATION_PRECEDES -> ca.phon.session.io.xml.v13.TerminatorType.QUOTATION_PRECEDES;
			case SELF_INTERRUPTION -> ca.phon.session.io.xml.v13.TerminatorType.SELF_INTERRUPTION;
			case SELF_INTERRUPTION_QUESTION -> ca.phon.session.io.xml.v13.TerminatorType.SELF_INTERRUPTION_QUESTION;
			case TECHNICAL_BREAK_TCU_CONTINUATION -> ca.phon.session.io.xml.v13.TerminatorType.TECHNICAL_BREAK_TCU_CONTINUATION;
			case TRAIL_OFF -> ca.phon.session.io.xml.v13.TerminatorType.TRAIL_OFF;
			case TRAIL_OFF_QUESTION -> ca.phon.session.io.xml.v13.TerminatorType.TRAIL_OFF_QUESTION;
		};
		xmlT.setType(type);
		u.setT(xmlT);
	}

	@Override
    @Visits
	public void visitPostcode(Postcode postcode) {
		final ca.phon.session.io.xml.v13.Postcode xmlPostcode = factory.createPostcode();
		xmlPostcode.setValue(postcode.getCode());
		u.getPostcode().add(xmlPostcode);
	}

	public UtteranceType getU() {
		return this.u;
	}

	@Override
	public void fallbackVisit(OrthographyElement obj) {

	}

}
