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
package ca.phon.session.check;

import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.*;
import ca.phon.ipa.parser.exceptions.StrayDiacriticException;
import ca.phon.orthography.Orthography;
import ca.phon.orthography.OrthographyBuilder;
import ca.phon.orthography.Terminator;
import ca.phon.orthography.TerminatorType;
import ca.phon.plugin.*;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.syllabifier.*;
import ca.phon.syllable.SyllableConstituentType;
import ca.phon.util.PrefHelper;

import java.util.*;

/**
 * Check IPA transcriptions for a session. 
 *
 */
@PhonPlugin(name="Check IPA Transcriptions", comments="Check IPA transcriptions and optionally reset syllabification")
@Rank(1)
public class CheckTranscripts implements SessionCheck, IPluginExtensionPoint<SessionCheck> {
	
	public final static String RESET_SYLLABIFICATION = CheckTranscripts.class.getName() + ".resetSyllabification";
	public final static boolean DEFAULT_RESET_SYLLABIFICATION = false;
	private boolean resetSyllabification = 
			PrefHelper.getBoolean(RESET_SYLLABIFICATION, DEFAULT_RESET_SYLLABIFICATION);
	
	public final static String SYLLABIFIER_LANG = CheckTranscripts.class.getName() + ".syllabifierLang";
	public final static String DEFAULT_SYLLABIFIER_LANG = SyllabifierLibrary.getInstance().defaultSyllabifierLanguage().toString();
	private String syllabifierLang = PrefHelper.get(SYLLABIFIER_LANG, DEFAULT_SYLLABIFIER_LANG);
	
	public CheckTranscripts() {
		super();
	}
	
	public boolean isResetSyllabification() {
		return resetSyllabification;
	}

	public void setResetSyllabification(boolean resetSyllabification) {
		this.resetSyllabification = resetSyllabification;
	}

	public String getSyllabifierLang() {
		return syllabifierLang;
	}

	public void setSyllabifierLang(String syllabifierLang) {
		this.syllabifierLang = syllabifierLang;
	}

	@Override
	public boolean performCheckByDefault() {
		return true;
	}

	@Override
	public boolean checkSession(SessionValidator validator, Session session) {
		boolean modified = false;
		Syllabifier syllabifier = SyllabifierLibrary.getInstance().defaultSyllabifier();
		if(isResetSyllabification() && getSyllabifierLang() != null) {
			syllabifier = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(getSyllabifierLang());
		}

		for(int eleIdx = 0; eleIdx < session.getTranscript().getNumberOfElements(); eleIdx++) {
			final Transcript.Element transcriptElement = session.getTranscript().getElementAt(eleIdx);
			if(transcriptElement.isRecord()) {
				final Record record = transcriptElement.asRecord();
				checkRecord(validator, session, eleIdx, record);
			} else if(transcriptElement.isGem()) {
				final Gem gem = transcriptElement.asGem();
				checkGem(validator, session, eleIdx, gem);
			} else if(transcriptElement.isComment()) {
				final Comment comment = transcriptElement.asComment();
				checkComment(validator, session, eleIdx, comment);
			}
		}

		return modified;
	}

	private void checkRecord(SessionValidator validator, Session session, int eleIdx, Record record) {
		if(record.getSpeaker() == Participant.UNKNOWN) {
			validator.fireValidationEvent(new ValidationEvent(ValidationEvent.Severity.WARNING, session, eleIdx, "Record", "Speaker is unidentified"));
		}

		if(record.getOrthographyTier().isUnvalidated()) {
			validator.fireValidationEvent(new ValidationEvent(ValidationEvent.Severity.WARNING, session, eleIdx, SystemTierType.Orthography.getName(),
				record.getOrthographyTier().getUnvalidatedValue().getParseError().getMessage()));
			return;
		} else {
			if (record.getOrthography() == null || record.getOrthography().length() == 0) {
				final SessionQuickFix quickFix = new SessionQuickFix() {
					@Override
					public boolean fix(ValidationEvent evt) {
						final OrthographyBuilder builder = new OrthographyBuilder();
						builder.append("xxx");
						builder.append(new Terminator(TerminatorType.PERIOD));
						record.setOrthography(builder.toOrthography());
						return true;
					}
				};
				validator.fireValidationEvent(new ValidationEvent(ValidationEvent.Severity.ERROR, session, eleIdx, SystemTierType.Orthography.getName(),
					"Orthography is blank", quickFix));
			} else {
				// check for terminator
				if(record.getOrthography().getTerminator() == null) {
					final SessionQuickFix quickFix = new SessionQuickFix() {
						@Override
						public boolean fix(ValidationEvent evt) {
							final OrthographyBuilder builder = new OrthographyBuilder();
							builder.append(record.getOrthography());
							builder.append(new Terminator(TerminatorType.PERIOD));
							record.setOrthography(builder.toOrthography());
							return true;
						}
					};
					validator.fireValidationEvent(new ValidationEvent(ValidationEvent.Severity.WARNING, session, eleIdx, SystemTierType.Orthography.getName(),
						"Orthography does not end with a terminator", quickFix));
				}
			}
		}

		// check default ipa tiers
		if(record.getIPATargetTier().isUnvalidated()) {
			validator.fireValidationEvent(new ValidationEvent(ValidationEvent.Severity.WARNING, session, eleIdx, SystemTierType.IPATarget.getName(),
				record.getIPATargetTier().getUnvalidatedValue().getParseError().getMessage()));
		}

		if(record.getIPAActualTier().isUnvalidated()) {
			validator.fireValidationEvent(new ValidationEvent(ValidationEvent.Severity.WARNING, session, eleIdx, SystemTierType.IPAActual.getName(),
				record.getIPAActualTier().getUnvalidatedValue().getParseError().getMessage()));
		}
	}

	private void checkGem(SessionValidator validator, Session session, int eleIdx, Gem gem) {
		if(gem.getLabel() == null || gem.getLabel().isBlank()) {
			validator.fireValidationEvent(new ValidationEvent(ValidationEvent.Severity.WARNING, session, eleIdx, "Gem", "Gem label is blank"));
		}
	}

	private void checkComment(SessionValidator validator, Session session, int eleIdx, Comment comment) {
		if(comment.getExtension(UnvalidatedValue.class) != null) {
			var uv = comment.getExtension(UnvalidatedValue.class);
			validator.fireValidationEvent(new ValidationEvent(ValidationEvent.Severity.ERROR, session, eleIdx, "Comment", uv.getParseError().getMessage()));
		} else {
			if(comment.getValue().toString().isBlank()) {
				validator.fireValidationEvent(new ValidationEvent(ValidationEvent.Severity.WARNING, session, eleIdx, "Comment", "Comment is blank"));
			}
		}
	}

	@Override
	public Class<?> getExtensionType() {
		return SessionCheck.class;
	}

	@Override
	public IPluginExtensionFactory<SessionCheck> getFactory() {
		return (Object ... args) -> this;
	}

	@Override
	public Properties getProperties() {
		Properties props = new Properties();
		
		props.put(RESET_SYLLABIFICATION, Boolean.toString(isResetSyllabification()));
		props.put(SYLLABIFIER_LANG, getSyllabifierLang());
		
		return props;
	}

	@Override
	public void loadProperties(Properties props) {
		if(props.containsKey(RESET_SYLLABIFICATION))
			setResetSyllabification(Boolean.parseBoolean(props.getProperty(RESET_SYLLABIFICATION)));
		setSyllabifierLang(props.getProperty(SYLLABIFIER_LANG, DEFAULT_SYLLABIFIER_LANG));
	}
	
	public SessionQuickFix[] getQuickFixes(UnvalidatedValue uv) {
		var err = uv.getParseError();
		if(err.getSuppressed().length > 0) {
			var err2 = uv.getParseError().getSuppressed()[0];
			
			if(err2.getMessage() != null && err2.getMessage().equals("Expecting new syllable")) {
				return new SessionQuickFix[] { new DuplicateSyllableBoundaryQuickFix() };
			}
		}
		
		return new SessionQuickFix[0];
	}
	
	public class ResetSyllabificationQuickFix extends SessionQuickFix {
		
		private Syllabifier syllabifier;
		
		public ResetSyllabificationQuickFix(Syllabifier syllabifier) {
			super();
			this.syllabifier = syllabifier;
		}
		
		@Override
		public String getDescription() {
			return "Reset syllabification";
		}

		@Override
		public boolean fix(ValidationEvent evt) {
//			Record r = evt.getSession().getRecord(evt.getRecord());
//			Group g = r.getGroup(evt.getGroup());
//			IPATranscript ipa = g.getTier(evt.getTierName(), IPATranscript.class);
//			syllabifier.syllabify(ipa.toList());
			
			return true;
		}
		
	}
	
	public static class DuplicateSyllableBoundaryQuickFix extends SessionQuickFix {
		
		public DuplicateSyllableBoundaryQuickFix() {
			super();
		}
		
		@Override
		public String getDescription() {
			return "Reset syllabification";
		}

		@Override
		public boolean fix(ValidationEvent evt) {
//			Record r = evt.getSession().getRecord(evt.getRecord());
//			Group g = r.getGroup(evt.getGroup());
//			IPATranscript ipa = g.getTier(evt.getTierName(), IPATranscript.class);
//			if(ipa.getExtension(UnvalidatedValue.class) == null) return false;
//
//			var uv = ipa.getExtension(UnvalidatedValue.class);
//			var err = (StrayDiacriticException)uv.getParseError().getSuppressed()[0];
//
//			String txt = ipa.getExtension(UnvalidatedValue.class).getValue();
//			var v = txt.substring(0, err.getPositionInLine()) + txt.substring(err.getPositionInLine()+1);
//			var newIpa = (new IPATranscriptBuilder()).append(v).toIPATranscript();
//			g.setTier(evt.getTierName(), IPATranscript.class, newIpa);
			
			return true;
		}
		
	}

}
