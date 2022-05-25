package ca.phon.media;

import javax.swing.*;
import java.awt.*;

public class VolumeSlider extends JComponent {

	private VolumeModel model;

	private int orientation;

	public VolumeSlider() {
		this(new VolumeModel(), SwingConstants.HORIZONTAL);
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
		checkOrientation(orientation);
		this.orientation = orientation;

		updateUI();
	}

	public VolumeModel getModel() {
		return this.model;
	}

	public void setModel(VolumeModel model) {
		var oldModel = this.model;
		this.model = model;
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

	public static void main(String[] args) throws Exception {
		final JFrame testFrame = new JFrame("Test");
		final VolumeSlider slider = new VolumeSlider();
		slider.setVolumeLevel(0.5f);
		testFrame.getContentPane().setLayout(new BorderLayout());
		testFrame.getContentPane().add(slider, BorderLayout.NORTH);
		testFrame.pack();
		testFrame.setVisible(true);
	}

}
