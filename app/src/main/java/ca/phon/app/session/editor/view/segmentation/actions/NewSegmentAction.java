/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015 The Phon Project, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
		final MediaSegment m = getSegmentationView().getCurrentSegement();
			
		// should we create a new record or overwrite
		// the data in the curent?
		final SessionFactory factory = SessionFactory.newFactory();
		Record utt = factory.createRecord();
		
		// setup orthography
		utt.getOrthography().addGroup(new Orthography());
		utt.getSegment().setGroup(0, m);
		
		// should we replace segment for current record instead?
		SegmentationMode mode = (SegmentationMode)getSegmentationView().getSegmentationMode();
		if(mode == SegmentationMode.REPLACE_CURRENT) {
			utt = getEditor().currentRecord();
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
			
			final ChangeSpeakerEdit speakerEdit = new ChangeSpeakerEdit(getEditor(), utt, speaker);
			speakerEdit.doIt();
			edit.addEdit(speakerEdit);
			
			final TierEdit<MediaSegment> segEdit = new TierEdit<MediaSegment>(getEditor(), utt.getSegment(), 0, m);
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
				idx = getEditor().getCurrentRecordIndex() + 1;
			}
			final AddRecordEdit edit = new AddRecordEdit(getEditor(), utt, idx);
			getEditor().getUndoSupport().postEdit(edit);
		}
	}

}
