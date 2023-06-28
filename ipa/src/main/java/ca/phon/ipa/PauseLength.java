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

/**
 * Pause lengths
 */
public enum PauseLength {
	SIMPLE("(.)", "simple"),
	LONG("(..)", "long"),
	VERY_LONG("(...)", "very-long"),
	NUMERIC("(%s)", "numeric");

	private String text;

	private String displayName;

	private PauseLength(String text, String displayName) {
		this.text = text;
		this.displayName = displayName;
	}

	public String getText() {
		return this.text;
	}

	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public String toString() {
		return this.getText();
	}

}
