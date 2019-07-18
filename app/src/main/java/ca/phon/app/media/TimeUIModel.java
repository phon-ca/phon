package ca.phon.app.media;

import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

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
	
	/* Curent playback position */
	private float currentTime = 0.0f;
	
	private final Collection<Marker> pointMarkers = Collections.synchronizedList(new ArrayList<>());
	
	private final Collection<Interval> intervals = Collections.synchronizedList(new ArrayList<>());
	
	private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	/**
	 * Utility method to round time to nearest millisecond.
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
	
	public float getCurrentTime() {
		return this.currentTime;
	}
	
	public void setCurrentTime(float currentTime) {
		var oldVal = this.currentTime;
		this.currentTime = currentTime;
		propSupport.firePropertyChange("currentTime", oldVal, currentTime);
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
	
	public Marker addMarker(float time, Icon icon, String text) {
		Marker marker = new Marker(time);
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
	
	public Interval addInterval(float startTime, float endTime) {
		Interval interval = new Interval(startTime, endTime);
		addInterval(interval);
		return interval;
	}
	
	public void addInterval(Interval interval) {
		var intervalCount = intervals.size();
		intervals.add(interval);
		propSupport.firePropertyChange("intervalCount", intervalCount, intervals.size());
	}
	
	public Collection<Interval> getIntervals() {
		return Collections.unmodifiableCollection(this.intervals);
	}
	
	public void clearIntervals() {
		var intervalCount = intervals.size();
		intervals.clear();
		propSupport.firePropertyChange("intervalCount", intervalCount, intervals.size());
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
	
	public static class Marker {
		
		private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
		
		private float time;
		private Icon icon;
		private String text;
		
		public Marker(float startTime) {
			super();
			this.time = startTime;
			this.icon = null;
			this.text = "";
		}
		
		public float getTime() {
			return this.time;
		}
		
		public void setTime(float time) {
			var oldVal = this.time;
			this.time = time;
			propSupport.firePropertyChange("time", oldVal, time);
		}
		
		public Icon getIcon() {
			return this.icon;
		}
		
		public void setIcon(Icon icon) {
			var oldVal = this.icon;
			this.icon = icon;
			propSupport.firePropertyChange("icon", oldVal, icon);
		}
		
		public String getText() {
			return this.text;
		}
		
		public void setText(String text) {
			var oldVal = this.text;
			this.text = text;
			propSupport.firePropertyChange("text", oldVal, text);
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propSupport.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propSupport.removePropertyChangeListener(listener);
		}

		public PropertyChangeListener[] getPropertyChangeListeners() {
			return propSupport.getPropertyChangeListeners();
		}

		public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propSupport.addPropertyChangeListener(propertyName, listener);
		}

		public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propSupport.removePropertyChangeListener(propertyName, listener);
		}

		public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
			return propSupport.getPropertyChangeListeners(propertyName);
		}
		
	}
	
	public static class Interval {
		
		private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
		
		private final Marker startMarker;
		private final Marker endMarker;
		
		/* true when dragging markers */
		private volatile boolean valueAdjusting = false;
		
		public Interval() {
			this(0.0f, 0.0f);
		}
		
		public Interval(float startTime, float endTime) {
			super();
			
			this.startMarker = new Marker(startTime);
			this.startMarker.addPropertyChangeListener(new ForwardingPropertyChangeListener("startMarker.", propSupport));
			this.endMarker = new Marker(endTime);
			this.endMarker.addPropertyChangeListener(new ForwardingPropertyChangeListener("endMarker.", propSupport));
		}
		
		public boolean isValueAdjusting() {
			return this.valueAdjusting;
		}
		
		public void setValueAdjusting(boolean valueAdjusting) {
			var oldVal = this.valueAdjusting;
			this.valueAdjusting = valueAdjusting;
			propSupport.firePropertyChange("valueAdjusting", oldVal, valueAdjusting);
		}
		
		public Marker getStartMarker() {
			return this.startMarker;
		}
		
		public Marker getEndMarker() {
			return this.endMarker;
		}

		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propSupport.addPropertyChangeListener(listener);
		}

		public void removePropertyChangeListener(PropertyChangeListener listener) {
			propSupport.removePropertyChangeListener(listener);
		}

		public PropertyChangeListener[] getPropertyChangeListeners() {
			return propSupport.getPropertyChangeListeners();
		}

		public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propSupport.addPropertyChangeListener(propertyName, listener);
		}

		public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
			propSupport.removePropertyChangeListener(propertyName, listener);
		}

		public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
			return propSupport.getPropertyChangeListeners(propertyName);
		}
		
	}
	
	private static class ForwardingPropertyChangeListener implements PropertyChangeListener {

		private String prefix;
		
		private PropertyChangeSupport propSupport;
		
		public ForwardingPropertyChangeListener(String prefix, PropertyChangeSupport propSupport) {
			super();
			this.prefix = prefix;
			this.propSupport = propSupport;
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			propSupport.firePropertyChange(createFowardingEvent(evt));
		}
		
		private PropertyChangeEvent createFowardingEvent(PropertyChangeEvent evt) {
			return new PropertyChangeEvent(evt.getSource(), prefix + evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
		}
		
	}

}
