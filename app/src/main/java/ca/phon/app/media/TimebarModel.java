package ca.phon.app.media;

import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class TimebarModel {
	
	public final static float MIN_PIXELS_PER_SECOND = 10.0f;
	public final static float MAX_PIXELS_PER_SECOND = 1000.0f;
	public final static float DEFAULT_PIXELS_PER_SECOND = 100.0f;
	
	private float pixelsPerSecond = DEFAULT_PIXELS_PER_SECOND;
	
	private Insets timeInsets = new Insets(0, 10, 0, 10);
	
	private float startTime = 0.0f;
	
	private float endTime = 0.0f;
	
	private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	public Insets getTimeInsets() {
		return timeInsets;
	}

	public void setTimeInsets(Insets timeInsets) {
		var oldVal = this.timeInsets;
		this.timeInsets = timeInsets;
		propSupport.firePropertyChange("timeInsets", oldVal, timeInsets);
	}

	public float getStartTime() {
		return startTime;
	}

	public void setStartTime(float startTime) {
		var oldVal = this.startTime;
		this.startTime = startTime;
		propSupport.firePropertyChange("startTime", oldVal, startTime);
	}

	public float getEndTime() {
		return endTime;
	}

	public void setEndTime(float endTime) {
		var oldVal = this.endTime;
		this.endTime = endTime;
		propSupport.firePropertyChange("endTime", oldVal, endTime);
	}

	public float getPixelsPerSecond() {
		return pixelsPerSecond;
	}

	public void setPixelsPerSecond(float pixelsPerSecond) {
		var oldVal = this.pixelsPerSecond;
		this.pixelsPerSecond = pixelsPerSecond;
		propSupport.firePropertyChange("pixelsPerSecond", oldVal, this.pixelsPerSecond);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propSupport.addPropertyChangeListener(propertyName, listener);
	}

	public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
		propSupport.removePropertyChangeListener(propertyName, listener);
	}

}
