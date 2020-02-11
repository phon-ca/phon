package ca.phon.app.media;

import java.awt.Color;
import java.awt.Component;
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
import javax.swing.event.EventListenerList;

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
	
	private float mediaEndTime = 0.0f;
	
	/* Curent playback position */
	private float currentTime = 0.0f;
	
	private final Collection<Marker> pointMarkers = Collections.synchronizedList(new ArrayList<>());
	
	private final Collection<Interval> intervals = Collections.synchronizedList(new ArrayList<>());
	
	private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	private final PropertyChangeListener propEventForwarder = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			for(TimeUIModelListener listener:getTimeUIModelListeners()) {
				listener.propertyChange(evt);
			}
		}
		
	};
	
	private final EventListenerList eventListeners = new EventListenerList();
	
	public TimeUIModel() {
		super();
		
		addPropertyChangeListener(propEventForwarder);
	}
	
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
	
	public float getMediaEndTime() {
		return this.mediaEndTime;
	}
	
	public void setMediaEndTime(float mediaEndTime) {
		var oldVal = this.mediaEndTime;
		this.mediaEndTime = mediaEndTime;
		propSupport.firePropertyChange("mediaEndTime", oldVal, mediaEndTime);
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
	
	public Marker addMarker(float time, Color color) {
		Marker marker = new Marker(time);
		marker.color = color;
		addMarker(marker);
		return marker;
	}
	
	public void addMarker(Marker marker) {
		if(marker == null) return;
		var oldVal = pointMarkers.size();
		pointMarkers.add(marker);
		propSupport.firePropertyChange("markerCount", oldVal, pointMarkers.size());
		fireMarkerAdded(marker);
	}
	
	public void removeMarker(Marker marker) {
		if(marker == null) return;
		var oldVal = pointMarkers.size();
		pointMarkers.remove(marker);
		propSupport.firePropertyChange("markerCount", oldVal, pointMarkers.size());
		fireMarkerRemoved(marker);
	}
	
	public Collection<Marker> getMarkers() {
		return Collections.unmodifiableCollection(this.pointMarkers);
	}
	
	public void clearMarkers() {
		var markerCount = pointMarkers.size();
		Collection<Marker> removed = new ArrayList<>(pointMarkers);
		pointMarkers.clear();
		propSupport.firePropertyChange("markerCount", markerCount, pointMarkers.size());
		removed.forEach(this::fireMarkerRemoved);
	}
	
	public Interval addInterval(float startTime, float endTime) {
		Interval interval = new Interval(startTime, endTime);
		addInterval(interval);
		return interval;
	}
	
	public Interval addInterval(Marker startMarker, Marker endMarker) {
		Interval interval = new Interval(startMarker, endMarker);
		addInterval(interval);
		return interval;
	}
	
	public void addInterval(Interval interval) {
		var intervalCount = intervals.size();
		intervals.add(interval);
		propSupport.firePropertyChange("intervalCount", intervalCount, intervals.size());
		fireIntervalAdded(interval);
	}
	
	public void removeInterval(Interval interval) {
		var intervalCount = intervals.size();
		intervals.remove(interval);
		propSupport.firePropertyChange("intervalCount", intervalCount, intervals.size());
		fireIntervalRemoved(interval);
	}
	
	public Collection<Interval> getIntervals() {
		return Collections.unmodifiableCollection(this.intervals);
	}
	
	public void clearIntervals() {
		var intervalCount = intervals.size();
		Collection<Interval> removed = new ArrayList<>(intervals);
		intervals.clear();
		propSupport.firePropertyChange("intervalCount", intervalCount, intervals.size());
		removed.forEach( this::fireIntervalRemoved );
	}
	
	public void addTimeUIModelListener(TimeUIModelListener listener) {
		eventListeners.add(TimeUIModelListener.class, listener);
	}
	
	public void removeTimeUIModelListener(TimeUIModelListener listener) {
		eventListeners.remove(TimeUIModelListener.class, listener);
	}
	
	public TimeUIModelListener[] getTimeUIModelListeners() {
		return eventListeners.getListeners(TimeUIModelListener.class);
	}
	
	public void fireIntervalAdded(Interval interval) {
		for(TimeUIModelListener listener:getTimeUIModelListeners()) {
			listener.intervalAdded(interval);
		}
	}
	
	public void fireIntervalRemoved(Interval interval) {
		for(TimeUIModelListener listener:getTimeUIModelListeners()) {
			listener.intervalRemoved(interval);
		}
	}
	
	public void fireMarkerAdded(Marker marker) {
		for(TimeUIModelListener listener:getTimeUIModelListeners()) {
			listener.markerAdded(marker);
		}
	}
	
	public void fireMarkerRemoved(Marker marker) {
		for(TimeUIModelListener listener:getTimeUIModelListeners()) {
			listener.markerRemoved(marker);
		}
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
		
		private boolean draggable = true;
		
		private boolean valueAdjusting = false;
		
		private Color color = Color.black;
		
		private boolean repaintOnTimeChange = true;
		
		private TimeComponent owner = null;
		
		public Marker(float startTime) {
			super();
			this.time = startTime;
		}
		
		public Marker(float startTime, Color color) {
			super();
			this.time = startTime;
			this.color = color;
		}
		
		public boolean isRepaintOnTimeChange() {
			return this.repaintOnTimeChange;
		}
		
		public void setRepaintOnTimeChange(boolean repaintOnTimeChange) {
			this.repaintOnTimeChange = repaintOnTimeChange;
		}
		
		public float getTime() {
			return this.time;
		}
		
		public void setTime(float time) {
			var oldVal = this.time;
			this.time = time;
			propSupport.firePropertyChange("time", oldVal, time);
		}

		public boolean isValueAdjusting() {
			return this.valueAdjusting;
		}
		
		public void setValueAdjusting(boolean valueAdjusting) {
			var oldVal = this.valueAdjusting;
			this.valueAdjusting = valueAdjusting;
			propSupport.firePropertyChange("valueAdjusting", oldVal, valueAdjusting);
		}
		
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			propSupport.addPropertyChangeListener(listener);
		}
		
		public boolean isDraggable() {
			return this.draggable;
		}
		
		public void setDraggable(boolean draggable) {
			var oldVal = this.draggable;
			this.draggable = draggable;
			propSupport.firePropertyChange("draggable", oldVal, draggable);
		}

		public Color getColor() {
			return this.color;
		}
		
		public void setColor(Color color) {
			var oldVal = this.color;
			this.color = color;
			propSupport.firePropertyChange("color", oldVal, color);
		}
		
		public TimeComponent getOwner() {
			return this.owner;
		}
		
		public void setOwner(TimeComponent owner) {
			var oldVal = this.owner;
			this.owner = owner;
			propSupport.firePropertyChange("owner", oldVal, owner);
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
		
		private Color color = new Color(255, 255, 255, 50);
		
		/* true when dragging markers */
		private volatile boolean valueAdjusting = false;
		
		/* If true, the entire interval will be repainted when values change */
		private boolean repaintEntireInterval = false;
		
		/* If false, the interval will not be repainted during event changes */
		private boolean repaintOnTimeChange = true;
		
		/* Owner, if not null interval will only be painted on owner component */
		private TimeComponent owner = null;
		
		public Interval() {
			this(0.0f, 0.0f);
		}
		
		public Interval(float startTime, float endTime) {
			this(new Marker(startTime), new Marker(endTime));
		}

		public Interval(Marker startMarker, Marker endMarker) {
			super();
			
			this.startMarker = startMarker;
			this.startMarker.addPropertyChangeListener(new ForwardingPropertyChangeListener(this, "startMarker.", propSupport));
			this.endMarker = endMarker;
			this.endMarker.addPropertyChangeListener(new ForwardingPropertyChangeListener(this, "endMarker.", propSupport));
		}
		
		public boolean isRepaintOnTimeChange() {
			return this.repaintOnTimeChange;
		}
		
		public void setRepaintOnTimeChange(boolean repaintOnTimeChange) {
			this.repaintOnTimeChange = repaintOnTimeChange;
		}
		
		public boolean isRepaintEntireInterval( ) {
			return this.repaintEntireInterval;
		}
		
		public void setRepaintEntireInterval(boolean repaintEntireInterval) {
			var oldVal = this.repaintEntireInterval;
			this.repaintEntireInterval = repaintEntireInterval;
			propSupport.firePropertyChange("repaintEntireInterval", oldVal, repaintEntireInterval);
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
		
		public Color getColor() {
			return this.color;
		}
		
		public void setColor(Color color) {
			var oldVal = this.color;
			this.color = color;
			propSupport.firePropertyChange("color", oldVal, color);
		}
		
		public TimeComponent getOwner() {
			return this.owner;
		}
		
		public void setOwner(TimeComponent owner) {
			var oldVal = this.owner;
			this.owner = owner;
			propSupport.firePropertyChange("owner", oldVal, owner);
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
		
		private Object sourceBean;
		
		private PropertyChangeSupport propSupport;
		
		public ForwardingPropertyChangeListener(Object sourceBean, String prefix, PropertyChangeSupport propSupport) {
			super();
			this.sourceBean = sourceBean;
			this.prefix = prefix;
			this.propSupport = propSupport;
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			propSupport.firePropertyChange(createFowardingEvent(evt));
		}
		
		private PropertyChangeEvent createFowardingEvent(PropertyChangeEvent evt) {
			return new PropertyChangeEvent(sourceBean, prefix + evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
		}
		
	}
	
}
