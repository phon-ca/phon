package ca.phon.app.session.editor.view.segmentation.actions;

import java.awt.event.ActionEvent;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.undo.AddRecordEdit;
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
		// setup speaker
		utt.setSpeaker(speaker);
	
		// setup orthography
		utt.getOrthography().addGroup(new Orthography());
		utt.getSegment().setGroup(0, m);
		
		// should we replace segment for current record instead?
		SegmentationMode mode = (SegmentationMode)getSegmentationView().getSegmentationMode();
		if(mode == SegmentationMode.REPLACE_CURRENT) {
			utt = getEditor().currentRecord();
		}
	
		if(mode == SegmentationMode.REPLACE_CURRENT) {
			final TierEdit<MediaSegment> segEdit = new TierEdit<MediaSegment>(getEditor(), utt.getSegment(), 0, m);
			getEditor().getUndoSupport().postEdit(segEdit);
			
			// move to next record (if available)
			int idx = getEditor().getCurrentRecordIndex() + 1;
			if(idx < getEditor().getDataModel().getRecordCount())
				getEditor().setCurrentRecordIndex(idx);
		} else {
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
