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
package ca.phon.media.player;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class TimeSlider extends JSlider {

	private static final long serialVersionUID = -3955798002058380628L;

	private final List<TimeSliderManualChangeListener> manualChangeListeners = new ArrayList<>();

	public TimeSlider() {
		super();
		
		setUI(new TimeSliderUI());
	}

	/**
	 * Add a manual change listener to this slider.
	 *
	 * @param listener
	 */
	public void addManualChangeListener(TimeSliderManualChangeListener listener) {
		if(listener == null) throw new NullPointerException("listener must not be null");
		synchronized(manualChangeListeners) {
			if(!manualChangeListeners.contains(listener)) {
				manualChangeListeners.add(listener);
			}
		}
	}

	/**
	 * Remove a manual change listener from this slider.
	 *
	 * @param listener
	 */
	public void removeManualChangeListener(TimeSliderManualChangeListener listener) {
		synchronized(manualChangeListeners) {
			if(manualChangeListeners.contains(listener)) {
				manualChangeListeners.remove(listener);
			}
		}
	}

	/**
	 * Notify all listeners of a manual change.
	 *
	 * @param value
	 */
	public void notifyManualChange(long value) {
		synchronized(manualChangeListeners) {
			for(TimeSliderManualChangeListener listener : manualChangeListeners) {
				listener.manualChange(this, value);
			}
		}
	}

	public interface TimeSliderManualChangeListener {
		/**
		 * Called when the user manually changes the slider value.
		 *
		 * @param slider
		 * @param value
		 */
		void manualChange(TimeSlider slider, long value);
	}

}
