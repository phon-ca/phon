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

import ca.phon.extensions.*;
import ca.phon.session.format.*;
import ca.phon.session.spi.*;

/**
 * Media segment
 *
 */ 
public final class MediaSegment extends ExtendableObject {
	
	private MediaSegmentSPI mediaSegmentImpl;
	
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

	public String toString() {
		return (new MediaSegmentFormatter()).format(this);
	}
	
}
