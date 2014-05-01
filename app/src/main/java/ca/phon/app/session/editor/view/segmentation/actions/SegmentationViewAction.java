package ca.phon.app.session.editor.view.segmentation.actions;

import javax.swing.AbstractAction;

import ca.phon.app.session.editor.SessionEditor;
import ca.phon.app.session.editor.actions.SessionEditorAction;
import ca.phon.app.session.editor.view.segmentation.SegmentationEditorView;

public abstract class SegmentationViewAction extends SessionEditorAction {

	private static final long serialVersionUID = -3403785044379049607L;

	private final SegmentationEditorView segmentationView;
	
	public SegmentationViewAction(SessionEditor editor, SegmentationEditorView view) {
		super(editor);
		this.segmentationView = view;
	}
	
	public SegmentationEditorView getSegmentationView() {
		return this.segmentationView;
	}

}
