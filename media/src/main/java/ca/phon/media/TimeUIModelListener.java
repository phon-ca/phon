package ca.phon.media;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import ca.phon.media.TimeUIModel.Interval;
import ca.phon.media.TimeUIModel.Marker;

public interface TimeUIModelListener extends EventListener {

	/**
	 * Same as addeding a {@link PropertyChangeListener}
	 * to the model.
	 * 
	 * @param e
	 */
	public void propertyChange(PropertyChangeEvent e);
	
	/**
	 * Called when an interval has been added to the model.
	 * 
	 * @param interval
	 */
	public void intervalAdded(Interval interval);
	
	/**
	 * Called when an interval has been removed from the model.
	 * 
	 * @param interval
	 */
	public void intervalRemoved(Interval interval);
	
	/**
	 * Called when a marker has been added to the model.
	 * 
	 * @param marker
	 */
	public void markerAdded(Marker marker);
	
	/**
	 * Called when a marker has been removed from the model.
	 * 
	 * @param marker
	 */
	public void markerRemoved(Marker marker);
	
}
