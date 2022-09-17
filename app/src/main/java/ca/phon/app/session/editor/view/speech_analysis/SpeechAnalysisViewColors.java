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
package ca.phon.app.session.editor.view.speech_analysis;

import javax.swing.*;
import java.awt.*;

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
