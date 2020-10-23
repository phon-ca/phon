/*
 * Copyright (C) 2012-2018 Gregory Hedlund & Yvan Rose
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
import java.util.*;

/**
 * Audio channels
 *
 */
public enum Channel {
	LEFT("Left", "L", Color.blue),
	RIGHT("Right", "R", Color.green.darker()),
	CENTER("Center", "C", Color.GRAY),
	LOW_FREQUENCY("Low Frequency", "Sub", Color.GRAY),
	SURROUND_LEFT("Surround Left", "SL", Color.GRAY),
	SURROUND_RIGHT("Surround Right", "SR", Color.GRAY),
	SURROUND_BACK_LEFT("Surround Back Left", "SBL", Color.GRAY),
	SURROUND_BACK_RIGHT("Surround Back Right", "SBR", Color.GRAY);
	
	private String name;
	
	private String label;
	
	private Color color;
	
	private Channel(String name, String label, Color color) {
		this.name = name;
		this.label = label;
		this.color = color;
	}
	
	public String getLabel() {
		return this.label;
	}
	
	public int channelNumber() {
		return ordinal();
	}
	
	public Color getColor() {
		return this.color;
	}
	public String getName() {
		return this.name;
	}
	
	public static Map<Channel, Color> createColorMap() {
		final Map<Channel, Color> retVal = new HashMap<Channel, Color>();
		
		for(Channel ch:values()) {
			retVal.put(ch, ch.getColor());
		}
		
		return retVal;
	}
}
