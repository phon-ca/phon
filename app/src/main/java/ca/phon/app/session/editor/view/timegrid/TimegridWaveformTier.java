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
		final TimeUIModel timebarModel = getParentView().getTimebarModel();
		
		wavDisplay = new WaveformDisplay();
		Insets channelInsets = new Insets(wavDisplay.getChannelInsets().top, timebarModel.getTimeInsets().left,
				wavDisplay.getChannelInsets().bottom, timebarModel.getTimeInsets().right);
		wavDisplay.setChannelInsets(channelInsets);
		wavDisplay.setBackground(Color.WHITE);
		wavDisplay.setOpaque(true);
		
		timebarModel.addPropertyChangeListener("pixelsPerSecond", (e) -> {
			wavDisplay.setPixelsPerSecond((float)e.getNewValue());
			revalidate();
		});
				
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(wavDisplay, BorderLayout.CENTER);
	}
	
	public WaveformDisplay getWaveformDisplay() {
		return this.wavDisplay;
	}
	
}
