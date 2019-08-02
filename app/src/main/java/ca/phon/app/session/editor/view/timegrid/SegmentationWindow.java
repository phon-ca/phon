package ca.phon.app.session.editor.view.timegrid;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * 
 */
public class SegmentationWindow {

	private long backwardWindowLengthMs = 0L;
	
	private long fowardWindowLengthMs = 100L;

	public long segmentStartLockMs = 0L;
	
	private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
	
	
	public SegmentationWindow() {
		super();
	}
	
	public SegmentationWindow(long windowLengthMs) {
		super();
		this.backwardWindowLengthMs = windowLengthMs - 100L;
		this.fowardWindowLengthMs = 100L;
	}
	
	public long getBackwardWindowLengthMs() {
		return this.backwardWindowLengthMs;
	
	}
	
	public void setBackwardWindowLengthMs(long backwardWindowLengthMs) {
		var oldVal = this.backwardWindowLengthMs;
		this.backwardWindowLengthMs = backwardWindowLengthMs;
		propSupport.firePropertyChange("backwardWindowLengthMs", oldVal, this.backwardWindowLengthMs);
	}
	
	public long getForwardWindowLengthMs() {
		return this.fowardWindowLengthMs;
	}
	
	public void setForwardWindowLengtMs(long forwardWindowLengthMs) {
		var oldVal = this.fowardWindowLengthMs;
		this.fowardWindowLengthMs = forwardWindowLengthMs;
		propSupport.firePropertyChange("forwardWindowLengthMs", oldVal, forwardWindowLengthMs);
	}
	
	public long getStartLockMs() {
		return this.segmentStartLockMs;
	}
	
	public void setStartLockMs(long lockMs) {
		var oldVal = this.segmentStartLockMs;
		this.segmentStartLockMs = lockMs;
		propSupport.firePropertyChange("startLockMs", oldVal, lockMs);
	}
	
	/**
	 * Return the current segment window start position given
	 * the playback position.
	 * 
	 *  @param playbackPosition
	 *  
	 */
	public long getWindowStartMs(long playbackPositionMs) {
		long startTime = 0L;
		if(getBackwardWindowLengthMs() > 0) {
			startTime = Math.max(0L, playbackPositionMs - getBackwardWindowLengthMs());
			
			if(segmentStartLockMs >= 0) {
				if(startTime < segmentStartLockMs) {
					startTime = segmentStartLockMs;
				}
			}
			
		} else {
			startTime = Math.max(0L, getStartLockMs());
		}
		return startTime;
	}
	
	/**
	 * Return the current segment window end position given
	 * the playback position.
	 * 
	 * @param playbackPositionMs
	 * @return
	 */
	public long getWindowEndMs(long playbackPositionMs) {
		long endTime = playbackPositionMs + getForwardWindowLengthMs();
		
		return endTime;
	}

	/* Property Support */
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
