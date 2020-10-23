/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
package ca.phon.app.session.editor.view.ipa_validation;

import javax.swing.undo.*;

import ca.phon.app.session.editor.undo.*;
import ca.phon.ipa.*;
import ca.phon.session.*;
import ca.phon.session.Record;
import ca.phon.session.filter.*;
import ca.phon.syllabifier.*;
import ca.phon.util.*;

/**
 * Auto-validate ipa fields for a session.
 * 
 * How data is validated:
 * 
 *  - if only one user has a transcription entered that transcription will be used
 *  - if all transcriptions are the same, the transcription is used
 *  - if transcriptions are different the transcriptions from the preferred transcriber
 *    (by default the first transcriber in the list) is used
 *
 */
public class AutoValidateTask extends ca.phon.worker.PhonTask {
	
	/**
	 * Include ipa target?
	 */
	private boolean validateTarget = true;
	
	/**
	 * Include ipa actual?
	 */
	private boolean validateActual = true;
	
	/**
	 * Overwrite data?
	 */
	private boolean overwriteData = false;
	
	/**
	 * Session
	 */
	private Session session;
	
	/**
	 * Preferred transcriber
	 */
	private Transcriber preferredTranscriber = null;
	
	/**
	 * Utterance filter
	 * 
	 */
	private RecordFilter recordFilter = null;
	
	/**
	 * Undoable edit
	 */
	private UndoableEdit undoableEdit = null;
	
	public AutoValidateTask(Session t) {
		this(t, null);
	}
	
	public AutoValidateTask(Session t, Transcriber tr) {
		super("Auto validate session");
		this.session = t;
		this.preferredTranscriber = tr;
	}
	
	public Transcriber getPreferredTranscriber() {
		Transcriber retVal = preferredTranscriber;
		
		if(retVal == null && session != null) {
			if(session.getTranscriberCount() > 0) {
				retVal = session.getTranscriber(0);
				preferredTranscriber = retVal;
			}
		}
		
		return retVal;
	}

	public void setPreferredTranscriber(Transcriber t) {
		this.preferredTranscriber = t;
	}
	
	/**
	 * Validate the given tier for a record
	 */
	private UndoableEdit validateIPA(Tier<IPATranscript> tier) {
		final CompoundEdit retVal = new CompoundEdit();
		
		for(int i = 0; i < tier.numberOfGroups(); i++) {
			final IPATranscript grp = tier.getGroup(i);
			final AlternativeTranscript alts = grp.getExtension(AlternativeTranscript.class);
			
			if(alts == null) continue;
			
			if(!overwriteData && grp.length() > 0) {
					continue;
			}
			
			IPATranscript setV = null;
			for(Transcriber t:session.getTranscribers()) {
				final IPATranscript ipa = alts.get(t.getUsername());
				
				if(ipa != null && ipa.length() > 0) {
					if(setV == null) {
						setV = ipa;
					} else {
						if(!setV.toString().equals(ipa.toString())) {
							final IPATranscript alt = alts.get(getPreferredTranscriber().getUsername());
							if(alt != null) {
								setV = alt;
							}
							break;
						}
					}
				}
			}
			
			if(setV != null) {
				setV.putExtension(AlternativeTranscript.class, alts);
				final SyllabifierInfo info = session.getExtension(SyllabifierInfo.class);
				final SyllabifierLibrary syllabifierLibrary = SyllabifierLibrary.getInstance();
				final Language lang = (info != null && info.getSyllabifierLanguageForTier(tier.getName()) != null ?
						info.getSyllabifierLanguageForTier(tier.getName()) : syllabifierLibrary.defaultSyllabifierLanguage());
				final Syllabifier syllabifier = syllabifierLibrary.getSyllabifierForLanguage(lang);
				if(syllabifier != null) {
					syllabifier.syllabify(setV.toList());
				}
				
				final TierEdit<IPATranscript> tierEdit = new TierEdit<IPATranscript>(null, tier, i, setV);
				tierEdit.doIt();
				retVal.addEdit(tierEdit);
			}
		}
		
		retVal.end();
		return retVal;
	}
	
	@Override
	public void performTask() {
		super.setStatus(TaskStatus.RUNNING);
		final CompoundEdit undoableEdit = new CompoundEdit();
		
		for(Record utt:session.getRecords()) {
			if(recordFilter != null && recordFilter.checkRecord(utt)) {
				if(validateTarget) {
					undoableEdit.addEdit(validateIPA(utt.getIPATarget()));
				}
				if(validateActual) {
					undoableEdit.addEdit(validateIPA(utt.getIPAActual()));
				}
			}
		}
		
		undoableEdit.end();
		this.undoableEdit = undoableEdit;
		super.setStatus(TaskStatus.FINISHED);
	}
	
	public UndoableEdit getUndoableEdit() {
		return this.undoableEdit;
	}

	public boolean isValidateTarget() {
		return validateTarget;
	}

	public void setValidateTarget(boolean validateTarget) {
		this.validateTarget = validateTarget;
	}

	public boolean isValidateActual() {
		return validateActual;
	}

	public void setValidateActual(boolean validateActual) {
		this.validateActual = validateActual;
	}

	public boolean isOverwriteData() {
		return overwriteData;
	}

	public void setOverwriteData(boolean overwriteData) {
		this.overwriteData = overwriteData;
	}

	public RecordFilter getRecordFilter() {
		return recordFilter;
	}

	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}

}
