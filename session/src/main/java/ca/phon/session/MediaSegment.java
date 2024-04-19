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

package ca.phon.session;

import ca.phon.extensions.ExtendableObject;
import ca.phon.formatter.MediaTimeFormatStyle;
import ca.phon.session.format.MediaSegmentFormatter;
import ca.phon.session.spi.MediaSegmentSPI;

/**
 * Media segment
 *
 */ 
public final class MediaSegment extends ExtendableObject {
	
	private final MediaSegmentSPI mediaSegmentImpl;
	
	MediaSegment(MediaSegmentSPI impl) {
		super();
		this.mediaSegmentImpl = impl;
	}

	/**
	 * Get start value as a floating point number
	 *
	 * @return start value
	 */
	public float getStartValue() {
		return mediaSegmentImpl.getStartValue();
	}

	/**
	 * Set start value, unit is specified by getUnitType()
	 *
	 * @param start
	 */
	public void setStartValue(float start) {
		mediaSegmentImpl.setStartValue(start);
	}

	/**
	 * Get end value
	 *
	 * @return end value
	 */
	public float getEndValue() {
		return mediaSegmentImpl.getEndValue();
	}

	/**
	 * Set end value
	 *
	 * @param end
	 */
	public void setEndValue(float end) {
		mediaSegmentImpl.setEndValue(end);
	}

	/**
	 * Get media unit represented by getStartValue() and getEndValue()
	 *
	 * @return media unit
	 */
	public MediaUnit getUnitType() {
		return mediaSegmentImpl.getUnitType();
	}

	/**
	 * Return start time in seconds
	 *
	 * @return start time in seconds
	 */
	public float getStartTime() {
		if(getUnitType() == MediaUnit.Second) {
			return getStartValue();
		} else if(getUnitType() == MediaUnit.Millisecond) {
			return getStartValue() / 1000.0f;
		} else {
			throw new IllegalArgumentException("Unsupported unit type: " + getUnitType());
		}
	}

	/**
	 * Set start time in seconds
	 *
	 * @param startTime
	 */
	public void setStartTime(float startTime) {
		if(getUnitType() == MediaUnit.Second) {
			setStartValue(startTime);
		} else if(getUnitType() == MediaUnit.Millisecond) {
			setStartValue(startTime * 1000.0f);
		} else {
			throw new IllegalArgumentException("Unsupported unit type: " + getUnitType());
		}
	}

	/**
	 * Return start time in milliseconds
	 *
	 * @return start time in milliseconds
	 */
	public long getStartValueMs() {
		if(getUnitType() == MediaUnit.Second) {
			return (long)(getStartValue() * 1000.0f);
		} else if(getUnitType() == MediaUnit.Millisecond) {
			return (long)getStartValue();
		} else {
			throw new IllegalArgumentException("Unsupported unit type: " + getUnitType());
		}
	}

	/**
	 * Set start time in milliseconds
	 *
	 * @param startTime
	 */
	public void setStartTimeMs(long startTime) {
		if(getUnitType() == MediaUnit.Second) {
			setStartValue(startTime / 1000.0f);
		} else if(getUnitType() == MediaUnit.Millisecond) {
			setStartValue(startTime);
		} else {
			throw new IllegalArgumentException("Unsupported unit type: " + getUnitType());
		}
	}

	/**
	 * Return end time in seconds
	 *
	 * @return end time in seconds
	 */
	public float getEndTime() {
		if(getUnitType() == MediaUnit.Second) {
			return getEndValue();
		} else if(getUnitType() == MediaUnit.Millisecond) {
			return getEndValue() / 1000.0f;
		} else {
			throw new IllegalArgumentException("Unsupported unit type: " + getUnitType());
		}
	}

	/**
	 * Set end time in seconds
	 *
	 * @param endTime
	 */
	public void setEndTime(float endTime) {
		if(getUnitType() == MediaUnit.Second) {
			setEndValue(endTime);
		} else if(getUnitType() == MediaUnit.Millisecond) {
			setEndValue(endTime * 1000.0f);
		} else {
			throw new IllegalArgumentException("Unsupported unit type: " + getUnitType());
		}
	}

	/**
	 * Return end time in milliseconds
	 *
	 * @return end time in milliseconds
	 */
	public long getEndValueMs() {
		if(getUnitType() == MediaUnit.Second) {
			return (long)(getEndValue() * 1000.0f);
		} else if(getUnitType() == MediaUnit.Millisecond) {
			return (long)getEndValue();
		} else {
			throw new IllegalArgumentException("Unsupported unit type: " + getUnitType());
		}
	}

	/**
	 * Set end time in milliseconds
	 *
	 * @param endTime
	 */
	public void setEndTimeMs(long endTime) {
		if(getUnitType() == MediaUnit.Second) {
			setEndValue(endTime / 1000.0f);
		} else if(getUnitType() == MediaUnit.Millisecond) {
			setEndValue(endTime);
		} else {
			throw new IllegalArgumentException("Unsupported unit type: " + getUnitType());
		}
	}

	/**
	 * Return segment length in seconds
	 *
	 * @return length in seconds
	 */
	public float getLength() {
		return getEndTime() - getStartTime();
	}

	/**
	 * Return segment length in milliseconds
	 *
	 * @return length in milliseconds
	 */
	public long getLengthMs() {
		return getEndValueMs() - getStartValueMs();
	}

	/**
	 * Set media unit for segment
	 *
	 * @param mediaUnit
	 */
	public void setUnitType(MediaUnit mediaUnit) {
		mediaSegmentImpl.setUnitType(mediaUnit);
	}

	public void setSegment(float startValue, float endValue) {
		setSegment(startValue, endValue, getUnitType());
	}

	/**
	 * Is the media segment a single point
	 * @return true if getStartValue() == getEndValue()
	 */
	public boolean isPoint() {
		return getStartValue() == getEndValue();
	}

	/**
	 * Is the media segment a single point at the origin (0.0f)
	 *
	 * @return true if isPoint() == true && getStartValue() == 0.0f
	 */
	public boolean isPointAtOrigin() {
		return isPoint() && getStartValue() == 0.0f;
	}

	public void setSegment(float startValue, float endValue, MediaUnit unit) {
		setStartValue(startValue);
		setEndValue(endValue);
		setUnitType(unit);
	}

	public void setSegment(MediaSegment seg) {
		setSegment(seg.getStartValue(), seg.getEndValue(), seg.getUnitType());
	}

	/**
	 * Equals will return true if start/end <em>times</em> are the same. Unit is ignored
	 * @param b
	 * @return
	 */
	public boolean equals(Object b) {
		if(!(b instanceof MediaSegment bSeg)) return false;
		return (getStartTime() == bSeg.getStartTime() && getEndTime() == bSeg.getEndTime());
	}

	public String toString() {
		return (new MediaSegmentFormatter(MediaTimeFormatStyle.MINUTES_AND_SECONDS)).format(this);
	}
	
}
