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
package ca.phon.ipa;

import ca.phon.formatter.MediaTimeFormatter;
import ca.phon.ipa.features.FeatureSet;

/**
 * Represents a pause in an IPA transcription.
 * 
 */
public final class Pause extends IPAElement {

	private final PauseLength type;

	/**
	 * Length in seconds
	 */
	private final float length;

	Pause(PauseLength type) {
		this(type, 0.0f);
	}

	Pause(PauseLength type, float seconds) {
		this.type = type;
		this.length = seconds;
	}

	public float getLength() {
		return this.length;
	}

	public PauseLength getType() {
		return this.type;
	}

	private String lengthToString() {
		return MediaTimeFormatter.timeToMinutesAndSeconds(getLength());
	}

	public String text() {
		return switch (type) {
			case SIMPLE, LONG, VERY_LONG -> type.getText();
			case NUMERIC -> String.format(type.getText(), lengthToString());
		};
	}

	@Override
	protected FeatureSet _getFeatureSet() {
		return new FeatureSet();
	}

	@Override
	public String getText() {
		return text();
	}

}
