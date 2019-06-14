package ca.phon.app.media;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

public class Timebar extends JComponent {

	public final static int DEFAULT_SMALL_TICK_HEIGHT = 5;
	public final static int DEFUALT_LARGE_TICK_HEIGHT = 8;
	
	private int smallTickHeight = DEFAULT_SMALL_TICK_HEIGHT;
	private int largeTickHeight = DEFUALT_LARGE_TICK_HEIGHT;
	
	public final static float MIN_PIXELS_PER_SECOND = 10.0f;
	public final static float MAX_PIXELS_PER_SECOND = 1000.0f;
	public final static float DEFAULT_PIXELS_PER_SECOND = 100.0f;
	
	private float pixelsPerSecond = DEFAULT_PIXELS_PER_SECOND;
	
	private Insets timeInsets = new Insets(0, 0, 0, 0);
	
	private float startTime = 0.0f;
	
	private float endTime = 0.0f;
	
	private final static String uiClassId = "TimebarUI";
	
	public Timebar() {
		super();
		
		updateUI();
	}
	
	public String getUIClassID() {
		return uiClassId;
	}

	@Override
	protected void setUI(ComponentUI newUI) {
		super.setUI(newUI);
	}

	@Override
	public void updateUI() {
		setUI(new DefaultTimebarUI());
	}

	public DefaultTimebarUI getUI() {
		return (DefaultTimebarUI)ui;
	}
	
	public Insets getTimeInsets() {
		return timeInsets;
	}

	public void setTimeInsets(Insets timeInsets) {
		this.timeInsets = timeInsets;
	}

	public float getStartTime() {
		return startTime;
	}

	public void setStartTime(float startTime) {
		this.startTime = startTime;
	}

	public float getEndTime() {
		return endTime;
	}

	public void setEndTime(float endTime) {
		this.endTime = endTime;
	}

	public int getSmallTickHeight() {
		return smallTickHeight;
	}

	public void setSmallTickHeight(int smallTickHeight) {
		this.smallTickHeight = smallTickHeight;
	}

	public int getLargeTickHeight() {
		return largeTickHeight;
	}

	public void setLargeTickHeight(int largeTickHeight) {
		this.largeTickHeight = largeTickHeight;
	}

	public float getPixelsPerSecond() {
		return pixelsPerSecond;
	}

	public void setPixelsPerSecond(float pixelsPerSecond) {
		this.pixelsPerSecond = pixelsPerSecond;
	}

	public float timeAtX(int x) {
		if(x <= getTimeInsets().left) return getStartTime();
		if(x >= (getWidth() - getTimeInsets().right)) return getEndTime();
		return getStartTime() + ((x - getTimeInsets().left) / getPixelsPerSecond());
	}
	
	public int xForTime(float time) {
		return (int)Math.round( getPixelsPerSecond() * (time - getStartTime()) ) + getTimeInsets().left;
	}
	
}
