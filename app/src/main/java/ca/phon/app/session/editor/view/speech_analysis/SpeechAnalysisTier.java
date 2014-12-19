package ca.phon.app.session.editor.view.speech_analysis;

import javax.swing.JComponent;
import javax.swing.JMenu;

/**
 * Extension point for waveform view tiers.
 *
 */
public interface SpeechAnalysisTier {
	
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
	
	/**
	 * Called on the Refresh action for the tier.
	 * 
	 */
	public void onRefresh();
	
}
