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
package ca.phon.session.spi;

import java.text.ParseException;
import java.util.List;

public interface TierSPI<T>  extends TierDescriptionSPI {

	/**
	 * Validate text and return object of type T
	 *
	 * @param text
	 * @throws java.text.ParseException
	 */
	public T parse(String text) throws ParseException;

	/**
	 * Get tier value
	 */
	public T getValue();

	/**
	 * Set tier value
	 *
	 * @param value
	 */
	public void setValue(T value);

	/**
	 * Set blind transcription
	 *
	 * @param transcriberId
	 * @param value
	 */
	public void setBlindTranscription(String transcriberId, T value);

	/**
	 * Get blind transcription for given transcriber
	 *
	 * @param transcriberId
	 * @return blind transcription for transcriber or null if none or isBlind() is false
	 */
	public T getBlindTranscription(String transcriberId);

	/**
	 * Return list of blind transcribers for this tier
	 *
	 * @param list of transcriberIds
	 */
	public List<String> getTranscribers();

}
