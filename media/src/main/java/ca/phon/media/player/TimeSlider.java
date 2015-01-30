package ca.phon.media.player;

import javax.swing.JSlider;

public class TimeSlider extends JSlider {

	private static final long serialVersionUID = -3955798002058380628L;
	
	public TimeSlider() {
		super();
		
		setUI(new TimeSliderUI());
	}
	
}
