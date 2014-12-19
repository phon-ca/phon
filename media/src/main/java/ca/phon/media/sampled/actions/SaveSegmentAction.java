package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;

import ca.phon.media.sampled.PCMSegmentView;

public class SaveSegmentAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = -3688820556623096827L;

	public static final String TXT = "Save segment...";
	
	public static final String DESC = "Save segment to file";
	
	public SaveSegmentAction(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(getView().hasSegment())
			getView().saveSegment();
	}

}
