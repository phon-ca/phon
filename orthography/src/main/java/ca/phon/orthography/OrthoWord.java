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
package ca.phon.orthography;


/**
 * A word in an orthographic phrase.
 * Words may have prefix and/or suffix
 * codes.
 */
public class OrthoWord extends AbstractOrthoElement {
	
	private final WordPrefix prefix;
	
	private final WordSuffix suffix;
	
	private final UntranscribedType untranscribed;
	
	private final String data;
	
	public OrthoWord(String data) {
		super();
		this.data = data;
		this.prefix = null;
		this.suffix = null;
		this.untranscribed = null;
	}
	
	public OrthoWord(String data, UntranscribedType untranscribed) {
		super();
		this.data = data;
		this.prefix = null;
		this.suffix = null;
		this.untranscribed = untranscribed;
	}

	public OrthoWord(String data, WordPrefixType prefix) {
		this(data, prefix, null);
	}
	
	public OrthoWord(String data, WordSuffixType suffix) {
		this(data, null, suffix);
	}
	
	public OrthoWord(String data, WordPrefixType prefix, WordSuffixType suffix) {
		super();
		this.prefix = new WordPrefix(prefix);
		this.suffix = new WordSuffix(suffix);
		this.data = data;
		this.untranscribed = null;
	}
	
	public OrthoWord(String data, WordPrefix prefix, WordSuffix suffix) {
		super();
		this.prefix = prefix;
		this.suffix = suffix;
		this.data = data;
		this.untranscribed = null;
	}
	
	public OrthoWord(String data, WordPrefix prefix, WordSuffix suffix, UntranscribedType untranscribed) {
		super();
		this.prefix = prefix;
		this.suffix = suffix;
		this.data = data;
		this.untranscribed = untranscribed;
	}
	
	public OrthoWord(String data, WordPrefixType prefix, WordSuffixType suffix, UntranscribedType untranscribed) {
		super();
		this.prefix = new WordPrefix(prefix);
		this.suffix = new WordSuffix(suffix);
		this.data = data;
		this.untranscribed = untranscribed;
	}
	
	public boolean isUntranscribed() {
		return this.untranscribed != null;
	}
	
	public UntranscribedType getUntranscribedType() {
		return this.untranscribed;
	}
	
	/**
	 * Get prefix for word.
	 * 
	 * @return the word prefix, or <code>null</code> if
	 *  none
	 */
	public WordPrefix getPrefix() {
		return this.prefix;
	}
	
	/**
	 * Get suffix for word.
	 * 
	 * @return the word suffix, or <code>null</code> if
	 *  none
	 */
	public WordSuffix getSuffix() {
		return this.suffix;
	}
	
	/**
	 * Get the root word data (without prefix/suffix)
	 * 
	 * @return the root word 
	 */
	public String getWord() {
		return this.data;
	}
	
	@Override
	public String text() {
		return (
			(this.prefix == null ? "" : this.prefix) + 
			this.data + 
			(this.suffix == null ? "" : this.suffix)
		);
	}
	
	@Override
	public String toString() {
		return text();
	}
	
}
