package ca.phon.app.media;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

/**
 * Base class for components which display information on a horizontal timeline
 *
 */
public abstract class TimeComponent extends JComponent {
	
	private static final long serialVersionUID = 1L;
	
	private Color selectionColor = new Color(50, 125, 200, 50);
	public static final String SELECTION_COLOR_PROP = "selectionColor";

	private TimeUIModel timeModel;
		
	public TimeComponent() {
		this(new TimeUIModel());
	}
	
	public TimeComponent(TimeUIModel timeModel) {
		super();
		
		this.timeModel = timeModel;
		this.timeModel.addPropertyChangeListener(modelPropListener);
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
	
	public Rectangle rectForInterval(float startTime, float endTime) {
		var startX = (int)Math.round(xForTime(startTime)) - 1;
		var endX = (int)Math.round(xForTime(endTime)) + 1;
		
		var clipRect = new Rectangle(startX, 0, endX-startX, getHeight());
		
		return clipRect;
	}
	
	public Color getSelectionColor() {
		return this.selectionColor;
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
		super.repaint(clipRect);
	}
	
	private final PropertyChangeListener modelPropListener = new PropertyChangeListener() {
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			// forward event to property change listeners for component
			TimeComponent.this.firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
		}
		
	};
	
}
