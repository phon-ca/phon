/*
 * Phon - An open source tool for research in phonology.
 * Copyright (C) 2005 - 2016, Gregory Hedlund <ghedlund@mun.ca> and Yvan Rose <yrose@mun.ca>
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
package ca.phon.session.impl;

import ca.phon.session.MediaSegment;
import ca.phon.session.MediaSegmentFormatter;
import ca.phon.session.MediaUnit;

/**
 * Media segment
 */
public class MediaSegmentImpl implements MediaSegment {
	
	MediaSegmentImpl() {
		super();
	}
	
	MediaSegmentImpl(float start, float end, MediaUnit unit) {
		super();
		this.startValue = start;
		this.endValue = end;
		this.unit = unit;
	}
	
	/**
	 * Start value
	 */
	private float startValue = 0.0f;
	
	/**
	 * End value
	 */
	private float endValue = 0.0f;
	
	/**
	 * Unit (default:ms)
	 */
	private MediaUnit unit = MediaUnit.Millisecond;

	@Override
	public float getStartValue() {
		return startValue;
	}

	@Override
	public void setStartValue(float start) {
		this.startValue = start;
	}

	@Override
	public float getEndValue() {
		return endValue;
	}

	@Override
	public void setEndValue(float end) {
		this.endValue = end;
	}

	@Override
	public MediaUnit getUnitType() {
		return unit;
	}

	@Override
	public void setUnitType(MediaUnit type) {
		this.unit = type;
	}

	public String toString() {
		return (new MediaSegmentFormatter()).format(this);
	}
	
}
