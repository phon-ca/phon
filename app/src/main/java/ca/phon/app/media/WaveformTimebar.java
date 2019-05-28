package ca.phon.app.media;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;

public class WaveformTimebar extends JComponent {

	private final int DEFAULT_SMALL_TICK_HEIGHT = 10;
	private final int DEFUALT_LARGE_TICK_HEIGHT = 20;
	
	private WaveformDisplay display;
	
	public WaveformTimebar(WaveformDisplay display) {
		super();
		
		this.display = display;
	}
	
	public WaveformDisplay getDisplay() {
		return this.display;
	}
	
	public Dimension getPreferredSize() {
		Dimension retVal = new Dimension();
		retVal.width = display.getWidth();
		retVal.height = HEIGHT;
		return retVal;
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	
		
	}
	
}
