/*
 * Copyright (C) 2005-2020 Gregory Hedlund & Yvan Rose
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *    http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.phon.app.session.editor.view.timeline;

import java.awt.*;

import javax.swing.*;

public class TimelineViewColors {
		
	public static void install() {
		UIManager.put(INTERVAL_BACKGROUND, DEFAULT_INTERVAL_BACKGROUND);
		UIManager.put(SELECTION_RECTANGLE, DEFAULT_SELECTION_RECTANGLE);
		UIManager.put(FOCUSED_INTERVAL_BACKGROUND, DEFAULT_FOCUSED_INTERVAL_BACKGROUND);
		UIManager.put(SEGMENTATION_INTERVAL_BACKGROUND, DEFAULT_SEGMENTATION_INTERVAL_BACKGROUND);
		UIManager.put(INTERVAL_MARKER_COLOR, DEFAULT_INTERVAL_MARKER_COLOR);
		UIManager.put(FOCUSED_INTERVAL_MARKER_COLOR, DEFAULT_FOCUSED_INTERVAL_MARKER_COLOR);
		UIManager.put(SPLIT_MARKER_COLOR, DEFAULT_SPLIT_MARKER_COLOR);
	}

	/**
	 *  Interval background 
	 */
	public final static String INTERVAL_BACKGROUND = "TimelineView.unfocusedInterval";
	public final static Color DEFAULT_INTERVAL_BACKGROUND = new Color(255, 255, 255, 50);

	public final static String SELECTION_RECTANGLE = "TimelineView.selectionRect";
	public final static Color DEFAULT_SELECTION_RECTANGLE =  new Color(55, 137, 220, 50);
	
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
	
	/**
	 * Split marker color
	 */
	public final static String SPLIT_MARKER_COLOR = "TimelineView.splitMarkerColor";
	public final static Color DEFAULT_SPLIT_MARKER_COLOR = Color.blue;
	
}
