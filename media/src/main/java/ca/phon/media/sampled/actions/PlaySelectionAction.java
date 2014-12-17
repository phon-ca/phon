package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;

import ca.phon.media.sampled.PCMSegmentView;

public class PlaySelectionAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = -7273435862236125102L;

	public final static String TXT = "Play selection";
	
	public final static String DESC = "Play current selection";
	
	public PlaySelectionAction(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getView().playSelection();
	}

}
