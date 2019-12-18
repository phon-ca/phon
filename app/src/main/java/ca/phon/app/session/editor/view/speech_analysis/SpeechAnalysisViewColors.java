package ca.phon.app.session.editor.view.speech_analysis;

import java.awt.Color;

import javax.swing.UIManager;

public class SpeechAnalysisViewColors {

	public static void install() {
		UIManager.put(INTERVAL_BACKGROUND, DEFAULT_INTERVAL_BACKGROUND);
		UIManager.put(SELECTED_INTERVAL_BACKGROUND, DEFAULT_SELECTED_INTERVAL_BACKGROUND);
		UIManager.put(INTERVAL_MARKER_COLOR, DEFAULT_INTERVAL_MARKER_COLOR);
		UIManager.put(SELECTED_INTERVAL_MARKER_COLOR, DEFAULT_SELECTED_INTERVAL_MARKER_COLOR);
		UIManager.put(CURSOR_MARKER_COLOR, DEFAULT_CURSOR_MARKER_COLOR);
		UIManager.put(PLAYBACK_MARKER_COLOR, DEFAULT_PLAYBACK_MARKER_COLOR);
	}

	/**
	 *  Interval background 
	 */
	public final static String INTERVAL_BACKGROUND = "SpeechAnalysisView.unfocusedInterval";
	public final static Color DEFAULT_INTERVAL_BACKGROUND = new Color(255, 255, 255, 50);
	
	/**
	 * Focused interval background
	 */
	public final static String SELECTED_INTERVAL_BACKGROUND = "SpeechAnalysisView.focusedInterval";
	public final static Color DEFAULT_SELECTED_INTERVAL_BACKGROUND = new Color(50, 125, 200, 50);
	
	
	/**
	 * Unfocused interval marker color
	 */
	public final static String INTERVAL_MARKER_COLOR = "SpeechAnalysisView.intervalMarkerColor";
	public final static Color DEFAULT_INTERVAL_MARKER_COLOR = Color.darkGray;
	
	/**
	 * Focused interval marker color
	 */
	public final static String SELECTED_INTERVAL_MARKER_COLOR = "SpeechAnalysisView.focusedIntervalMarkerColor";
	public final static Color DEFAULT_SELECTED_INTERVAL_MARKER_COLOR = Color.darkGray;
	
	/**
	 * Cursor color
	 */
	public final static String CURSOR_MARKER_COLOR = "SpeechAnalysisView.cursorMarkerColor";
	public final static Color DEFAULT_CURSOR_MARKER_COLOR = Color.lightGray;
	
	/**
	 * Playback marker color
	 */
	public final static String PLAYBACK_MARKER_COLOR = "SpeechAnalysisView.playbackMarkerColor";
	public final static Color DEFAULT_PLAYBACK_MARKER_COLOR = Color.blue;
	
}
