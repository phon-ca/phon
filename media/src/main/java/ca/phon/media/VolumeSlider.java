package ca.phon.media;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;

public class VolumeSlider extends JComponent {

	private VolumeModel model;

	private int orientation;

	public VolumeSlider() {
		this(new VolumeModel(), SwingConstants.HORIZONTAL);
	}

	public VolumeSlider(VolumeModel model) {
		this(model, SwingConstants.HORIZONTAL);
	}

	public VolumeSlider(int orientation) {
		this(new VolumeModel(), orientation);
	}

	/**
	 *
	 * @param model
	 * @param orientation
	 */
	public VolumeSlider(VolumeModel model, int orientation) {
		super();

		this.model = model;
		model.addPropertyChangeListener(forwardListener);
		checkOrientation(orientation);
		this.orientation = orientation;

		updateUI();
	}

	public VolumeModel getModel() {
		return this.model;
	}

	public void setModel(VolumeModel model) {
		var oldModel = this.model;
		oldModel.removePropertyChangeListener(forwardListener);
		this.model = model;
		model.addPropertyChangeListener(forwardListener);
		firePropertyChange("model", oldModel, model);
	}

	public int getOrientation() {
		return this.orientation;
	}

	private void checkOrientation(int orientation) {
		if(orientation != SwingConstants.HORIZONTAL
			&& orientation != SwingConstants.VERTICAL) {
			throw new IllegalArgumentException("incorrect orientation");
		}
	}

	public void setOrientation(int orientation) {
		checkOrientation(orientation);
		var oldVal = this.orientation;
		this.orientation = orientation;
		firePropertyChange("orientation", oldVal, orientation);
	}

	public float getVolumeLevel() {
		return model.getVolumeLevel();
	}

	public void setVolumeLevel(float volumeLevel) {
		model.setVolumeLevel(volumeLevel);
	}

	public boolean isMuted() {
		return model.isMuted();
	}

	public void setMuted(boolean muted) {
		model.setMuted(muted);
	}

	@Override
	public String getUIClassID() {
		return "VolumeSliderUI";
	}

	public void updateUI() {
		setUI(new DefaultVolumeSliderUI());
	}

	private final PropertyChangeListener forwardListener = (evt) -> {
		firePropertyChange(evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
	};

}
