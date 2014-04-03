package ca.phon.app.session.editor.view.segmentation.actions;

import javax.swing.AbstractAction;

import ca.phon.app.session.editor.view.segmentation.SegmentationEditorView;

public abstract class SegmentationViewAction extends AbstractAction {

	private static final long serialVersionUID = -3403785044379049607L;

	private final SegmentationEditorView segmentationView;
	
	public SegmentationViewAction(SegmentationEditorView view) {
		super();
		this.segmentationView = view;
	}
	
	public SegmentationEditorView getSegmentationView() {
		return this.segmentationView;
	}

}
