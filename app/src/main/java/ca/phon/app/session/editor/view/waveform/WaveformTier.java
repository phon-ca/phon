package ca.phon.app.session.editor.view.waveform;

import javax.swing.JComponent;

/**
 * Extension point for waveform view tiers.
 *
 */
public interface WaveformTier {
	
	/**
	 * Get the tier component
	 * 
	 * @return component
	 */
	public JComponent getTierComponent();
	
}
