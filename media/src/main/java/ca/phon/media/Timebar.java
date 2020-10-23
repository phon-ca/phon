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
package ca.phon.media;

import java.awt.*;

import javax.swing.*;

import ca.phon.media.TimeUIModel.*;

public class Timebar extends TimeComponent {

	public final static int DEFAULT_MINOR_TICK_HEIGHT = 5;
	public final static int DEFUALT_MAJOR_TICK_HEIGHT = 8;
	
	private int minorTickHeight = DEFAULT_MINOR_TICK_HEIGHT;
	private int majorTickHeight = DEFUALT_MAJOR_TICK_HEIGHT;
	
	private final static String uiClassId = "TimebarUI";
	
	public Timebar() {
		this(new TimeUIModel());
	}
	
	public Timebar(TimeUIModel model) {
		super(model);
		
		setFont(UIManager.getFont("Label.font"));
		updateUI();
	}
	
	public String getUIClassID() {
		return uiClassId;
	}

	@Override
	public void updateUI() {
		setUI(new DefaultTimebarUI());
	}

	public DefaultTimebarUI getUI() {
		return (DefaultTimebarUI)ui;
	}
	
	public int getMinorTickHeight() {
		return minorTickHeight;
	}

	public void setMinorTickHeight(int minorTickHeight) {
		var oldVal = this.minorTickHeight;
		this.minorTickHeight = minorTickHeight;
		super.firePropertyChange("minorTickHeight", oldVal, minorTickHeight);
	}

	public int getMajorTickHeight() {
		return majorTickHeight;
	}

	public void setMajorTickHeight(int majorTickHeight) {
		var oldVal = this.majorTickHeight;
		this.majorTickHeight = majorTickHeight;
		super.firePropertyChange("majorTickHeight", oldVal, majorTickHeight);
	}
	
	public boolean isRepaintAll() {
		return true;
	}

	@Override
	public void repaint(float startTime, float endTime) {
		repaintAll();
	}

	@Override
	public void repaint(long tn, float startTime, float endTime) {
		repaintAll();
	}

	@Override
	public void repaintInterval(Interval interval) {
		repaintAll();
	}

	@Override
	public void repaintInterval(long tn, Interval interval) {
		repaintAll();
	}

	@Override
	public void repaintMarker(Marker marker) {
		repaintAll();
	}

	@Override
	public void repaintMarker(long tn, Marker marker) {
		repaintAll();
	}
	
	@Override
	public void repaint(Rectangle clip) {
		repaintAll();
	}
	
	public void repaintAll() {
		super.repaint(getVisibleRect());
	}
	
}
