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
package ca.phon.app.session.editor.view.ipa_lookup;

import java.util.List;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import ca.phon.app.ipalookup.OrthoLookupVisitor;
import ca.phon.app.ipalookup.OrthoWordIPAOptions;
import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.BlindTierEdit;
import ca.phon.app.session.editor.undo.SessionEditorUndoableEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.extensions.UnvalidatedValue;
import ca.phon.ipa.AlternativeTranscript;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.orthography.OrthoElement;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.Session;
import ca.phon.session.SystemTierType;
import ca.phon.session.Transcriber;
import ca.phon.session.Word;
import ca.phon.session.filter.RecordFilter;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.util.Tuple;

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
	
	private final SessionEditor editor;
	
	public AutoTranscriber(SessionEditor editor) {
		super();
		this.editor = editor;
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
	
	public SessionEditor getEditor() {
		return this.editor;
	}
	
	/**
	 * Transcribe the given group.
	 * 
	 * @param group
	 * 
	 * @return tuple of containing the automatic transcription for
	 *  IPA Target and IPA Actual respectively
	 */
	private Tuple<IPATranscript, IPATranscript> transcribeGroup(Group group) {
		final IPATranscriptBuilder ipaTBuilder = new IPATranscriptBuilder();
		final IPATranscriptBuilder ipaABuilder = new IPATranscriptBuilder();
		
		boolean ipaTUnset = isUnset(group.getIPATarget());
		boolean ipaTUnvalidated = 
				(group.getIPATarget().getExtension(UnvalidatedValue.class) != null
					&& group.getIPATarget().getExtension(UnvalidatedValue.class).getValue().length() > 0);
		
		boolean ipaAUnset = isUnset(group.getIPAActual());
		boolean ipaAUnvalidated = 
				(group.getIPAActual().getExtension(UnvalidatedValue.class) != null
					&& group.getIPAActual().getExtension(UnvalidatedValue.class).getValue().length() > 0);
		
		for(int i = 0; i < group.getWordCount(SystemTierType.Orthography.getName()); i++) {
			final Word word = group.getAlignedWord(i);
			
			final OrthoElement orthoEle = word.getOrthography();
			final OrthoLookupVisitor visitor = new OrthoLookupVisitor(getDictionary());
			visitor.visit(orthoEle);
			final OrthoWordIPAOptions ipaExt =
					orthoEle.getExtension(OrthoWordIPAOptions.class);
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
		
		return new Tuple<IPATranscript, IPATranscript>(
				(ipaTUnset && !ipaTUnvalidated) || isOverwrite() ? ipaT : null, 
				(ipaAUnset && !ipaAUnvalidated) || isOverwrite() ? ipaA : null);
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
		
		for(int i = 0; i < record.numberOfGroups(); i++) {
			final Group g = record.getGroup(i);
			final Tuple<IPATranscript, IPATranscript> autoTranscription = 
					transcribeGroup(g);
			
			if(isSetIPATarget() && autoTranscription.getObj1() != null) {
				SessionEditorUndoableEdit targetEdit = null;
				if(getTranscriber() != null) {
					IPATranscript grpVal = (g.getIPATarget() != null ? g.getIPATarget() : new IPATranscript());
					targetEdit = 
							new BlindTierEdit(getEditor(), record.getIPATarget(), i, getTranscriber(), 
									autoTranscription.getObj1(), grpVal);
				} else {
					IPATranscript currentValue = 
							(record.getIPATarget().numberOfGroups() > i ? 
									record.getIPATarget().getGroup(i) : new IPATranscript());
					IPATranscript newValue = autoTranscription.getObj1();
					
					AlternativeTranscript alts = 
							(currentValue != null ? currentValue.getExtension(AlternativeTranscript.class) : null);
					if(alts != null) newValue.putExtension(AlternativeTranscript.class, alts);
					
					targetEdit = 
							new TierEdit<IPATranscript>(getEditor(), record.getIPATarget(), i, 
									newValue);
				}
				targetEdit.doIt();
				retVal.addEdit(targetEdit);
			}
			
			if(isSetIPAActual() && autoTranscription.getObj2() != null) {
				SessionEditorUndoableEdit actualEdit = null;
				if(getTranscriber() != null) {
					IPATranscript grpVal = (g.getIPAActual() != null ? g.getIPAActual() : new IPATranscript());
					actualEdit = 
							new BlindTierEdit(getEditor(), record.getIPAActual(), i, getTranscriber(),
									autoTranscription.getObj2(), grpVal);
				} else {
					IPATranscript currentValue = 
							(record.getIPAActual().numberOfGroups() > i ?
									record.getIPAActual().getGroup(i) : new IPATranscript());
					IPATranscript newValue = autoTranscription.getObj2();
					
					AlternativeTranscript alts =
							(currentValue != null ? currentValue.getExtension(AlternativeTranscript.class) : null);
					if(alts != null) newValue.putExtension(AlternativeTranscript.class, alts);
					
					actualEdit = 
							new TierEdit<IPATranscript>(getEditor(), record.getIPAActual(), i, 
									newValue);
				}
				actualEdit.doIt();
				retVal.addEdit(actualEdit);
			}
			
			if(getTranscriber() == null) {
				final PhoneAligner aligner = new PhoneAligner();
				final PhoneMap pm = aligner.calculatePhoneAlignment(g.getIPATarget(), g.getIPAActual());
				final TierEdit<PhoneMap> alignmentEdit = 
						new TierEdit<PhoneMap>(getEditor(), record.getPhoneAlignment(), i, pm);
				alignmentEdit.setFireHardChangeOnUndo(true);
				alignmentEdit.doIt();
				retVal.addEdit(alignmentEdit);
			}
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
