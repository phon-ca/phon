package ca.phon.app.session.editor.view.ipa_lookup;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

import ca.phon.alignment.Aligner;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.IPATranscriptBuilder;
import ca.phon.ipa.alignment.PhoneAligner;
import ca.phon.ipa.alignment.PhoneMap;
import ca.phon.ipadictionary.IPADictionary;
import ca.phon.orthography.OrthoElement;
import ca.phon.session.Group;
import ca.phon.session.Record;
import ca.phon.session.RecordFilter;
import ca.phon.session.Session;
import ca.phon.session.Word;
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
	
	public AutoTranscriber() {
		super();
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
		return (t == null || t.length() == 0 || t.matches("\\*+"));
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
		
		for(int i = 0; i < group.getAlignedWordCount(); i++) {
			final Word word = group.getAlignedWord(i);
			
			final OrthoElement orthoEle = word.getOrthography();
			final String[] ipaOpts = getDictionary().lookup(orthoEle.toString());
			
			final IPATranscript ipaT = word.getIPATarget();
			final IPATranscript ipaA = word.getIPAActual();
			
			if(ipaTBuilder.size() > 0) ipaTBuilder.appendWordBoundary();
			if(ipaABuilder.size() > 0) ipaABuilder.appendWordBoundary();
			
			if(isUnset(ipaT) || isOverwrite()) {
				if(ipaOpts.length > 0) {
					ipaTBuilder.append(ipaOpts[0]);
				} else {
					ipaTBuilder.append("*");
				}
			} else {
				if(isUnset(ipaT)) {
					ipaTBuilder.append("*");
				} else {
					ipaTBuilder.append(ipaT);
				}
			}
			
			if(isUnset(ipaA) || isOverwrite()) {
				if(ipaOpts.length > 0) {
					ipaABuilder.append(ipaOpts[0]);
				} else {
					ipaABuilder.append("*");
				}
			} else {
				if(isUnset(ipaA)) {
					ipaABuilder.append("*");
				} else {
					ipaABuilder.append(ipaA);
				}
			}
		}
		
		final IPATranscript ipaT = ipaTBuilder.toIPATranscript();
		final IPATranscript ipaA = ipaABuilder.toIPATranscript();
		if(getSyllabifier() != null) {
			getSyllabifier().syllabify(ipaT.toList());
			getSyllabifier().syllabify(ipaA.toList());
			
		}
		
		return new Tuple<IPATranscript, IPATranscript>(ipaT, ipaA);
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
			
			if(isSetIPATarget()) {
				final TierEdit<IPATranscript> targetEdit = 
						new TierEdit<IPATranscript>(null, record.getIPATarget(), i, autoTranscription.getObj1());
				targetEdit.doIt();
				retVal.addEdit(targetEdit);
			}
			
			if(isSetIPAActual()) {
				final TierEdit<IPATranscript> actualEdit = 
						new TierEdit<IPATranscript>(null, record.getIPAActual(), i, autoTranscription.getObj2());
				actualEdit.doIt();
				retVal.addEdit(actualEdit);
			}
			
			final PhoneAligner aligner = new PhoneAligner();
			final PhoneMap pm = aligner.calculatePhoneMap(autoTranscription.getObj1(), autoTranscription.getObj2());
			final TierEdit<PhoneMap> alignmentEdit = 
					new TierEdit<PhoneMap>(null, record.getPhoneAlignment(), i, pm);
			alignmentEdit.doIt();
			retVal.addEdit(alignmentEdit);
		}
		
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
		final CompoundEdit retVal = new CompoundEdit();
		
		for(int i = 0; i < session.getRecordCount(); i++) {
			final Record r = session.getRecord(i);
			
			boolean transcribeRecord = 
					(getRecordFilter() != null ? getRecordFilter().checkRecord(r) : true);
			if(transcribeRecord) {
				final UndoableEdit edit = transcribeRecord(r);
				retVal.addEdit(edit);
			}
		}
		
		return retVal;
	}
	
}
