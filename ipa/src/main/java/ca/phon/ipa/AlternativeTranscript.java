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

import ca.phon.extensions.Extension;

import java.util.HashMap;

/**
 * Extension for alternative forms for an IPATranscript.
 * 
 * This is most often used for mutl-blind transcription methods.
 */
@Extension(IPATranscript.class)
public class AlternativeTranscript extends HashMap<String, IPATranscript> {
	
	private static final long serialVersionUID = -9179068664990132970L;
	
	private String selected = null;

	public AlternativeTranscript() {
		super();
	}
	
	/**
	 * Set selected transcriber key
	 * 
	 * @param selected
	 */
	public void setSelected(String selected) {
		this.selected = selected;
	}
	
	/**
	 * Get selected transcriber key
	 * 
	 * @return selected transcriber or <code>null</code>
	 */
	public String getSelected() {
		return this.selected;
	}
	
}
