package ca.phon.media.sampled.actions;

import java.awt.event.ActionEvent;

import ca.phon.media.sampled.PCMSegmentView;

public class ToggleLoop extends PCMSegmentViewAction {

	private static final long serialVersionUID = 3781234856731539939L;

	public final static String TXT = "Loop";
	
	public final static String DESC = "Loop playback";
	
	public ToggleLoop(PCMSegmentView view) {
		super(view);
		
		putValue(NAME, TXT);
		putValue(SHORT_DESCRIPTION, DESC);
	}
	
	@Override
	public Object getValue(String key) {
		if(key.equals(SELECTED_KEY)) {
			return getView().isLoop();
		} else {
			return super.getValue(key);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		boolean loop = getView().isLoop();
		getView().setLoop(!loop);
	}

}
