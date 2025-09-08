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
import ca.phon.ipa.*;
import ca.phon.session.Session;
import ca.phon.session.Tier;
import ca.phon.session.Transcriber;
import ca.phon.session.Transcript;

public class ToggleDiphthongEdit extends SessionUndoableEdit {

	private int transcriptElementIndex;

	private Tier<IPATranscript> tier;

	private final int index1;

    private final int index2;

    private final Transcriber transcriber;

	public ToggleDiphthongEdit(SessionEditor editor, int transcriptElementIndex, Tier<IPATranscript> tier, int index1, int index2) {
		this(editor.getSession(), editor.getEventManager(), transcriptElementIndex, tier, index1, index2, Transcriber.VALIDATOR);
	}

	public ToggleDiphthongEdit(Session session, EditorEventManager editorEventManager, int transcriptElementIndex, Tier<IPATranscript> tier, int index1, int index2, Transcriber transcriber) {
		super(session, editorEventManager);
		this.transcriptElementIndex = transcriptElementIndex;
		this.tier = tier;
        this.index1 = index1;
        this.index2 = index2;
        this.transcriber = transcriber;
	}
	
	@Override
	public void undo() {
		super.redo();
	}

	@Override
	public void doIt() {
        final IPATranscript transcript = transcriber == Transcriber.VALIDATOR ?
                tier.getValue() : tier.getBlindTranscription(transcriber.getUsername());
        final IPAElementFactory factory = new IPAElementFactory();

        final Transcript.Element transcriptElement = getSession().getTranscript().getElementAt(transcriptElementIndex);
        if(transcriptElement.isRecord()) {
            if(index1 >= 0 && index1 < transcript.length() && index2 >= 0 && index2 < transcript.length()) {
                boolean setDiphthong = !transcript.elementAt(index1).isDiphthong();
                final IPATranscriptBuilder builder = new IPATranscriptBuilder();
                for(int i = 0; i < index1; i++) {
                    builder.append(transcript.elementAt(i));
                }
                final IPAElement firstElem = transcript.elementAt(index1);
                final SyllableInfo firstInfo = firstElem.syllableInfo();
                final SyllableInfo firstNewInfo = new SyllableInfo(
                    firstInfo.constituentType(),
                    setDiphthong,
                    firstInfo.stress(),
                    firstInfo.syllableIndex(),
                    firstInfo.segregated(),
                    firstInfo.sonority(),
                    firstInfo.sonorityDistance(),
                    firstInfo.tone()
                );
                builder.append(factory.cloneElementWithSyllableInfo(firstElem, firstNewInfo));
                for(int i = index1+1; i < index2; i++) {
                    builder.append(transcript.elementAt(i));
                }
                final IPAElement secondElem = transcript.elementAt(index2);
                final SyllableInfo secondInfo = secondElem.syllableInfo();
                final SyllableInfo secondNewInfo = new SyllableInfo(
                    secondInfo.constituentType(),
                    setDiphthong,
                    secondInfo.stress(),
                    secondInfo.syllableIndex(),
                    secondInfo.segregated(),
                    secondInfo.sonority(),
                    secondInfo.sonorityDistance(),
                    secondInfo.tone()
                );
                builder.append(factory.cloneElementWithSyllableInfo(secondElem, secondNewInfo));
                for(int i = index2+1; i < transcript.length(); i++) {
                    builder.append(transcript.elementAt(i));
                }

                final TierEdit<IPATranscript> innerEdit = new TierEdit<>(getSession(), getEditorEventManager(),
                    transcriber, transcriptElement.asRecord(), tier, builder.toIPATranscript(), false);
                innerEdit.doIt();

//                final EditorEvent<ScTypeEdit.ScEditData> ee =
//                        new EditorEvent<>(ScTypeEdit.ScEdit, getSource(),
//                                new ScTypeEdit.ScEditData(transcriptElementIndex, tier,
//                                        transcript, index, info.getConstituentType(), info.getConstituentType()));
//                getEditorEventManager().queueEvent(ee);
            }
        }
	}

}
