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
package ca.phon.app.session.editor.undo;

import ca.phon.app.session.editor.*;
import ca.phon.session.*;

import java.awt.*;

/**
 * {@link Session} edits involving participants.
 */
public class ParticipantUndoableEdit extends SessionEditorUndoableEdit {

	/**
	 * Participant involved in the edit
	 */
	private Participant participant;
	
	private Participant template;
	
	private Participant oldVals;
	
	public ParticipantUndoableEdit(SessionEditor editor, Participant participant, Participant template) {
		super(editor);
		this.participant = participant;
		this.template = template;
	}
	
	public Participant getParticipant() {
		return this.participant;
	}
	
	@Override
	public void undo() {
		Participants.copyParticipantInfo(oldVals, participant);

		final EditorEvent<Participant> ee =
				new EditorEvent<>(EditorEventType.ParticipantChanged, (Component) getSource(), participant);
		getEditor().getEventManager().queueEvent(ee);
	}

	@Override
	public void doIt() {
		final SessionFactory factory = SessionFactory.newFactory();
		final Participant p = factory.createParticipant();
		Participants.copyParticipantInfo(participant, p);
		oldVals = p;
		
		Participants.copyParticipantInfo(template, participant);

		final EditorEvent<Participant> ee =
				new EditorEvent<>(EditorEventType.ParticipantChanged, (Component) getSource(), participant);
		getEditor().getEventManager().queueEvent(ee);
	}
	
}
