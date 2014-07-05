package ca.phon.app.session.editor.view.waveform;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.MenuElement;

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
	
	/**
	 * Add custom commands to the editor view menu.
	 * 
	 * @param menu
	 */
	public void addMenuItems(JMenu menuEle);
	
}
