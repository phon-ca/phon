/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.media;

import ca.phon.media.TimeUIModel.*;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import java.awt.*;
import java.beans.*;

/**
 * Base class for components which display information on a horizontal timeline
 *
 */
public abstract class TimeComponent extends JComponent {
	
	private static final long serialVersionUID = 1L;
	
	private Color selectionColor = new Color(50, 125, 200, 50);
	public static final String SELECTION_COLOR_PROP = "selectionColor";

	private TimeUIModel timeModel;
	
	private Cursor defaultCursor = Cursor.getDefaultCursor();
		
	public TimeComponent() {
		this(new TimeUIModel());
	}
	
	public TimeComponent(TimeUIModel timeModel) {
		super();
		
		this.timeModel = timeModel;
		this.timeModel.addPropertyChangeListener(modelPropListener);
		
		setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	}
	
	public Cursor getDefaultCursor() {
		return this.defaultCursor;
	}
	
	public void setDefaultCursor(Cursor cursor) {
		var oldVal = this.defaultCursor;
		this.defaultCursor = cursor;
		super.firePropertyChange("defaultCursor", oldVal, cursor);
	}
	
	@Override
	public void setUI(ComponentUI ui) {
		if(!(ui instanceof TimeComponentUI))
			throw new IllegalArgumentException("ui must extends TimeComponentUI");
		super.setUI(ui);
	}
	
	@Override
	public TimeComponentUI getUI() {
		return TimeComponentUI.class.cast(super.getUI());
	}
	
	public TimeUIModel getTimeModel() {
		return this.timeModel;
	}
	
	public void setTimeModel(TimeUIModel timeModel) {
		var oldVal = this.timeModel;
		this.timeModel.removePropertyChangeListener(modelPropListener);
		this.timeModel = timeModel;
		this.timeModel.addPropertyChangeListener(modelPropListener);
		super.firePropertyChange("timeModel", oldVal, timeModel);
	}
	
	/* Time model delegate methods */
	public float getStartTime() {
		return timeModel.getStartTime();
	}

	public void setStartTime(float startTime) {
		timeModel.setStartTime(startTime);
	}

	public float getEndTime() {
		return timeModel.getEndTime();
	}

	public void setEndTime(float endTime) {
		timeModel.setEndTime(endTime);
	}

	public float getPixelsPerSecond() {
		return timeModel.getPixelsPerSecond();
	}

	public void setPixelsPerSecond(float pixelsPerSecond) {
		timeModel.setPixelsPerSecond(pixelsPerSecond);
	}

	public float timeAtX(double x) {
		return timeModel.timeAtX(x);
	}

	public double xForTime(float time) {
		return timeModel.xForTime(time);
	}
	
	/**
	 * Return the start time in our visible rectangle
	 * 
	 * @return first visible time in s
	 */
	public float getWindowStart() {
		return getTimeModel().timeAtX(getWindowStartX());
	}
	
	/**
	 * Returns the last time visible in our visible rectangle
	 * 
	 * @return final visible time in s
	 */
	public float getWindowEnd() {
		return getTimeModel().timeAtX(getWindowEndX());
	}

	/**
	 * Get x position of first visible start time
	 * 
	 * @return
	 */
	public double getWindowStartX() {
		return Math.max(timeModel.getTimeInsets().left , getVisibleRect().getX());
	}
	
	/**
	 * Get x position of last visible time
	 * 
	 * @return
	 */
	public double getWindowEndX() {
		return Math.min(getWidth() - timeModel.getTimeInsets().right, getVisibleRect().getMaxX());
	}
	
	/**
	 * Get visible window length in seconds
	 * 
	 * @return
	 */
	public float getWindowLength() {
		return getWindowEnd() - getWindowStart();
	}
	
	
	public Rectangle rectForInterval(float startTime, float endTime) {
		var startX = (int)Math.floor(xForTime(startTime));
		var endX = (int)Math.ceil(xForTime(endTime));
		
		var clipRect = new Rectangle(startX, 0, endX-startX, getHeight());
		
		return clipRect;
	}
	
	public Color getSelectionColor() {
		return this.selectionColor;
	}
	
	/**
	 * Flag for repaint events on intervals which do not belong to this component.
	 * If <code>true</code> this component will receive all repaint events for intervals
	 * registered with the same {@link TimeUIModel} 
	 * 
	 * @return <code>true</code> if repaints all interval events, <code>false</code> otherwise
	 */
	public boolean isRepaintAll() {
		return false;
	}
		
	/**
	 * Repaint rectangle between given time values
	 * @param startTime
	 * @param endTime
	 */
	public void repaint(float startTime, float endTime) {
		if(endTime < startTime)
			throw new IllegalArgumentException("end time < start time");
		
		var clipRect = rectForInterval(startTime, endTime);
		super.repaint(Math.max(0, clipRect.x - 1), clipRect.y, clipRect.width + 2, clipRect.height);
	}
	
	/**
	 * Repaint rectangle between given time values
	 * 
	 * @param tn
	 * @param startTime
	 * @param endTime
	 */
	public void repaint(long tn, float startTime, float endTime) {
		if(endTime < startTime)
			throw new IllegalArgumentException("end time < start time");
		
		var clipRect = rectForInterval(startTime, endTime);
		super.repaint(tn, Math.max(0, clipRect.x - 2), clipRect.y, clipRect.width + 4, clipRect.height);
	}
	
	public void repaintInterval(Interval interval) {
		repaint(interval.getStartMarker().getTime(), interval.getEndMarker().getTime());
	}
	
	public void repaintInterval(long tn, Interval interval) {
		repaint(tn, interval.getStartMarker().getTime(), interval.getEndMarker().getTime());
	}
	
	public void repaintMarker(Marker marker) {
		var markerX = xForTime(marker.getTime());
		
		Rectangle clipRect = new Rectangle(
				(int)Math.max(0, markerX - 1), 0, 3, getHeight());
		super.repaint(clipRect.x, clipRect.y, clipRect.width, clipRect.height);
	}
	
	public void repaintMarker(long tn, Marker marker) {
		var markerX = xForTime(marker.getTime());
		
		Rectangle clipRect = new Rectangle(
				(int)Math.max(0, markerX - 1), 0, 3, getHeight());
		super.repaint(tn, clipRect.x, clipRect.y, clipRect.width, clipRect.height);
	}
	
	private final PropertyChangeListener modelPropListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// forward event to property change listeners for component
			TimeComponent.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
		}
		
	};
	
}
