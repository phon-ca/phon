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

import java.util.*;

import ca.phon.extensions.*;
import ca.phon.ipa.*;
import ca.phon.ipa.parser.exceptions.*;
import ca.phon.plugin.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.syllabifier.*;
import ca.phon.syllable.*;
import ca.phon.util.*;

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
	public boolean checkSession(SessionValidator validator, Session session) {
		boolean modified = false;
		Syllabifier syllabifier = SyllabifierLibrary.getInstance().defaultSyllabifier();
		if(isResetSyllabification() && getSyllabifierLang() != null) {
			syllabifier = SyllabifierLibrary.getInstance().getSyllabifierForLanguage(getSyllabifierLang());
		}
		
		for(int i = 0; i < session.getRecordCount(); i++) {
			final Record r = session.getRecord(i);
			for(Tier<IPATranscript> tier:r.getTiersOfType(IPATranscript.class)) {
				for(int gIdx = 0; gIdx < r.numberOfGroups(); gIdx++) {
					final IPATranscript ipa = tier.getGroup(gIdx);
					final UnvalidatedValue uv = ipa.getExtension(UnvalidatedValue.class);
					if(uv != null) {
						// can we fix this issue?
						SessionQuickFix[] quickFixes = getQuickFixes(uv);
						
						// error in this transcription
						final ValidationEvent ve = new ValidationEvent(session, i, tier.getName(), gIdx,
								uv.getParseError().getMessage(), quickFixes);
						validator.fireValidationEvent(ve);
					} else {
						if(isResetSyllabification() && syllabifier != null) {
							String prev = ipa.toString(true);
							ipa.resetSyllabification();
							syllabifier.syllabify(ipa.toList());
							
							boolean changed = !prev.equals(ipa.toString(true));
							
							if(changed) {
								ValidationEvent evt = new ValidationEvent(session, i, tier.getName(), gIdx,
										String.format("Reset syllabification (%s)", syllabifier.getName()), new ResetSyllabificationQuickFix(syllabifier));
								validator.fireValidationEvent(evt);
							}
							
							modified |= changed;
						}
						
						// check syllabification, see if any elements are unassigned
						Optional<IPAElement> unknownEle = ipa.toList().stream()
							.filter( (ele) -> (new IPATranscript(ele)).matches("\\w") && ele.getScType() == SyllableConstituentType.UNKNOWN )
							.findFirst();
						if(unknownEle.isPresent()) {
							ValidationEvent evt = new ValidationEvent(session, i, tier.getName(), gIdx,
									String.format("Incomplete syllabification: %s", ipa.toString(true)), new ResetSyllabificationQuickFix(syllabifier));
							validator.fireValidationEvent(evt);
						}
					}
				}
			}
		}
		return modified;
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
			
			if(err2.getMessage().equals("Expecting new syllable")) {
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
			Record r = evt.getSession().getRecord(evt.getRecord());
			Group g = r.getGroup(evt.getGroup());
			IPATranscript ipa = g.getTier(evt.getTierName(), IPATranscript.class);
			syllabifier.syllabify(ipa.toList());
			
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
			Record r = evt.getSession().getRecord(evt.getRecord());
			Group g = r.getGroup(evt.getGroup());
			IPATranscript ipa = g.getTier(evt.getTierName(), IPATranscript.class);
			if(ipa.getExtension(UnvalidatedValue.class) == null) return false;
			
			var uv = ipa.getExtension(UnvalidatedValue.class);
			var err = (StrayDiacriticException)uv.getParseError().getSuppressed()[0];
			
			String txt = ipa.getExtension(UnvalidatedValue.class).getValue();
			var v = txt.substring(0, err.getPositionInLine()) + txt.substring(err.getPositionInLine()+1);
			var newIpa = (new IPATranscriptBuilder()).append(v).toIPATranscript();
			g.setTier(evt.getTierName(), IPATranscript.class, newIpa);
			
			return true;
		}
		
	}

}
