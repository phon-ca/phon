package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;

import ca.phon.media.sampled.PCMSegmentView;

public class PlaySegmentAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = 7689540383090206277L;

	public final static String TXT = "Play segment";
	
	public final static String DESC = "Play current segement";
	
	public PlaySegmentAction(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		getView().playSegment();
	}

}
