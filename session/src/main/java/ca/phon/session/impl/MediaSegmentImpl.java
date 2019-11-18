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
package ca.phon.session.impl;

import ca.phon.session.MediaSegment;
import ca.phon.session.MediaSegmentFormatter;
import ca.phon.session.MediaUnit;
import ca.phon.session.spi.MediaSegmentSPI;

/**
 * Media segment
 */
public class MediaSegmentImpl implements MediaSegmentSPI {
	
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

}
