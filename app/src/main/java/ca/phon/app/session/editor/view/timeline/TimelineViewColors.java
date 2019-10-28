package ca.phon.app.session.editor.view.timeline;

import java.awt.Color;

import javax.swing.UIManager;

public class TimelineViewColors {
		
	public static void install() {
		UIManager.put(INTERVAL_BACKGROUND, DEFAULT_INTERVAL_BACKGROUND);
		UIManager.put(FOCUSED_INTERVAL_BACKGROUND, DEFAULT_FOCUSED_INTERVAL_BACKGROUND);
		UIManager.put(SEGMENTATION_INTERVAL_BACKGROUND, DEFAULT_SEGMENTATION_INTERVAL_BACKGROUND);
		UIManager.put(INTERVAL_MARKER_COLOR, DEFAULT_INTERVAL_MARKER_COLOR);
		UIManager.put(FOCUSED_INTERVAL_MARKER_COLOR, DEFAULT_FOCUSED_INTERVAL_MARKER_COLOR);
	}

	/**
	 *  Interval background 
	 */
	public final static String INTERVAL_BACKGROUND = "TimelineView.unfocusedInterval";
	public final static Color DEFAULT_INTERVAL_BACKGROUND = new Color(255, 255, 255, 50);
	
	/**
	 * Focused interval background
	 */
	public final static String FOCUSED_INTERVAL_BACKGROUND = "TimelineView.focusedInterval";
	public final static Color DEFAULT_FOCUSED_INTERVAL_BACKGROUND = new Color(50, 125, 200, 50);
	
	/**
	 * Segmentation interval background
	 */
	public final static String SEGMENTATION_INTERVAL_BACKGROUND = "TimelineView.segmentationIntervalBackground";
	public final static Color DEFAULT_SEGMENTATION_INTERVAL_BACKGROUND = new Color(255, 255, 0, 50);
	
	/**
	 * Unfocused interval marker color
	 */
	public final static String INTERVAL_MARKER_COLOR = "TimelineView.intervalMarkerColor";
	public final static Color DEFAULT_INTERVAL_MARKER_COLOR = Color.lightGray;
	
	/**
	 * Focused interval marker color
	 */
	public final static String FOCUSED_INTERVAL_MARKER_COLOR = "TimelineView.focusedIntervalMarkerColor";
	public final static Color DEFAULT_FOCUSED_INTERVAL_MARKER_COLOR = Color.darkGray;
	
}
