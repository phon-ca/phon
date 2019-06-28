package ca.phon.app.session.editor.view.timegrid;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;

import ca.phon.app.media.TimeUIModel;
import ca.phon.app.media.WaveformDisplay;

public class TimegridWaveformTier extends TimeGridTier  {

	private static final long serialVersionUID = -2864344329017995791L;

	private WaveformDisplay wavDisplay;
	
	public TimegridWaveformTier(TimeGridView parent) {
		super(parent);
		
		init();
	}
	
	private void init() {
		final TimeUIModel timeModel = getParentView().getTimeModel();
		
		wavDisplay = new WaveformDisplay(timeModel);
		Insets channelInsets = new Insets(wavDisplay.getChannelInsets().top, timeModel.getTimeInsets().left,
				wavDisplay.getChannelInsets().bottom, timeModel.getTimeInsets().right);
		wavDisplay.setChannelInsets(channelInsets);
		wavDisplay.setTrackViewportHeight(true);
		wavDisplay.setBackground(Color.WHITE);
		wavDisplay.setOpaque(true);
		
		wavDisplay.getPreferredSize();
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(wavDisplay, BorderLayout.CENTER);
	}
	
	public WaveformDisplay getWaveformDisplay() {
		return this.wavDisplay;
	}
	
}
