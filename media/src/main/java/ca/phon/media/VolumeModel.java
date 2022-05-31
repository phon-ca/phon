package ca.phon.media;

import java.beans.*;
import java.util.Map;

/**
 * Volume model used by all media playback elements in the session editor.
 *
 */
public final class VolumeModel {

	public final static float MIN_LEVEL = 0.0f;

	public final static float DEFAULT_LEVEL = 1.0f;

	public final static float MAX_LEVEL = 1.25f;

	/**
	 * Value between MIN_LEVEL and MAX_LEVEL indicating volume level
	 */
	private float volumeLevel;

	/**
	 * Muted
	 */
	public boolean muted;

	private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

	public VolumeModel() {
		super();

		this.volumeLevel = DEFAULT_LEVEL;
		this.muted = false;
	}

	public float getVolumeLevel() {
		return volumeLevel;
	}

	public void setVolumeLevel(float volumeLevel) {
		var oldVal = this.volumeLevel;
		if(volumeLevel < MIN_LEVEL)
			volumeLevel = MIN_LEVEL;
		else if(volumeLevel > MAX_LEVEL)
			volumeLevel = MAX_LEVEL;
		this.volumeLevel = volumeLevel;
		propSupport.firePropertyChange("volumeLevel", oldVal, volumeLevel);
	}

	public boolean isMuted() {
		return muted;
	}

	public void setMuted(boolean muted) {
		var oldVal = this.muted;
		this.muted = muted;
		propSupport.firePropertyChange("muted", oldVal, muted);
	}

	public float getMaximumVolumeLevel() {
		return MAX_LEVEL;
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
