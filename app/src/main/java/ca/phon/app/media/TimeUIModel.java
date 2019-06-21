package ca.phon.app.media;

import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.Icon;

/**
 * Time model for UI applications.  Useful for time based media.
 * 
 */
public class TimeUIModel {
	
	public final static float MIN_PIXELS_PER_SECOND = 10.0f;
	public final static float MAX_PIXELS_PER_SECOND = 1000.0f;
	public final static float DEFAULT_PIXELS_PER_SECOND = 100.0f;
	
	private float pixelsPerSecond = DEFAULT_PIXELS_PER_SECOND;
	
	private Insets timeInsets = new Insets(0, 10, 0, 10);
	
	private float startTime = 0.0f;
	
	private float endTime = 0.0f;
	
	private final Collection<Marker> pointMarkers = Collections.synchronizedList(new ArrayList<>());
	
	private final Collection<Interval> intervals = Collections.synchronizedList(new ArrayList<>());
	
	private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	/**
	 * Utility method to round time to nearest milisecond.
	 * 
	 * @param time
	 * @return roundedTime
	 */
	public static double roundTime(double time) {
		try {
			final double rounded = (new BigDecimal(Double.toString(time))
				.setScale(3, RoundingMode.HALF_UP))
				.doubleValue();
			return rounded == 0d ? 0d * time : rounded;
		} catch (NumberFormatException ex) {
			if (Double.isInfinite(time)) {
				return time;
			} else {
				return Double.NaN;
			}
		}
	}

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
	
	/**
	 * Returns the preferred UI width of the component based on current time values
	 *
	 * @return
	 */
	public int getPreferredWidth() {
		int prefWidth =
				 (getTimeInsets().left+ getTimeInsets().right) + 
						((int)Math.round( (getEndTime() - getStartTime()) * getPixelsPerSecond()) );
		return prefWidth;
	}
	
	public float timeAtX(double x) {
		if(x <= getTimeInsets().left) return getStartTime();
		if(x >= (getPreferredWidth() - getTimeInsets().right)) return getEndTime();
		double time = getStartTime() + ((x - getTimeInsets().left) / getPixelsPerSecond());
		
		return (float)TimeUIModel.roundTime(time);
	}
	
	public double xForTime(float time) {
		return ( getPixelsPerSecond() * (time - getStartTime()) ) + getTimeInsets().left;
	}
	
//	public Marker addMarker(float time) {
//		addMarker(time, DEFAULT_MARKER_ICON);
//	}
//	
//	public Marker addMaker(float time, Icon icon) {
//		addMarker(time, icon, "");
//	}
	
	public Marker addMaker(float time, Icon icon, String text) {
		Marker marker = new Marker();
		marker.time = time;
		marker.icon = icon;
		marker.text = text;
		addMarker(marker);
		return marker;
	}
	
	public void addMarker(Marker marker) {
		pointMarkers.add(marker);
	}
	
	public void removeMarker(Marker marker) {
		pointMarkers.remove(marker);
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
	
	public class Marker {
		public float time;
		public Icon icon;
		public String text;
	}
	
	public class Interval {
		public Marker startMarker;
		public Marker endMarker;
	}

}
