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
package ca.phon.app.session.editor.autotranscribe;

import ca.phon.app.ipalookup.*;
import ca.phon.app.session.editor.EditorEventManager;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.*;
import ca.phon.app.session.editor.view.ipa_lookup.IPALookupEdit;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.*;
import ca.phon.ipa.alignment.*;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.orthography.OrthographyElement;
import ca.phon.session.Record;
import ca.phon.session.*;
import ca.phon.session.alignment.TierElementFilter;
import ca.phon.session.filter.RecordFilter;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.util.Tuple;

import javax.swing.undo.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for performing automatic IPA transcription for a {@link Record}.
 * 
 * 
 */
public class AutoTranscriber {

	private boolean setIPATarget = true;
	
	private boolean setIPAActual = true;
	
	private boolean overwrite = true;
	
	private RecordFilter recordFilter = null;
	
	private IPADictionary dictionary;
	
	private Syllabifier syllabifier;
	
	private Transcriber transcriber = null;

	private final Session session;

	private final EditorEventManager editorEventManager;

	public AutoTranscriber(Session session, EditorEventManager editorEventManager) {
		super();

		this.session = session;
		this.editorEventManager = editorEventManager;
	}

	public Session getSession() {
		return session;
	}

	public EditorEventManager getEditorEventManager() {
		return editorEventManager;
	}

	public void setTranscriber(Transcriber transcriber) {
		this.transcriber = transcriber;
	}
	
	public Transcriber getTranscriber() {
		return this.transcriber;
	}

	public boolean isSetIPATarget() {
		return setIPATarget;
	}

	public void setSetIPATarget(boolean setIPATarget) {
		this.setIPATarget = setIPATarget;
	}

	public boolean isSetIPAActual() {
		return setIPAActual;
	}

	public void setSetIPAActual(boolean setIPAActual) {
		this.setIPAActual = setIPAActual;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}

	public RecordFilter getRecordFilter() {
		return recordFilter;
	}

	public void setRecordFilter(RecordFilter recordFilter) {
		this.recordFilter = recordFilter;
	}

	public IPADictionary getDictionary() {
		return dictionary;
	}

	public void setDictionary(IPADictionary dictionary) {
		this.dictionary = dictionary;
	}
	
	public Syllabifier getSyllabifier() {
		return syllabifier;
	}

	public void setSyllabifier(Syllabifier syllabifier) {
		this.syllabifier = syllabifier;
	}

	private boolean isUnset(IPATranscript t) {
		boolean isEmpty = (t == null || t.length() == 0 || t.matches("\\*+"));
		return isEmpty;
	}
	
	/**
	 * Transcribe the given record.
	 * 
	 * @param record
	 *
	 * @return tuple of containing the automatic transcription for
	 *  IPA Target and IPA Actual respectively
	 */
	private Map<Tier<IPATranscript>, IPATranscript> transcribe(Record record) {
		final IPATranscriptBuilder ipaTBuilder = new IPATranscriptBuilder();
		final IPATranscriptBuilder ipaABuilder = new IPATranscriptBuilder();
		
		boolean ipaTUnset = isUnset(record.getIPATarget());
		boolean ipaTUnvalidated = 
				(record.getIPATarget().getExtension(UnvalidatedValue.class) != null
					&& record.getIPATarget().getExtension(UnvalidatedValue.class).getValue().length() > 0);
		
		boolean ipaAUnset = isUnset(record.getIPAActual());
		boolean ipaAUnvalidated = 
				(record.getIPAActual().getExtension(UnvalidatedValue.class) != null
					&& record.getIPAActual().getExtension(UnvalidatedValue.class).getValue().length() > 0);

		final List<OrthographyElement> orthoWords = TierElementFilter.orthographyFilterForIPAAlignment().filterOrthography(record.getOrthography());
		for(int i = 0; i < orthoWords.size(); i++) {
			final OrthographyElement ele = orthoWords.get(i);
			final OrthoLookupVisitor visitor = new OrthoLookupVisitor(getDictionary());
			visitor.visit(ele);

			final OrthoWordIPAOptions ipaExt =
					ele.getExtension(OrthoWordIPAOptions.class);
			if(ipaExt == null) continue;
			
			final List<String> ipaOpts = ipaExt.getOptions();
			final int selectedOption = 
					(ipaExt.getSelectedOption() >= 0 && ipaExt.getSelectedOption() < ipaOpts.size() ? 
							ipaExt.getSelectedOption() : 0);
			
			if(ipaOpts != null && ipaOpts.size() > 0) {
				if(ipaTBuilder.size() > 0) ipaTBuilder.appendWordBoundary();
				if(ipaABuilder.size() > 0) ipaABuilder.appendWordBoundary();

				ipaTBuilder.append(ipaOpts.get(selectedOption));
				ipaABuilder.append(ipaOpts.get(selectedOption));
			} else {
				ipaTBuilder.append("*");
				ipaABuilder.append("*");
			}
		}
		
		final IPATranscript ipaT = ipaTBuilder.toIPATranscript();
		final IPATranscript ipaA = ipaABuilder.toIPATranscript();
		if(getSyllabifier() != null) {
			getSyllabifier().syllabify(ipaT.toList());
			getSyllabifier().syllabify(ipaA.toList());
		}

		final Map<Tier<IPATranscript>, IPATranscript> retVal = new LinkedHashMap<>();
		retVal.put(record.getIPATargetTier(), (ipaTUnset && !ipaTUnvalidated) || isOverwrite() ? ipaT : null);
		retVal.put(record.getIPAActualTier(), (ipaAUnset && !ipaAUnvalidated) || isOverwrite() ? ipaA : null);
		return retVal;
	}

	private boolean isSetTier(Tier<IPATranscript> tier) {
		if(SystemTierType.IPATarget.getName().equals(tier.getName())) {
			return isSetIPATarget();
		} else if(SystemTierType.IPAActual.getName().equals(tier.getName())) {
			return isSetIPAActual();
		} else {
			return false;
		}
	}
	
	/**
	 * Transcribe the given record.
	 * 
	 * @param record
	 * 
	 * @return an undoable edit for the transciption
	 */
	public UndoableEdit transcribeRecord(Record record) {
		final CompoundEdit retVal = new CompoundEdit();
		final var autoTranscriptionMap = transcribe(record);
		for(Tier<IPATranscript> tier:autoTranscriptionMap.keySet()) {
			if(!isSetTier(tier)) continue;
			final IPATranscript newValue = autoTranscriptionMap.get(tier);
			final IPALookupEdit edit =
					new IPALookupEdit(getSession(), getEditorEventManager(), getTranscriber(), record, getDictionary(),
							record.getOrthographyTier().toString(), tier, newValue, false);
			edit.doIt();
			retVal.addEdit(edit);
		}
		retVal.end();
		return retVal;
	}
	
	/**
	 * Transcribe the given session.
	 * 
	 * @param session
	 * 
	 * @return the undoable edit for the transcription operation
	 */
	public UndoableEdit transcribeSession(Session session) {
		final CompoundEdit retVal = new CompoundEdit() {

			@Override
			public String getUndoPresentationName() {
				return "Undo automatic transcription";
			}

			@Override
			public String getRedoPresentationName() {
				return "Redo automatic transcription";
			}
			
		};
		
		for(int i = 0; i < session.getRecordCount(); i++) {
			final Record r = session.getRecord(i);
			boolean transcribeRecord = 
					(getRecordFilter() != null ? getRecordFilter().checkRecord(r) : true);
			if(transcribeRecord) {
				final UndoableEdit edit = transcribeRecord(r);
				retVal.addEdit(edit);
			}
		}
		retVal.end();
		
		return retVal;
	}
	
}
