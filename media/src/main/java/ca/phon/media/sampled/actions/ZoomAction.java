/*
 * 
 */
package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;

import ca.phon.media.sampled.PCMSegmentView;

public class ZoomAction extends PCMSegmentViewAction {

	private static final long serialVersionUID = 4112458751805094501L;

	private final String TXT = "Zoom";
	
	private final int amount;
	
	public ZoomAction(PCMSegmentView view, int amount) {
		super(view);

		this.amount = amount;
		
		putValue(NAME, TXT);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final PCMSegmentView view = getView();
		float windowLength = view.getWindowLength() + amount;
		
		if(windowLength < 0.1f) {
			windowLength = 0.1f;
		} else if(windowLength > view.getSampled().getLength()) {
			windowLength = view.getSampled().getLength();
		}
		
		if((view.getWindowStart()+windowLength) > (view.getSampled().getStartTime() + view.getSampled().getLength())) {
			float newStart = (view.getSampled().getStartTime()+view.getSampled().getLength()) - windowLength;
			
			if(newStart < view.getSampled().getStartTime()) {
				newStart = view.getSampled().getStartTime();
				windowLength = view.getSampled().getLength();
			}
			view.setWindowStart(newStart);
		}
		
		view.setWindowLength(windowLength);
	}

}
