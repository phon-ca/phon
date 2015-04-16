/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2015, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
 * Dept of Linguistics, Memorial University <https://phon.ca>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ca.phon.media.sampled;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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
