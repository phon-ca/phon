package ca.phon.media.sampled.actions;

import javax.swing.AbstractAction;

import ca.phon.media.sampled.PCMSegmentView;

public abstract class PCMSegmentViewAction extends AbstractAction {

	private static final long serialVersionUID = -1601105535567999442L;

	private final PCMSegmentView view;
	
	public PCMSegmentViewAction(PCMSegmentView view) {
		super();
		this.view = view;
	}

	public PCMSegmentView getView() {
		return this.view;
	}
	
}
