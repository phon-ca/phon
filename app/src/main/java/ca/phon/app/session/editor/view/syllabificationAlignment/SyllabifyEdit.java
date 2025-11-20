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
package ca.phon.app.session.editor.view.syllabificationAlignment;

import ca.phon.app.session.editor.*;
import ca.phon.app.session.editor.undo.SessionUndoableEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.ipa.IPATranscript;
import ca.phon.ipa.SyllableConstituentType;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.session.Transcript;
import ca.phon.syllabifier.Syllabifier;
import ca.phon.syllable.*;

/**
 * Reset syllabification info on an IPA tier using a given {@link Syllabifier}.
 */
public class SyllabifyEdit extends SessionUndoableEdit {

	private final int transcriptElementIndex;

	private final Tier<IPATranscript> tier;
	
	private final Syllabifier syllabifier;
	
	private IPATranscript oldVal = null;

	private final Transcriber transcriber;

    private TierEdit<IPATranscript> innerEdit = null;
	
	public SyllabifyEdit(SessionEditor editor, int transcriptElementIndex, Tier<IPATranscript> ipaTier, Syllabifier syllabifier) {
		this(editor.getSession(), editor.getEventManager(), transcriptElementIndex, ipaTier, syllabifier, Transcriber.VALIDATOR);
	}

	public SyllabifyEdit(SessionEditor editor, int transcriptElementIndex, Tier<IPATranscript> ipaTier, Syllabifier syllabifier, Transcriber transcriber) {
		this(editor.getSession(), editor.getEventManager(), transcriptElementIndex, ipaTier, syllabifier, transcriber);
	}

	public SyllabifyEdit(Session session, EditorEventManager eventManager, int transcriptElementIndex, Tier<IPATranscript> ipaTier, Syllabifier syllabifier, Transcriber transcriber) {
		super(session, eventManager);
		this.transcriptElementIndex = transcriptElementIndex;
		this.tier = ipaTier;
		this.syllabifier = syllabifier;
		this.transcriber = transcriber;
	}

	@Override
	public void undo() {
		if(oldVal == null || innerEdit == null) return;
        final Transcript.Element transcriptEle = getSession().getTranscript().getElementAt(transcriptElementIndex);
        if(transcriptEle.isRecord()) {
            innerEdit.undo();

            final EditorEvent<ScTypeEdit.ScEditData> ee =
                    new EditorEvent<>(ScTypeEdit.ScEdit, getSource(),
                            new ScTypeEdit.ScEditData(transcriptEle.asRecord(), tier, oldVal, -1, SyllableConstituentType.UNKNOWN, SyllableConstituentType.UNKNOWN));
            getEditorEventManager().queueEvent(ee);
        }
	}

	@Override
	public void doIt() {
		IPATranscript ipa = tier.getValueForTranscriber(transcriber).orElse(new IPATranscript());
		oldVal = ipa;

        ipa = ipa.resetSyllabification();
		ipa = syllabifier.syllabify(ipa);

        final Transcript.Element transcriptEle = getSession().getTranscript().getElementAt(transcriptElementIndex);
        if(transcriptEle.isRecord()) {
            this.innerEdit = new TierEdit<>(getSession(), getEditorEventManager(), transcriber, transcriptEle.asRecord(), tier, ipa, false);
            this.innerEdit.doIt();

            final EditorEvent<ScTypeEdit.ScEditData> ee =
                    new EditorEvent<>(ScTypeEdit.ScEdit, getSource(),
                            new ScTypeEdit.ScEditData(transcriptEle.asRecord(), tier, ipa, -1, SyllableConstituentType.UNKNOWN, SyllableConstituentType.UNKNOWN));
            getEditorEventManager().queueEvent(ee);
        }
	}
	
}
