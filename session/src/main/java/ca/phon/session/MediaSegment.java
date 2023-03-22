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

	public float getStartValue() {
		return mediaSegmentImpl.getStartValue();
	}

	public void setStartValue(float start) {
		mediaSegmentImpl.setStartValue(start);
	}

	public float getEndValue() {
		return mediaSegmentImpl.getEndValue();
	}

	public void setEndValue(float end) {
		mediaSegmentImpl.setEndValue(end);
	}

	public MediaUnit getUnitType() {
		return mediaSegmentImpl.getUnitType();
	}

	public void setUnitType(MediaUnit type) {
		mediaSegmentImpl.setUnitType(type);
	}

	public void setSegment(float startValue, float endValue) {
		setSegment(startValue, endValue, getUnitType());
	}

	public void setSegment(float startValue, float endValue, MediaUnit unit) {
		setStartValue(startValue);
		setEndValue(endValue);
		setUnitType(unit);
	}

	public void setSegment(MediaSegment seg) {
		setSegment(seg.getStartValue(), seg.getEndValue(), seg.getUnitType());
	}

	public boolean equals(Object b) {
		if(!(b instanceof MediaSegment bSeg)) return false;
		return getStartValue() == bSeg.getStartValue()
				&& getEndValue() == bSeg.getEndValue()
				&& getUnitType() == bSeg.getUnitType();
	}

	public String toString() {
		return (new MediaSegmentFormatter()).format(this);
	}
	
}
