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
package ca.phon.session.io.xml.v2_0;

import ca.phon.orthography.mor.Grasp;
import ca.phon.orthography.mor.GraspTierData;
import ca.phon.orthography.mor.Pos;
import ca.phon.orthography.*;
import ca.phon.orthography.Action;
import ca.phon.orthography.Error;
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
import ca.phon.orthography.TagMarker;
import ca.phon.orthography.ToneMarker;
import ca.phon.orthography.Underline;
import ca.phon.session.UserTierType;
import ca.phon.session.io.xml.OneToOne;
import ca.phon.session.io.xml.v2_0.*;
import ca.phon.util.Language;
import ca.phon.visitor.annotation.Visits;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class OrthoToXmlVisitor extends AbstractOrthographyVisitor {

	private final ObjectFactory factory = new ObjectFactory();

	private final XmlUtteranceType u;

	public OrthoToXmlVisitor() {
		this.u = factory.createXmlUtteranceType();
	}

	public OrthoToXmlVisitor(XmlUtteranceType u) {
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
		final XmlWType w = factory.createXmlWType();
		final XmlWordType wordType = word.getPrefix() == null ? null : switch (word.getPrefix().getType()) {
			case FILLER -> XmlWordType.FILLER;
			case FRAGMENT -> XmlWordType.FRAGMENT;
			case NONWORD -> XmlWordType.NONWORD;
			case OMISSION -> XmlWordType.OMISSION;
		};
		w.setType(wordType);

		if(word.getSuffix() != null) {
			final XmlWordFormType wordFormType = word.getSuffix().getType() == null
					? null : switch (word.getSuffix().getType()) {
				case ADDITION -> XmlWordFormType.ADDITION;
				case BABBLING -> XmlWordFormType.BABBLING;
				case CHILD_INVENTED -> XmlWordFormType.CHILD_INVENTED;
				case DIALECT -> XmlWordFormType.DIALECT;
				case ECHOLALIA -> XmlWordFormType.ECHOLALIA;
				case FAMILY_SPECIFIC -> XmlWordFormType.FAMILY_SPECIFIC;
				case FILLED_PAUSE -> XmlWordFormType.FILLED_PAUSE;
				case GENERIC -> XmlWordFormType.GENERIC;
				case INTERJECTION -> XmlWordFormType.INTERJECTION;
				case KANA -> XmlWordFormType.KANA;
				case LETTER -> XmlWordFormType.LETTER;
				case NEOLOGISM -> XmlWordFormType.NEOLOGISM;
				case NO_VOICE -> XmlWordFormType.NO_VOICE;
				case ONOMATOPOEIA -> XmlWordFormType.ONOMATOPOEIA;
				case PHONOLOGY_CONSISTENT -> XmlWordFormType.PHONOLOGY_CONSISTENT;
				case QUOTED_METAREFERENCE -> XmlWordFormType.QUOTED_METAREFERENCE;
				case SIGNED_LANGUAGE -> XmlWordFormType.SIGNED_LANGUAGE;
				case SIGN_SPEECH -> XmlWordFormType.SIGN_SPEECH;
				case SINGING -> XmlWordFormType.SINGING;
				case TEST -> XmlWordFormType.TEST;
				case UNIBET -> XmlWordFormType.UNIBET;
				case WORDS_TO_BE_EXCLUDED -> XmlWordFormType.WORDS_TO_BE_EXCLUDED;
				case WORD_PLAY -> XmlWordFormType.WORD_PLAY;
			};
			w.setFormType(wordFormType);
			w.setFormSuffix(word.getSuffix().getFormSuffix());
			if(word.getSuffix().isSeparatedPrefix())
				w.setSeparatedPrefix(word.getSuffix().isSeparatedPrefix());
			if(word.isUntranscribed()) {
				final XmlUntranscribedType untranscribed = switch (word.getUntranscribedType()) {
					case UNTRANSCRIBED -> XmlUntranscribedType.UNTRANSCRIBED;
					case UNINTELLIGIBLE -> XmlUntranscribedType.UNINTELLIGIBLE;
					case UNINTELLIGIBLE_WORD_WITH_PHO -> XmlUntranscribedType.UNINTELLIGIBLE_WITH_PHO;
				};
				w.setUntranscribed(untranscribed);
			}
			if(word.getSuffix().getUserSpecialForm() != null && word.getSuffix().getUserSpecialForm().length() > 0)
				w.setUserSpecialForm(word.getSuffix().getUserSpecialForm());
		}

		// add langs
		if(word.getLangs() != null) {
			XmlLangsType langs = factory.createXmlLangsType();
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
				w.getContent().add(factory.createLangs(langs));
		}

		final WordElementTwoXmlVisitor wordContentVisitor = new WordElementTwoXmlVisitor(w.getContent());
		word.getWordElements().forEach(wordContentVisitor::visit);

		if(word.getSuffix() != null) {
			for (Pos wordPos : word.getSuffix().getWordPos()) {
				final XmlPosType pos = factory.createXmlPosType();
				final XmlCategoryType xmlCategoryType = factory.createXmlCategoryType();
				xmlCategoryType.setValue(wordPos.getCategory());
				pos.setC(xmlCategoryType);
				wordPos.getSubCategories().stream()
						.map(subc -> {
							var t = factory.createXmlSubcategoryType();
							t.setValue(subc);
							return t; })
						.forEach(pos.getSubc()::add);
				w.getContent().add(factory.createPos(pos));
			}
		}

		for(Replacement replacement:word.getReplacements()) {
			final XmlReplacementType xmlReplacement = factory.createXmlReplacementType();
			xmlReplacement.setReal(replacement.isReal());
			final OrthoToXmlVisitor wordVisitor = new OrthoToXmlVisitor();
			replacement.getWords().forEach(wordVisitor::visitWord);
			wordVisitor.getU().getWOrGOrPg().stream()
					.map(XmlWType.class::cast).forEach(xmlReplacement.getW()::add);
			w.getContent().add(factory.createReplacement(xmlReplacement));
		}

		final OneToOne oneToOne = word.getExtension(OneToOne.class);
		if(oneToOne != null) {
			final List<XmlMorType> morTiers = writeMorTierData(oneToOne);
			for(XmlMorType mor:morTiers) {
				w.getContent().add(factory.createMor(mor));
			}
		}

		u.getWOrGOrPg().add(w);
	}

	private List<XmlMorType> writeMorTierData(OneToOne oneToOne) {
		List<XmlMorType> retVal = new ArrayList<>();
		for(String tierName:oneToOne.getMorTierData().keySet()) {
			final UserTierType userTierType = UserTierType.fromPhonTierName(tierName);
			if(userTierType == null) continue;
			final String type = userTierType.getChatTierName().substring(1);
			final XmlSessionWriterV2_0 writer = new XmlSessionWriterV2_0();
			final XmlMorType xmlMorType = writer.writeMor(new ObjectFactory(), oneToOne.getMorTierData().get(tierName), type);
			retVal.add(xmlMorType);

			// check for grasp data
			if(userTierType == UserTierType.Mor) {
				final GraspTierData grasp = oneToOne.getGraspTierData().get(UserTierType.Gra.getTierName());
				if(grasp != null) {
					writeGraspTierData(writer, xmlMorType, grasp, UserTierType.Gra.getChatTierName().substring(1));
				}
			} else if(userTierType == UserTierType.Trn) {
				final GraspTierData grasp = oneToOne.getGraspTierData().get(UserTierType.Grt.getTierName());
				if(grasp != null) {
					writeGraspTierData(writer, xmlMorType, grasp, UserTierType.Grt.getChatTierName().substring(1));
				}
			}
		}
		return retVal;
	}

	private void writeGraspTierData(XmlSessionWriterV2_0 writer, XmlMorType xmlMorType, GraspTierData grasp, String type) {
		int graIdx = 0;
		for(XmlMorphemicBaseType morPre:xmlMorType.getMorPre()) {
			if(graIdx >= grasp.size()) break;
			final Grasp gra = grasp.get(graIdx++);
			final XmlGraType graType = writer.writeGra(new ObjectFactory(), gra, type);
			morPre.getGra().add(graType);
		}
		if(graIdx < grasp.size()) {
			final Grasp gra = grasp.get(graIdx++);
			final XmlGraType graType = writer.writeGra(new ObjectFactory(), gra, type);
			xmlMorType.getGra().add(graType);
			for (XmlMorphemicBaseType morPost : xmlMorType.getMorPost()) {
				if (graIdx >= grasp.size()) break;
				final Grasp gra1 = grasp.get(graIdx++);
				final XmlGraType graType1 = writer.writeGra(new ObjectFactory(), gra1, type);
				morPost.getGra().add(graType1);
			}
		}
	}

	@Override
    @Visits
	public void visitLinker(Linker linker) {
		final XmlLinkerType xmlLinker = factory.createXmlLinkerType();
		final XmlLinkerTypeType type = switch (linker.getType()) {
			case LAZY_OVERLAP_MARK -> XmlLinkerTypeType.LAZY_OVERLAP_MARK;
			case NO_BREAK_TCU_COMPLETION -> XmlLinkerTypeType.NO_BREAK_TCU_COMPLETION;
			case OTHER_COMPLETION -> XmlLinkerTypeType.OTHER_COMPLETION;
			case QUICK_UPTAKE -> XmlLinkerTypeType.QUICK_UPTAKE;
			case QUOTED_UTTERANCE_NEXT -> XmlLinkerTypeType.QUOTED_UTTERANCE_NEXT;
			case SELF_COMPLETION -> XmlLinkerTypeType.SELF_COMPLETION;
			case TECHNICAL_BREAK_TCU_COMPLETION -> XmlLinkerTypeType.TECHNICAL_BREAK_TCU_COMPLETION;
		};
		xmlLinker.setType(type);
		u.getLinker().add(xmlLinker);
	}

	@Override
    @Visits
	public void visitOrthoGroup(OrthoGroup group) {
		final XmlGroupType xmlG = factory.createXmlGroupType();
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
		final XmlPhoneticGroupType xmlPg = factory.createXmlPhoneticGroupType();
		final OrthoToXmlVisitor innnerGVisitor = new OrthoToXmlVisitor();
		phoneticGroup.getElements().forEach(innnerGVisitor::visit);
		innnerGVisitor.getU().getWOrGOrPg().forEach(xmlPg.getWOrGOrE()::add);
		u.getWOrGOrPg().add(xmlPg);
	}

	@Override
    @Visits
	public void visitQuotation(Quotation quotation) {
		final XmlQuotationType xmlQuotation = factory.createXmlQuotationType();
		final XmlBeginEndType beginEnd = switch (quotation.getBeginEnd()) {
			case BEGIN -> XmlBeginEndType.BEGIN;
			case END -> XmlBeginEndType.END;
		};
		xmlQuotation.setType(beginEnd);

		final OneToOne oneToOne = quotation.getExtension(OneToOne.class);
		if(oneToOne != null) {
			final List<XmlMorType> morTiers = writeMorTierData(oneToOne);
			for(XmlMorType mor:morTiers) {
				xmlQuotation.getMor().add(mor);
			}
		}

		u.getWOrGOrPg().add(xmlQuotation);
	}

	@Override
    @Visits
	public void visitPause(Pause pause) {
		final XmlPauseType xmlPause = factory.createXmlPauseType();
		final XmlPauseSymbolicLengthType type = switch (pause.getType()) {
			case SIMPLE -> XmlPauseSymbolicLengthType.SIMPLE;
			case LONG -> XmlPauseSymbolicLengthType.LONG;
			case VERY_LONG -> XmlPauseSymbolicLengthType.VERY_LONG;
			case NUMERIC -> null;
		};
		if(type == null) {
			xmlPause.setLength(BigDecimal.valueOf(pause.getLength()).setScale(3, RoundingMode.HALF_UP));
		} else {
			xmlPause.setSymbolicLength(type);
		}
		u.getWOrGOrPg().add(xmlPause);
	}

	@Override
    @Visits
	public void visitInternalMedia(InternalMedia internalMedia) {
		final XmlMediaType xmlMedia = factory.createXmlMediaType();
		xmlMedia.setUnit(XmlMediaUnitType.MS.MS);
		float startTime = internalMedia.getStartTime() * 1000.0f;
		float endTime = internalMedia.getEndTime() * 1000.0f;
		xmlMedia.setStart(BigDecimal.valueOf(startTime));
		xmlMedia.setEnd(BigDecimal.valueOf(endTime));
		u.getWOrGOrPg().add(xmlMedia);
	}

	@Override
    @Visits
	public void visitFreecode(Freecode freecode) {
		final XmlFreecodeType xmlFreecode = factory.createXmlFreecodeType();
		xmlFreecode.setValue(freecode.getCode());
		u.getWOrGOrPg().add(xmlFreecode);
	}

	public void visitEventAnnotations(Event event, XmlEventType xmlEvent) {
		final OrthoAnnotationToXmlVisitor annotationVisitor = new OrthoAnnotationToXmlVisitor(xmlEvent.getKOrErrorOrOverlap());
		event.getAnnotations().forEach(annotationVisitor::visit);
	}

	@Override
    @Visits
	public void visitAction(Action action) {
		final XmlEventType xmlEvent = factory.createXmlEventType();
		xmlEvent.setAction(factory.createXmlActionType());
		visitEventAnnotations(action, xmlEvent);
		u.getWOrGOrPg().add(xmlEvent);
	}

	@Override
    @Visits
	public void visitHappening(Happening happening) {
		final XmlEventType xmlEvent = factory.createXmlEventType();
		final XmlHappeningType happeningType = factory.createXmlHappeningType();
		happeningType.setValue(happening.getData());
		xmlEvent.setHappening(happeningType);
		visitEventAnnotations(happening, xmlEvent);
		u.getWOrGOrPg().add(xmlEvent);
	}

	@Override
    @Visits
	public void visitOtherSpokenEvent(OtherSpokenEvent otherSpokenEvent) {
		final XmlEventType xmlEvent = factory.createXmlEventType();
		final XmlOtherSpokenEventType xmlOte = factory.createXmlOtherSpokenEventType();
		xmlOte.setWho(otherSpokenEvent.getWho());
		xmlOte.setSaid(otherSpokenEvent.getData());
		xmlEvent.setOtherSpokenEvent(xmlOte);
		visitEventAnnotations(otherSpokenEvent, xmlEvent);
		u.getWOrGOrPg().add(xmlEvent);
	}

	@Override
    @Visits
	public void visitSeparator(Separator separator) {
		final XmlSeparatorType xmlS = factory.createXmlSeparatorType();
		final XmlSeparatorTypeType type = switch (separator.getType()) {
			case CLAUSE_DELIMITER -> XmlSeparatorTypeType.CLAUSE_DELIMITER;
			case COLON -> XmlSeparatorTypeType.COLON;
			case SEMICOLON -> XmlSeparatorTypeType.SEMICOLON;
			case UNMARKED_ENDING -> XmlSeparatorTypeType.UNMARKED_ENDING;
			case UPTAKE -> XmlSeparatorTypeType.UPTAKE;
		};
		xmlS.setType(type);
		u.getWOrGOrPg().add(xmlS);
	}

	@Override
    @Visits
	public void visitToneMarker(ToneMarker toneMarker) {
		final XmlToneMarkerType xmlToneMarker = factory.createXmlToneMarkerType();
		final XmlToneMarkerTypeType type = switch (toneMarker.getType()) {
			case FALLING_TO_LOW -> XmlToneMarkerTypeType.FALLING_TO_LOW;
			case FALLING_TO_MID -> XmlToneMarkerTypeType.FALLING_TO_MID;
			case LEVEL -> XmlToneMarkerTypeType.LEVEL;
			case RISING_TO_HIGH -> XmlToneMarkerTypeType.RISING_TO_HIGH;
			case RISING_TO_MID -> XmlToneMarkerTypeType.RISING_TO_MID;
		};
		xmlToneMarker.setType(type);
		u.getWOrGOrPg().add(xmlToneMarker);
	}

	@Override
    @Visits
	public void visitTagMarker(TagMarker tagMarker) {
		final XmlTagMarkerType xmlTagMarker = factory.createXmlTagMarkerType();
		final XmlTagMarkerTypeType type = switch (tagMarker.getType()) {
			case COMMA -> XmlTagMarkerTypeType.COMMA;
			case TAG -> XmlTagMarkerTypeType.TAG;
			case VOCATIVE -> XmlTagMarkerTypeType.VOCATIVE;
		};
		xmlTagMarker.setType(type);

		final OneToOne oneToOne = tagMarker.getExtension(OneToOne.class);
		if(oneToOne != null) {
			final List<XmlMorType> morTiers = writeMorTierData(oneToOne);
			for(XmlMorType mor:morTiers) {
				xmlTagMarker.getMor().add(mor);
			}
		}

		u.getWOrGOrPg().add(xmlTagMarker);
	}

	@Override
    @Visits
	public void visitOverlapPoint(OverlapPoint overlapPoint) {
		final XmlOverlapPointType xmlOverlapPt = factory.createXmlOverlapPointType();
		final XmlStartEndType startEnd = switch (overlapPoint.getType()) {
			case TOP_END,BOTTOM_END -> XmlStartEndType.END;
			case TOP_START,BOTTOM_START -> XmlStartEndType.START;
		};
		final XmlTopBottomType topBottom = switch (overlapPoint.getType()) {
			case TOP_START,TOP_END -> XmlTopBottomType.TOP;
			case BOTTOM_END,BOTTOM_START -> XmlTopBottomType.BOTTOM;
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
		final XmlUnderlineType xmlUnderline = factory.createXmlUnderlineType();
		final XmlBeginEndType beginEnd = switch (underline.getBeginEnd()) {
			case END -> XmlBeginEndType.END;
			case BEGIN -> XmlBeginEndType.BEGIN;
		};
		xmlUnderline.setType(beginEnd);
		u.getWOrGOrPg().add(xmlUnderline);
	}

	@Override
    @Visits
	public void visitItalic(Italic italic) {
		final XmlItalicType xmlItalic = factory.createXmlItalicType();
		final XmlBeginEndType beginEnd = switch (italic.getBeginEnd()) {
			case END -> XmlBeginEndType.END;
			case BEGIN -> XmlBeginEndType.BEGIN;
		};
		xmlItalic.setType(beginEnd);
		u.getWOrGOrPg().add(xmlItalic);
	}

	@Override
    @Visits
	public void visitLongFeature(LongFeature longFeature) {
		final XmlLongFeatureType xmlLongFeature = factory.createXmlLongFeatureType();
		final XmlBeginEndType beginEnd = switch (longFeature.getBeginEnd()) {
			case BEGIN -> XmlBeginEndType.BEGIN;
			case END -> XmlBeginEndType.END;
		};
		xmlLongFeature.setType(beginEnd);
		xmlLongFeature.setContent(longFeature.getLabel());
		u.getWOrGOrPg().add(xmlLongFeature);
	}

	@Override
    @Visits
	public void visitNonvocal(Nonvocal nonvocal) {
		final XmlNonvocalType xmlNonvocal = factory.createXmlNonvocalType();
		final XmlBeginEndSimpleType beginEndSimple = switch (nonvocal.getBeginEndSimple()) {
			case BEGIN -> XmlBeginEndSimpleType.BEGIN;
			case END -> XmlBeginEndSimpleType.END;
			case SIMPLE -> XmlBeginEndSimpleType.SIMPLE;
		};
		xmlNonvocal.setType(beginEndSimple);
		xmlNonvocal.setContent(nonvocal.getLabel());
		u.getWOrGOrPg().add(xmlNonvocal);
	}

	@Override
    @Visits
	public void visitTerminator(Terminator terminator) {
		final XmlUtteranceTerminatorType xmlT = factory.createXmlUtteranceTerminatorType();
		final XmlTerminatorType type = switch (terminator.getType()) {
			case BROKEN_FOR_CODING -> XmlTerminatorType.BROKEN_FOR_CODING;
			case EXCLAMATION -> XmlTerminatorType.E;
			case INTERRUPTION -> XmlTerminatorType.INTERRUPTION;
			case INTERRUPTION_QUESTION -> XmlTerminatorType.INTERRUPTION_QUESTION;
			case NO_BREAK_TCU_CONTINUATION -> XmlTerminatorType.NO_BREAK_TCU_CONTINUATION;
			case PERIOD -> XmlTerminatorType.P;
			case QUESTION -> XmlTerminatorType.Q;
			case QUESTION_EXCLAMATION -> XmlTerminatorType.QUESTION_EXCLAMATION;
			case QUOTATION_NEXT_LINE -> XmlTerminatorType.QUOTATION_NEXT_LINE;
			case QUOTATION_PRECEDES -> XmlTerminatorType.QUOTATION_PRECEDES;
			case SELF_INTERRUPTION -> XmlTerminatorType.SELF_INTERRUPTION;
			case SELF_INTERRUPTION_QUESTION -> XmlTerminatorType.SELF_INTERRUPTION_QUESTION;
			case TECHNICAL_BREAK_TCU_CONTINUATION -> XmlTerminatorType.TECHNICAL_BREAK_TCU_CONTINUATION;
			case TRAIL_OFF -> XmlTerminatorType.TRAIL_OFF;
			case TRAIL_OFF_QUESTION -> XmlTerminatorType.TRAIL_OFF_QUESTION;
		};
		xmlT.setType(type);

		final OneToOne oneToOne = terminator.getExtension(OneToOne.class);
		if(oneToOne != null) {
			final List<XmlMorType> morTiers = writeMorTierData(oneToOne);
			for(XmlMorType mor:morTiers) {
				xmlT.getMor().add(mor);
			}
		}

		u.setT(xmlT);
	}

	@Override
    @Visits
	public void visitPostcode(Postcode postcode) {
		final XmlPostcodeType xmlPostcode = factory.createXmlPostcodeType();
		xmlPostcode.setValue(postcode.getCode());
		u.getPostcode().add(xmlPostcode);
	}

	@Visits
	@Override
	public void visitUtteranceLanguage(UtteranceLanguage utteranceLanguage) {
		u.setLang(utteranceLanguage.getLanguage().toString());
	}

	@Visits
	public void visitError(Error error) {
		final XmlErrorType xmlError = factory.createXmlErrorType();
		if(error.getData() != null && !error.getData().isBlank())
			xmlError.setValue(error.getData());
		u.getKOrError().add(xmlError);
	}

	@Visits
	public void visitMarker(Marker marker) {
		final XmlMarkerType xmlMarker = factory.createXmlMarkerType();
		final XmlMarkerTypeType type = switch (marker.getType()) {
			case BEST_GUESS -> XmlMarkerTypeType.BEST_GUESS;
			case CONTRASTIVE_STRESSING -> XmlMarkerTypeType.CONTRASTIVE_STRESSING;
			case EXCLUDE -> XmlMarkerTypeType.MOR_EXCLUDE;
			case FALSE_START -> XmlMarkerTypeType.FALSE_START;
			case RETRACING -> XmlMarkerTypeType.RETRACING;
			case RETRACING_REFORMULATION -> XmlMarkerTypeType.RETRACING_REFORMULATION;
			case RETRACING_UNCLEAR -> XmlMarkerTypeType.RETRACING_UNCLEAR;
			case RETRACING_WITH_CORRECTION -> XmlMarkerTypeType.RETRACING_WITH_CORRECTION;
			case STRESSING -> XmlMarkerTypeType.STRESSING;
		};
		xmlMarker.setType(type);
		u.getKOrError().add(xmlMarker);
	}

	public XmlUtteranceType getU() {
		return this.u;
	}

	@Override
	public void fallbackVisit(OrthographyElement obj) {

	}

}
