package ca.phon.media;

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
		
		timebar = new Timebar(display.getTimeModel());
		setColumnHeaderView(timebar);
	}

}
