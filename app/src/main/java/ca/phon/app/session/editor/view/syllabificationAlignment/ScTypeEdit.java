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
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.ipa.*;
import ca.phon.session.*;
import ca.phon.session.Record;

public class ScTypeEdit extends TierEdit<IPATranscript> {

    public record ScEditData(Record record, Tier<IPATranscript> tier, IPATranscript ipa, int eleIdx, SyllableConstituentType oldType, SyllableConstituentType newType) { }
    public final static EditorEventType<ScEditData> ScEdit = new EditorEventType<>(EditorEventName.MODIFICATION_EVENT + "_SC_TYPE_", ScEditData.class);

    private final int index;

	private final SyllableConstituentType scType;

	private SyllableConstituentType prevScType;

	public ScTypeEdit(SessionEditor editor, Transcriber transcriber, Tier<IPATranscript> tier, Record record, IPATranscript transcript, int index, SyllableConstituentType scType) {
		this(editor.getSession(), editor.getEventManager(), transcriber, record, tier, transcript, index, scType);
	}

	public ScTypeEdit(Session session, EditorEventManager eventManager, Transcriber transcriber, Record record, Tier<IPATranscript> tier, IPATranscript transcript, int index, SyllableConstituentType scType) {
		super(session, eventManager, transcriber, record, tier, transcript, false);
		this.index = index;
		this.scType = scType;
	}

	@Override
	public void undo() {
        super.undo();
        final EditorEvent<ScEditData> ee =
                new EditorEvent<>(ScEdit, getSource(),
                        new ScEditData(getRecord(), getTier(), getTier().getValue(), index, scType, prevScType));
        getEditorEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
        super.doIt();
        final EditorEvent<ScEditData> ee =
                new EditorEvent<>(ScEdit, getSource(),
                        new ScEditData(getRecord(), getTier(), getTier().getValue(), index, prevScType, scType));
        getEditorEventManager().queueEvent(ee);
	}

	@Override
	public String getPresentationName() {
		return "Change syllable constituent type";
	}
}
