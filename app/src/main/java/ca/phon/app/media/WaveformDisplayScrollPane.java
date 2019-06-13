package ca.phon.app.media;

import java.awt.Dimension;
import java.awt.Insets;
import java.beans.PropertyChangeListener;

import javax.swing.JScrollPane;

/**
 * Scroll pane for waveform displays providing a timeline and
 * channel actions.
 * 
 */
public class WaveformDisplayScrollPane extends JScrollPane {
	
	private Timebar timebar;
	
	private WaveformDisplay display;
	
	public WaveformDisplayScrollPane(WaveformDisplay display) {
		super(display);
		
		this.display = display;
		
		timebar = new Timebar();
		setColumnHeaderView(timebar);
		setupTimebar();
		
		display.addPropertyChangeListener(propListener);
	}
	
	private void setupTimebar() {
		timebar.setTimeInsets(new Insets(0, display.getChannelInsets().left, 0, display.getChannelInsets().right));
		
		if(display.getLongSound() != null) {
			timebar.setStartTime(display.getStartTime());
			timebar.setEndTime(display.getEndTime());
		} else {
			timebar.setStartTime(0.0f);
			timebar.setEndTime(0.0f);
		}
		timebar.revalidate();
		timebar.repaint();
	}
	
	private final PropertyChangeListener propListener = (e) -> {
		if(e.getPropertyName().equals("longSound")) {
			setupTimebar();
		}
	};

}
