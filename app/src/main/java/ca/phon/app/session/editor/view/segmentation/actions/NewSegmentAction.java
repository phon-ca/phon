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
package ca.phon.app.session.editor.view.segmentation.actions;

import java.awt.event.ActionEvent;

import javax.swing.undo.CompoundEdit;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
import ca.phon.app.session.editor.undo.ChangeSpeakerEdit;
import ca.phon.app.session.editor.undo.TierEdit;
import ca.phon.app.session.editor.view.segmentation.SegmentationEditorView;
import ca.phon.app.session.editor.view.segmentation.SegmentationEditorView.SegmentationMode;
import ca.phon.orthography.Orthography;
import ca.phon.session.MediaSegment;
import ca.phon.session.Participant;
import ca.phon.session.Record;
import ca.phon.session.SessionFactory;

public class NewSegmentAction extends SegmentationViewAction {

	private static final long serialVersionUID = 593003680580794252L;

	private final Participant speaker;

	public NewSegmentAction(SessionEditor editor, SegmentationEditorView view, Participant speaker) {
		super(editor, view);
		this.speaker = speaker;
	}

	@Override
	public void hookableActionPerformed(ActionEvent e) {
		final MediaSegment m = getSegmentationView().getCurrentSegment();

		// should we create a new record or overwrite
		// the data in the curent?
		final SessionFactory factory = SessionFactory.newFactory();
		Record utt = factory.createRecord();

		// setup orthography
		utt.getOrthography().addGroup(new Orthography());
		utt.getSegment().setGroup(0, m);

		// should we replace segment for current record instead?
		SegmentationMode mode = (SegmentationMode)getSegmentationView().getSegmentationMode();
		if(mode == SegmentationMode.REPLACE_CURRENT && getEditor().currentRecord() == null) {
			// switch to 'add record' mode
			mode = SegmentationMode.INSERT_AT_END;
		}

		if(mode == SegmentationMode.REPLACE_CURRENT) {
			final CompoundEdit edit = new CompoundEdit() {

				@Override
				public String getUndoPresentationName() {
					return "Undo replace segment";
				}

				@Override
				public String getRedoPresentationName() {
					return "Redo replace segment";
				}

			};

			// don't replace speaker if no speaker was defined
			if(this.speaker != null) {
				final ChangeSpeakerEdit speakerEdit = new ChangeSpeakerEdit(getEditor(), getEditor().currentRecord(), this.speaker);
				speakerEdit.doIt();
				edit.addEdit(speakerEdit);
			}

			final TierEdit<MediaSegment> segEdit = new TierEdit<MediaSegment>(getEditor(), getEditor().currentRecord().getSegment(), 0, m);
			segEdit.doIt();
			edit.addEdit(segEdit);

			edit.end();
			getEditor().getUndoSupport().postEdit(edit);

			// move to next record (if available)
			int idx = getEditor().getCurrentRecordIndex() + 1;
			if(idx >= getEditor().getDataModel().getRecordCount()) {
				final AddRecordEdit addRecordEdit = new AddRecordEdit(getEditor());
				getEditor().getUndoSupport().postEdit(addRecordEdit);
			}
			getEditor().setCurrentRecordIndex(idx);

		} else {
			// setup speaker
			utt.setSpeaker(speaker);

			int idx = getEditor().getDataModel().getRecordCount();
			// where are we going to insert
			if(mode == SegmentationMode.INSERT_AFTER_CURRENT) {
				idx = (getEditor().getSession().getRecordCount() == 0 ? 0 : getEditor().getCurrentRecordIndex() + 1);
			}
			final AddRecordEdit edit = new AddRecordEdit(getEditor(), utt, idx);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}
